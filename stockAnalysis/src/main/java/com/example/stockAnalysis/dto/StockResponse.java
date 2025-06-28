package com.example.stockAnalysis.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StockResponse {
    private String symbol;
    private String name;
    private String sector;
    private Double currentPrice;
    private Double dayChange;
    private Double dayChangePercent;
    private Double openPrice;
    private Double highPrice;
    private Double lowPrice;
    private Double previousClose;
    private Long volume;
    private Double marketCap;
    private LocalDateTime lastUpdated;
    private List<PriceDataPoint> priceHistory;
    private Integer rank;
}
