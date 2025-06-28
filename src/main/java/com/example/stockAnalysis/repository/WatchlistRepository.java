package com.example.stockAnalysis.repository;

import com.example.stockAnalysis.model.Watchlist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchlistRepository extends MongoRepository<Watchlist, String> {
    List<Watchlist> findByUserId(String userId);
}
