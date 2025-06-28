package com.example.stockAnalysis.config;

import com.example.stockAnalysis.service.ExternalApiService;
import com.example.stockAnalysis.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Configuration
@RequiredArgsConstructor
public class SchedulingConfig {

    private final ExternalApiService externalApiService;

    @Bean
    public ApplicationRunner initializeData() {
        return args -> {
            // Initialize stock data on startup
            externalApiService.initializeStockData();
        };
    }

    @Component
    @RequiredArgsConstructor
    public static class StockPriceUpdater {

        private final StockService stockService;

        // Update stock prices every 5 minutes during market hours
        @Scheduled(fixedRate = 300000) // 5 minutes
        public void updateStockPrices() {
            stockService.updateStockPrices();
        }
    }
}