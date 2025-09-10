package org.badmintonchain.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class OpenAiClient {

    private final WebClient webClient;
    private final String apiKey;

    // Quota/ngày
    private final AtomicInteger chatCount = new AtomicInteger(0);
    private final AtomicInteger embeddingCount = new AtomicInteger(0);
    private LocalDate currentDate = LocalDate.now();
    private static final int CHAT_LIMIT_PER_DAY = 100;
    private static final int EMBEDDING_LIMIT_PER_DAY = 200;

    private final Semaphore semaphore = new Semaphore(1);

    // Sliding window rate limiter
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofSeconds(60);
    private static final int MAX_REQUESTS_PER_WINDOW = 20;
    private final Queue<Long> requestTimestamps = new ConcurrentLinkedQueue<>();

    public OpenAiClient(WebClient.Builder builder,
                        @Value("${openai.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = builder.baseUrl("https://generativelanguage.googleapis.com").build();
    }

    private void resetIfNewDay() {
        if (!LocalDate.now().equals(currentDate)) {
            currentDate = LocalDate.now();
            chatCount.set(0);
            embeddingCount.set(0);
        }
    }

    private boolean allowRequest() {
        long now = System.currentTimeMillis();
        long windowStart = now - RATE_LIMIT_WINDOW.toMillis();
        while (!requestTimestamps.isEmpty() && requestTimestamps.peek() < windowStart) {
            requestTimestamps.poll();
        }
        if (requestTimestamps.size() < MAX_REQUESTS_PER_WINDOW) {
            requestTimestamps.offer(now);
            return true;
        }
        return false;
    }

    private Retry createRetrySpec() {
        return Retry.backoff(2, Duration.ofSeconds(2))
                .filter(ex -> ex instanceof WebClientResponseException.TooManyRequests
                        || ex instanceof WebClientResponseException.ServiceUnavailable)
                .maxBackoff(Duration.ofSeconds(20))
                .doBeforeRetry(signal -> {
                    Throwable err = signal.failure();
                    log.warn("Gemini call failed (will retry): {}", err == null ? "unknown" : err.getMessage());
                })
                .onRetryExhaustedThrow((spec, signal) ->
                        new RuntimeException("Retries exhausted when calling Gemini API", signal.failure())
                );
    }

    // Embedding
    public float[] createEmbedding(String text) {
        resetIfNewDay();
        if (embeddingCount.incrementAndGet() > EMBEDDING_LIMIT_PER_DAY) {
            throw new RuntimeException("Quá giới hạn embedding trong ngày (" + EMBEDDING_LIMIT_PER_DAY + ")");
        }

        while (!allowRequest()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            semaphore.acquire();

            // Đúng schema cho batchEmbedContents
            Map<String, Object> requestItem = Map.of(
                    "model", "models/text-embedding-004",
                    "content", Map.of(
                            "parts", List.of(Map.of("text", text))
                    )
            );

            Map<String, Object> body = Map.of(
                    "requests", List.of(requestItem)
            );

            JsonNode resp = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/text-embedding-004:batchEmbedContents")
                            .queryParam("key", apiKey)
                            .build())
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .retryWhen(createRetrySpec())
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (resp == null || !resp.has("embeddings")) {
                throw new RuntimeException("Invalid response from Gemini embedding API");
            }

            JsonNode arr = resp.get("embeddings").get(0).get("values");
            float[] embedding = new float[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                embedding[i] = (float) arr.get(i).asDouble();
            }
            return embedding;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (WebClientResponseException e) {
            log.error("Gemini embedding error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Failed to create embedding: {}", e.getMessage());
            throw new RuntimeException("Failed to create embedding", e);
        } finally {
            semaphore.release();
        }
    }

    // Chat
    public String chatCompletion(String prompt, String context, String question) {
        resetIfNewDay();
        if (chatCount.incrementAndGet() > CHAT_LIMIT_PER_DAY) {
            return "Quá giới hạn chat trong ngày (" + CHAT_LIMIT_PER_DAY + ")";
        }

        while (!allowRequest()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Gộp prompt + context + question
        String fullPrompt = prompt + "\nContext:\n" + context + "\nQuestion:\n" + question;

        try {
            semaphore.acquire();

            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", fullPrompt)
                            ))
                    )
            );

            JsonNode resp = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/gemini-2.0-flash:generateContent")
                            .queryParam("key", apiKey)
                            .build())
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .retryWhen(createRetrySpec())
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (resp == null || !resp.has("candidates") || resp.get("candidates").isEmpty()) {
                return "Không nhận được phản hồi hợp lệ từ Gemini";
            }

            return resp.get("candidates").get(0)
                    .get("content")
                    .get("parts").get(0)
                    .get("text").asText();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Thao tác bị gián đoạn";
        } catch (WebClientResponseException e) {
            log.error("Gemini chat error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "Lỗi gọi Gemini API: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
        } catch (Exception e) {
            log.error("Failed to call chatCompletion: {}", e.getMessage());
            return "Lỗi khi gọi dịch vụ chat";
        } finally {
            semaphore.release();
        }
    }
}
