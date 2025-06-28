package com.example.stockAnalysis.service;

import com.example.stockAnalysis.dto.PriceDataPoint;
import com.example.stockAnalysis.dto.StockResponse;
import com.example.stockAnalysis.model.PriceHistory;
import com.example.stockAnalysis.model.Stock;
import com.example.stockAnalysis.model.User;
import com.example.stockAnalysis.repository.StockRepository;
import com.example.stockAnalysis.repository.UserRepository;
import com.example.stockAnalysis.service.StockService;
import com.example.stockAnalysis.service.ExternalApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final ExternalApiService externalApiService;

    @Override
    public List<StockResponse> getTop100Stocks() {
        List<Stock> stocks = stockRepository.findTop100ByIsActiveTrueOrderByRank();
        return stocks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public StockResponse getStockBySymbol(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + symbol));
        return convertToResponse(stock);
    }

    @Override
    public List<StockResponse> searchStocks(String query) {
        List<Stock> stocks = stockRepository.findByNameContainingIgnoreCase(query);
        return stocks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockResponse> getStocksBySector(String sector) {
        List<Stock> stocks = stockRepository.findBySectorAndIsActiveTrue(sector);
        return stocks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void updateStockPrices() {
        // This method would call external API to update stock prices
        // Implementation depends on the chosen API
        List<Stock> stocks = stockRepository.findAll();
        for (Stock stock : stocks) {
            try {
                // Call external API service to get updated price
                externalApiService.updateStockPrice(stock);
                stockRepository.save(stock);
            } catch (Exception e) {
                // Log error and continue with next stock
                System.err.println("Error updating stock: " + stock.getSymbol() + " - " + e.getMessage());
            }
        }
    }

    @Override
    public void addToFavorites(String userId, String stockSymbol) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getFavoriteStocks() == null) {
            user.setFavoriteStocks(new HashSet<>());
        }

        user.getFavoriteStocks().add(stockSymbol);
        userRepository.save(user);
    }

    @Override
    public void removeFromFavorites(String userId, String stockSymbol) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getFavoriteStocks() != null) {
            user.getFavoriteStocks().remove(stockSymbol);
            userRepository.save(user);
        }
    }

    @Override
    public List<StockResponse> getFavoriteStocks(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getFavoriteStocks() == null || user.getFavoriteStocks().isEmpty()) {
            return new ArrayList<>();
        }

        List<Stock> favoriteStocks = stockRepository.findBySymbolIn(
                new ArrayList<>(user.getFavoriteStocks())
        );

        return favoriteStocks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private StockResponse convertToResponse(Stock stock) {
        StockResponse response = new StockResponse();
        response.setSymbol(stock.getSymbol());
        response.setName(stock.getName());
        response.setSector(stock.getSector());
        response.setCurrentPrice(stock.getCurrentPrice());
        response.setDayChange(stock.getDayChange());
        response.setDayChangePercent(stock.getDayChangePercent());
        response.setOpenPrice(stock.getOpenPrice());
        response.setHighPrice(stock.getHighPrice());
        response.setLowPrice(stock.getLowPrice());
        response.setPreviousClose(stock.getPreviousClose());
        response.setVolume(stock.getVolume());
        response.setMarketCap(stock.getMarketCap());
        response.setLastUpdated(stock.getLastUpdated());
        response.setRank(stock.getRank());

        if (stock.getPriceHistory() != null) {
            List<PriceDataPoint> priceHistory = stock.getPriceHistory().stream()
                    .map(this::convertToPriceDataPoint)
                    .collect(Collectors.toList());
            response.setPriceHistory(priceHistory);
        }

        return response;
    }

    private PriceDataPoint convertToPriceDataPoint(PriceHistory priceHistory) {
        PriceDataPoint dataPoint = new PriceDataPoint();
        dataPoint.setTimestamp(priceHistory.getTimestamp());
        dataPoint.setPrice(priceHistory.getPrice());
        dataPoint.setVolume(priceHistory.getVolume());
        return dataPoint;
    }
}
