package com.example.stockAnalysis.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "watchlists")
public class Watchlist {
    @Id
    private String id;

    private String userId;
    private String name;
    private List<String> stockSymbols;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
