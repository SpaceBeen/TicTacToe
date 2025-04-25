package org.example.web.model.request;

import lombok.Data;

@Data
public class JwtRequest {
    private String login;
    private String password;
}
