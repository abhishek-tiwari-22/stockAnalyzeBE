package com.example.stockAnalysis.service;

import com.example.stockAnalysis.model.Stock;

public interface ExternalApiService {
    void updateStockPrice(Stock stock);
    void initializeStockData();
}
