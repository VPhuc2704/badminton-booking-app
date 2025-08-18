package org.badmintonchain.model.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenDTO {
        private String type;
        private String accessToken;
        private String refreshToken;
}
