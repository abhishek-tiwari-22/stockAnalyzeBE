package com.example.stockAnalysis.controller;

import com.example.stockAnalysis.dto.StockResponse;
import com.example.stockAnalysis.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping("/top100")
    public ResponseEntity<List<StockResponse>> getTop100Stocks() {
        List<StockResponse> stocks = stockService.getTop100Stocks();
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<StockResponse> getStockBySymbol(@PathVariable String symbol) {
        StockResponse stock = stockService.getStockBySymbol(symbol.toUpperCase());
        return ResponseEntity.ok(stock);
    }

    @GetMapping("/search")
    public ResponseEntity<List<StockResponse>> searchStocks(@RequestParam String query) {
        List<StockResponse> stocks = stockService.searchStocks(query);
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/sector/{sector}")
    public ResponseEntity<List<StockResponse>> getStocksBySector(@PathVariable String sector) {
        List<StockResponse> stocks = stockService.getStocksBySector(sector);
        return ResponseEntity.ok(stocks);
    }

    @PostMapping("/favorites/{stockSymbol}")
    public ResponseEntity<String> addToFavorites(
            @PathVariable String stockSymbol,
            @RequestParam String userId) {
        stockService.addToFavorites(userId, stockSymbol.toUpperCase());
        return ResponseEntity.ok("Stock added to favorites");
    }

    @DeleteMapping("/favorites/{stockSymbol}")
    public ResponseEntity<String> removeFromFavorites(
            @PathVariable String stockSymbol,
            @RequestParam String userId) {
        stockService.removeFromFavorites(userId, stockSymbol.toUpperCase());
        return ResponseEntity.ok("Stock removed from favorites");
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<StockResponse>> getFavoriteStocks(@RequestParam String userId) {
        List<StockResponse> favoriteStocks = stockService.getFavoriteStocks(userId);
        return ResponseEntity.ok(favoriteStocks);
    }
}