package org.example.web.model.request;

import lombok.Data;

@Data
public class RefreshJwtRequest {
    private String refreshToken;
}
