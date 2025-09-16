package org.badmintonchain.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.badmintonchain.service.impl.ChatService;
import org.badmintonchain.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> ask(@RequestBody Map<String, String> body,
                                                   HttpServletRequest request) {
        String question = body.get("question");
        Object answer = chatService.chat(question);

        ApiResponse<Object> response  = new ApiResponse<>(
                "Success",
                HttpStatus.OK.value(),
                answer,
                request.getRequestURI()
        );
        return ResponseEntity.ok(response );
    }


}
