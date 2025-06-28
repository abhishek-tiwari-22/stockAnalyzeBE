package com.example.stockAnalysis.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "stocks")
public class Stock {
    @Id
    private String id;

    @Indexed(unique = true)
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
    private List<PriceHistory> priceHistory;
    private Boolean isActive;
    private Integer rank;
}