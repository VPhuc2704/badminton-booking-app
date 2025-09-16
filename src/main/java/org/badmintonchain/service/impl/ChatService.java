package org.badmintonchain.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private final OpenAiClient openAiClient;
    private final DocumentSearchService documentSearchService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${weather.api.key}")
    private String weatherApiKey;

    public String chat(String question) {
        float[] queryEmbedding = openAiClient.createEmbedding(question);
        List<String> chunks = documentSearchService.searchRelevantChunks(queryEmbedding, 3);
        String context = String.join("\n", chunks);

        String intentPrompt = """
        Bạn là hệ thống phân loại intent cho sân cầu lông.
        
        Nếu user hỏi về kiểm tra sân trống, hãy trả về JSON với format:
        {"intent":"checkAvailability","courtId":x,"date":"YYYY-MM-DD","startTime":"HH:mm","endTime":"HH:mm"}
        
        Lưu ý:
        - "hôm nay" = ngày hiện tại
        - "ngày mai" = ngày hiện tại + 1
        - "ngày mốt" = ngày hiện tại + 2
        - "thứ 2, thứ 3..." = thứ trong tuần này hoặc tuần sau
        - Nếu không có giờ cụ thể, mặc định 08:00-10:00
        - Nếu chỉ có giờ bắt đầu, thêm 2 tiếng cho giờ kết thúc
        
        Nếu không phải kiểm tra sân, trả về: {"intent":"normal"}
        
        Câu hỏi: """ + question;

        String intent = openAiClient.chatCompletion(intentPrompt, "", "");

        if (intent.contains("\"checkAvailability\"")) {
            try {
                JsonNode intentNode = parseIntentResponse(intent);
                if (intentNode == null) {
                    return "Không thể phân tích được intent từ AI response.";
                }

                // Trích xuất thông tin với validation
                Long courtId = extractCourtId(intentNode);
                String date = extractAndValidateDate(intentNode, question);
                String startTime = extractAndValidateTime(intentNode, "startTime", "08:00");
                String endTime = extractAndValidateTime(intentNode, "endTime",
                        calculateDefaultEndTime(startTime));

                if (courtId == null) {
                    return "Vui lòng chỉ rõ số sân cần kiểm tra (ví dụ: sân 1, sân 2).";
                }

                if (date == null) {
                    return "Vui lòng chỉ rõ ngày cần kiểm tra (ví dụ: hôm nay, ngày mai, 2025-01-01).";
                }

                return checkCourtAvailability(courtId, date, startTime, endTime);

            } catch (Exception e) {
                log.error("Lỗi khi xử lý intent kiểm tra sân: ", e);
                return "Xin lỗi, có lỗi khi xử lý yêu cầu kiểm tra sân. Vui lòng thử lại với format rõ ràng hơn.";
            }
        } else {
            // Trường hợp hỏi dịch vụ
            return openAiClient.chatCompletion(
                    """
                    Bạn là trợ lý sân cầu lông.
                    Nhiệm vụ: trả lời CHÍNH XÁC dựa trên dữ liệu dịch vụ cung cấp bên dưới.
                    - Nếu người dùng hỏi tất cả dịch vụ, hãy liệt kê đầy đủ tên, loại, giá, mô tả.
                    - Nếu người dùng hỏi chi tiết một dịch vụ, chỉ trả lời đúng thông tin dịch vụ đó.
                    - Nếu không thấy thông tin, trả lời: "Xin lỗi, tôi không tìm thấy thông tin trong dữ liệu dịch vụ."
                    """,
                    context,
                    question
            );
        }
    }

    private JsonNode parseIntentResponse(String intent) {
        try {
            String cleanedIntent = intent.trim();

            // Loại bỏ markdown code blocks
            if (cleanedIntent.startsWith("```") && cleanedIntent.endsWith("```")) {
                cleanedIntent = cleanedIntent.substring(3, cleanedIntent.length() - 3);
                if (cleanedIntent.startsWith("json")) {
                    cleanedIntent = cleanedIntent.substring(4);
                }
            }

            // Loại bỏ backticks
            if (cleanedIntent.startsWith("`") && cleanedIntent.endsWith("`")) {
                cleanedIntent = cleanedIntent.substring(1, cleanedIntent.length() - 1);
            }

            // Tìm JSON object
            int start = cleanedIntent.indexOf("{");
            int end = cleanedIntent.lastIndexOf("}");
            if (start >= 0 && end >= 0 && start < end) {
                cleanedIntent = cleanedIntent.substring(start, end + 1);
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(cleanedIntent);

        } catch (Exception e) {
            log.warn("Không thể parse intent response: " + intent, e);
            return null;
        }
    }

    private Long extractCourtId(JsonNode node) {
        if (node.has("courtId") && !node.get("courtId").isNull()) {
            return node.get("courtId").asLong();
        }
        return null;
    }

    private String extractAndValidateDate(JsonNode node, String originalQuestion) {
        String dateStr = null;
        String extractedFromQuestion = extractDateFromQuestion(originalQuestion);
        if (extractedFromQuestion != null) {
            return extractedFromQuestion;
        }

        if (node.has("date") && !node.get("date").isNull()) {
            dateStr = node.get("date").asText();
        }

        // Nếu AI không trích xuất được ngày, thử phân tích từ câu hỏi gốc
        if (dateStr == null || dateStr.isEmpty() || "null".equals(dateStr)) {
            dateStr = extractDateFromQuestion(originalQuestion);
        }

        return validateAndFormatDate(dateStr);
    }

    private String extractDateFromQuestion(String question) {
        String lowerQuestion = question.toLowerCase();
        LocalDate today = LocalDate.now();

        // Hôm nay
        if (lowerQuestion.contains("hôm nay")) {
            return today.toString();
        }
        // Ngày mai
        if (lowerQuestion.contains("ngày mai")) {
            return today.plusDays(1).toString();
        }
        // Ngày mốt
        if (lowerQuestion.contains("ngày mốt")) {
            return today.plusDays(2).toString();
        }

        // Thứ 2 - CN
        Map<String, DayOfWeek> dayMap = new HashMap<>();
        dayMap.put("thứ 2", DayOfWeek.MONDAY);
        dayMap.put("thứ hai", DayOfWeek.MONDAY);
        dayMap.put("thứ 3", DayOfWeek.TUESDAY);
        dayMap.put("thứ ba", DayOfWeek.TUESDAY);
        dayMap.put("thứ 4", DayOfWeek.WEDNESDAY);
        dayMap.put("thứ tư", DayOfWeek.WEDNESDAY);
        dayMap.put("thứ 5", DayOfWeek.THURSDAY);
        dayMap.put("thứ năm", DayOfWeek.THURSDAY);
        dayMap.put("thứ 6", DayOfWeek.FRIDAY);
        dayMap.put("thứ sáu", DayOfWeek.FRIDAY);
        dayMap.put("thứ 7", DayOfWeek.SATURDAY);
        dayMap.put("thứ bảy", DayOfWeek.SATURDAY);
        dayMap.put("chủ nhật", DayOfWeek.SUNDAY);

        for (Map.Entry<String, DayOfWeek> entry : dayMap.entrySet()) {
            if (lowerQuestion.contains(entry.getKey())) {
                DayOfWeek target = entry.getValue();
                DayOfWeek todayDow = today.getDayOfWeek();

                int diff = target.getValue() - todayDow.getValue();
                if (diff <= 0) diff += 7; // luôn lấy ngày tương lai gần nhất

                // Nếu có từ "tuần sau" thì cộng thêm 7 ngày
                if (lowerQuestion.contains("tuần sau")) {
                    diff += 7;
                }

                return today.plusDays(diff).toString();
            }
        }

        // Pattern dd/MM/yyyy hoặc dd/MM
        Pattern datePattern = Pattern.compile("(\\d{1,2})/(\\d{1,2})(?:/(\\d{2,4}))?");
        Matcher matcher = datePattern.matcher(question);
        if (matcher.find()) {
            int day = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int year = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : today.getYear();

            if (year < 100) year += 2000; // 24 -> 2024

            try {
                LocalDate parsedDate = LocalDate.of(year, month, day);
                if (parsedDate.isBefore(today)) {
                    return today.toString();
                }
                return parsedDate.toString();
            } catch (Exception e) {
                log.warn("Invalid date extracted: {}/{}/{}", day, month, year);
            }
        }

        return null;
    }

    private String validateAndFormatDate(String dateStr) {
        LocalDate today = LocalDate.now();
        if (dateStr == null || dateStr.isEmpty() || "null".equals(dateStr)) {
            return today.toString();
        }

        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);

            if (date.isBefore(today)) {
                return today.toString();
            }
            return date.toString();
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format: " + dateStr);
            return today.toString();
        }
    }

    private String extractAndValidateTime(JsonNode node, String field, String defaultValue) {
        String timeStr = defaultValue;

        if (node.has(field) && !node.get(field).isNull()) {
            timeStr = node.get(field).asText();
        }

        return validateAndFormatTime(timeStr, defaultValue);
    }

    private String validateAndFormatTime(String timeStr, String defaultValue) {
        if (timeStr == null || timeStr.isEmpty() || "null".equals(timeStr)) {
            return defaultValue;
        }

        try {
            LocalTime time = LocalTime.parse(timeStr);
            return time.format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            log.warn("Invalid time format: " + timeStr);
            return defaultValue;
        }
    }

    private String calculateDefaultEndTime(String startTime) {
        try {
            LocalTime start = LocalTime.parse(startTime);
            return start.plusHours(2).format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return "10:00";
        }
    }

    private String getWeatherAdvice(String date, String startTime, String location) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://api.openweathermap.org/data/2.5/forecast")
                    .queryParam("id", location)
                    .queryParam("appid", weatherApiKey)
                    .queryParam("units", "metric")
                    .queryParam("lang", "vi")
                    .toUriString();
            System.out.println("url: " + url);

            JsonNode forecastData = restTemplate.getForObject(url, JsonNode.class);

            if (forecastData == null || !forecastData.has("list")) {
                return "Không lấy được thông tin thời tiết.";
            }

            LocalDateTime targetDateTime = LocalDateTime.parse(
                    date + "T" + startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
            );

            JsonNode bestMatch = null;
            long minDiff = Long.MAX_VALUE;

            for (JsonNode item : forecastData.get("list")) {
                String forecastTime = item.get("dt_txt").asText(); // ví dụ: "2025-09-16 12:00:00"
                LocalDateTime forecastDateTime = LocalDateTime.parse(
                        forecastTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );

                long diff = Math.abs(Duration.between(targetDateTime, forecastDateTime).toMinutes());
                if (diff < minDiff) {
                    minDiff = diff;
                    bestMatch = item;
                }
            }

            if (bestMatch == null) {
                return "Không tìm thấy dự báo thời tiết gần thời gian bạn yêu cầu.";
            }

            String description = bestMatch.path("weather").get(0).path("description").asText("").toLowerCase();
            double temp = bestMatch.path("main").path("temp").asDouble();

            if (description.contains("mưa") || description.contains("giông")) {
                return String.format("Dự báo lúc %s: %s, %.1f°C. Không nên đặt sân ngoài trời.",
                        startTime, description, temp);
            } else {
                return String.format("Dự báo lúc %s: %s, %.1f°C. Bạn có thể yên tâm đặt sân.",
                        startTime, description, temp);
            }

        } catch (Exception e) {
            log.error("Lỗi khi lấy dự báo thời tiết: ", e);
            return "Không thể lấy dự báo thời tiết.";
        }
    }



    private String checkCourtAvailability(Long courtId, String date, String startTime, String endTime) {
            try {
                String url = UriComponentsBuilder.fromHttpUrl("http://localhost:8080/api/courts/{courtId}/availability")
                        .queryParam("date", date)
                        .queryParam("startTime", startTime)
                        .queryParam("endTime", endTime)
                        .buildAndExpand(courtId)
                        .toUriString();

                log.info("Checking availability: {}", url);
                Boolean available = restTemplate.getForObject(url, Boolean.class);

                if (available == null) {
                    return "Không thể kiểm tra tình trạng sân. Vui lòng thử lại sau.";
                }

                String availabilityMessage = available
                        ? String.format("Sân %d trống vào %s từ %s đến %s", courtId, date, startTime, endTime)
                        : String.format("Xin lỗi, sân %d đã có người đặt vào %s từ %s đến %s",
                        courtId, date, startTime, endTime);
                String weatherAdvice = getWeatherAdvice(date, startTime,"1566083");

                return availabilityMessage + "\n" + weatherAdvice;

            } catch (Exception e) {
                log.error("Lỗi khi gọi API kiểm tra sân: ", e);
                return "Có lỗi xảy ra khi kiểm tra tình trạng sân. Vui lòng thử lại sau.";
            }
    }
}


