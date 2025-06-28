package com.example.stockAnalysis.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PriceHistory {
    private LocalDateTime timestamp;
    private Double price;
    private Long volume;
}
