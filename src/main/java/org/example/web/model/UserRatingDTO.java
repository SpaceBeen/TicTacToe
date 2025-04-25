package org.example.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserRatingDTO {
    private UUID userId;
    private double winrate;

   }
