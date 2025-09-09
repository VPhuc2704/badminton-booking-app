package org.badmintonchain.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final OpenAiClient openAiClient;
    private final DocumentSearchService documentSearchService;

    public String chat(String question) {
        // 1. Tạo embedding cho câu hỏi
        float[] queryEmbedding = openAiClient.createEmbedding(question);

        // 2. Tìm chunks liên quan
        List<String> chunks = documentSearchService.searchRelevantChunks(queryEmbedding, 3);

        // 3. Ghép context
        String context = String.join("\n", chunks);

        // 4. Gọi OpenAI để trả lời
        return openAiClient.chatCompletion(
                "Bạn là trợ lý sân cầu lông. Chỉ trả lời dựa trên dữ liệu dịch vụ cung cấp.",
                context,
                question
        );
    }
}

