package com.example.stockAnalysis.repository;

import com.example.stockAnalysis.model.Stock;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends MongoRepository<Stock, String> {
    Optional<Stock> findBySymbol(String symbol);

    @Query("{'isActive': true}")
    List<Stock> findTop100ByIsActiveTrueOrderByRank();

    List<Stock> findBySymbolIn(List<String> symbols);

    @Query("{'name': {$regex: ?0, $options: 'i'}}")
    List<Stock> findByNameContainingIgnoreCase(String name);

    @Query("{'sector': ?0, 'isActive': true}")
    List<Stock> findBySectorAndIsActiveTrue(String sector);
}

