package org.example.web.model.response;

import lombok.Data;

@Data
public class JwtResponse {
    private String type = "Bearer";
    private String accessToken;
    private String refreshToken;

    public JwtResponse(String type, String accessToken, String refreshToken) {
        this.type = type;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
