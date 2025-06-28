package com.example.stockAnalysis.service;

import com.example.stockAnalysis.dto.StockResponse;
import com.example.stockAnalysis.model.Stock;

import java.util.List;

public interface StockService {
    List<StockResponse> getTop100Stocks();
    StockResponse getStockBySymbol(String symbol);
    List<StockResponse> searchStocks(String query);
    List<StockResponse> getStocksBySector(String sector);
    void updateStockPrices();
    void addToFavorites(String userId, String stockSymbol);
    void removeFromFavorites(String userId, String stockSymbol);
    List<StockResponse> getFavoriteStocks(String userId);
}
