package com.example.stockAnalysis.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PriceDataPoint {
    private LocalDateTime timestamp;
    private Double price;
    private Long volume;
}
