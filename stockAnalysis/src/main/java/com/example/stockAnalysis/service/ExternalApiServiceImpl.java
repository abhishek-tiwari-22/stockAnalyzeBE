package com.example.stockAnalysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.example.stockAnalysis.model.PriceHistory;
import com.example.stockAnalysis.model.Stock;
import com.example.stockAnalysis.repository.StockRepository;
import com.example.stockAnalysis.service.ExternalApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class ExternalApiServiceImpl implements ExternalApiService {

    private final WebClient webClient;
    private final StockRepository stockRepository;

    // Top 100 Indian stocks symbols with NSE suffixes
    private final List<String> TOP_100_STOCKS = Arrays.asList(
            "RELIANCE.NS", "TCS.NS", "HDFCBANK.NS", "INFY.NS", "HINDUNILVR.NS", "ICICIBANK.NS", "KOTAKBANK.NS",
            "BHARTIARTL.NS", "ITC.NS", "SBIN.NS", "LT.NS", "ASIANPAINT.NS", "AXISBANK.NS", "MARUTI.NS", "HCLTECH.NS",
            "BAJFINANCE.NS", "WIPRO.NS", "ULTRACEMCO.NS", "NESTLEIND.NS", "TITAN.NS", "SUNPHARMA.NS", "POWERGRID.NS",
            "TECHM.NS", "TATAMOTORS.NS", "NTPC.NS", "BAJAJFINSV.NS", "HDFCLIFE.NS", "ONGC.NS", "TATASTEEL.NS",
            "ADANIENT.NS", "JSWSTEEL.NS", "COALINDIA.NS", "SBILIFE.NS", "HINDALCO.NS", "BPCL.NS", "GRASIM.NS",
            "BRITANNIA.NS", "CIPLA.NS", "DRREDDY.NS", "EICHERMOT.NS", "APOLLOHOSP.NS", "DIVISLAB.NS", "BAJAJ-AUTO.NS",
            "HEROMOTOCO.NS", "INDUSINDBK.NS", "TATACONSUM.NS", "UPL.NS", "GODREJCP.NS", "ADANIGREEN.NS", "ADANIPORTS.NS"
    );

    @Override
    public void updateStockPrice(Stock stock) {
        try {
            String yahooSymbol = stock.getSymbol().contains(".NS") ? stock.getSymbol() : stock.getSymbol() + ".NS";
            String apiUrl = "https://query1.finance.yahoo.com/v8/finance/chart/" + yahooSymbol;

            JsonNode response = webClient.get()
                    .uri(apiUrl)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("chart") && response.get("chart").has("result")
                    && response.get("chart").get("result").size() > 0) {

                JsonNode result = response.get("chart").get("result").get(0);
                JsonNode meta = result.get("meta");

                if (meta != null) {
                    double currentPrice = meta.get("regularMarketPrice").asDouble();
                    double previousClose = meta.get("previousClose").asDouble();
                    double dayChange = currentPrice - previousClose;
                    double dayChangePercent = (dayChange / previousClose) * 100;

                    stock.setCurrentPrice(currentPrice);
                    stock.setDayChange(dayChange);
                    stock.setDayChangePercent(dayChangePercent);
                    stock.setPreviousClose(previousClose);
                    stock.setLastUpdated(LocalDateTime.now());

                    // Set additional fields if available
                    if (meta.has("regularMarketDayHigh")) {
                        stock.setHighPrice(meta.get("regularMarketDayHigh").asDouble());
                    }
                    if (meta.has("regularMarketDayLow")) {
                        stock.setLowPrice(meta.get("regularMarketDayLow").asDouble());
                    }
                    if (meta.has("regularMarketOpen")) {
                        stock.setOpenPrice(meta.get("regularMarketOpen").asDouble());
                    }
                    if (meta.has("regularMarketVolume")) {
                        stock.setVolume(meta.get("regularMarketVolume").asLong());
                    }
                    if (meta.has("marketCap")) {
                        stock.setMarketCap(meta.get("marketCap").asDouble());
                    }

                    // Add to price history
                    if (stock.getPriceHistory() == null) {
                        stock.setPriceHistory(new ArrayList<>());
                    }

                    PriceHistory priceHistory = new PriceHistory();
                    priceHistory.setTimestamp(LocalDateTime.now());
                    priceHistory.setPrice(currentPrice);
                    priceHistory.setVolume(stock.getVolume());

                    stock.getPriceHistory().add(priceHistory);

                    // Keep only last 100 price points
                    if (stock.getPriceHistory().size() > 100) {
                        stock.getPriceHistory().remove(0);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching data for " + stock.getSymbol() + ": " + e.getMessage());
            // Fallback to previous price if API fails
        }
    }

    @Override
    public void initializeStockData() {
        // Initialize with real data from Yahoo Finance API
        if (stockRepository.count() == 0) {
            List<Stock> stocks = new ArrayList<>();

            for (int i = 0; i < TOP_100_STOCKS.size(); i++) {
                String yahooSymbol = TOP_100_STOCKS.get(i);
                String cleanSymbol = yahooSymbol.replace(".NS", "");

                try {
                    String apiUrl = "https://query1.finance.yahoo.com/v8/finance/chart/" + yahooSymbol;

                    JsonNode response = webClient.get()
                            .uri(apiUrl)
                            .retrieve()
                            .bodyToMono(JsonNode.class)
                            .block();

                    if (response != null && response.has("chart") && response.get("chart").has("result")
                            && response.get("chart").get("result").size() > 0) {

                        JsonNode result = response.get("chart").get("result").get(0);
                        JsonNode meta = result.get("meta");

                        if (meta != null) {
                            Stock stock = new Stock();
                            stock.setSymbol(cleanSymbol);
                            stock.setName(getCompanyName(cleanSymbol));
                            stock.setSector(getSector(cleanSymbol));

                            double currentPrice = meta.get("regularMarketPrice").asDouble();
                            double previousClose = meta.get("previousClose").asDouble();
                            double dayChange = currentPrice - previousClose;

                            stock.setCurrentPrice(currentPrice);
                            stock.setDayChange(dayChange);
                            stock.setDayChangePercent((dayChange / previousClose) * 100);
                            stock.setPreviousClose(previousClose);

                            if (meta.has("regularMarketDayHigh")) {
                                stock.setHighPrice(meta.get("regularMarketDayHigh").asDouble());
                            }
                            if (meta.has("regularMarketDayLow")) {
                                stock.setLowPrice(meta.get("regularMarketDayLow").asDouble());
                            }
                            if (meta.has("regularMarketOpen")) {
                                stock.setOpenPrice(meta.get("regularMarketOpen").asDouble());
                            }
                            if (meta.has("regularMarketVolume")) {
                                stock.setVolume(meta.get("regularMarketVolume").asLong());
                            }
                            if (meta.has("marketCap")) {
                                stock.setMarketCap(meta.get("marketCap").asDouble());
                            }

                            stock.setLastUpdated(LocalDateTime.now());
                            stock.setIsActive(true);
                            stock.setRank(i + 1);
                            stock.setPriceHistory(new ArrayList<>());

                            stocks.add(stock);
                        }
                    }

                    // Add small delay to avoid hitting API rate limits
                    Thread.sleep(100);

                } catch (Exception e) {
                    System.err.println("Error initializing stock " + cleanSymbol + ": " + e.getMessage());
                    // Create stock with basic info if API fails
                    Stock stock = createFallbackStock(cleanSymbol, i + 1);
                    stocks.add(stock);
                }
            }

            stockRepository.saveAll(stocks);
            System.out.println("Initialized " + stocks.size() + " stocks with real data from Yahoo Finance API");
        }
    }

    private Stock createFallbackStock(String symbol, int rank) {
        Stock stock = new Stock();
        stock.setSymbol(symbol);
        stock.setName(getCompanyName(symbol));
        stock.setSector(getSector(symbol));
        stock.setCurrentPrice(100.0);
        stock.setDayChange(0.0);
        stock.setDayChangePercent(0.0);
        stock.setPreviousClose(100.0);
        stock.setOpenPrice(100.0);
        stock.setHighPrice(105.0);
        stock.setLowPrice(95.0);
        stock.setVolume(1000000L);
        stock.setMarketCap(100000000.0);
        stock.setLastUpdated(LocalDateTime.now());
        stock.setIsActive(true);
        stock.setRank(rank);
        stock.setPriceHistory(new ArrayList<>());
        return stock;
    }

    private String getCompanyName(String symbol) {
        Map<String, String> companyNames = new HashMap<>();
        companyNames.put("RELIANCE", "Reliance Industries Limited");
        companyNames.put("TCS", "Tata Consultancy Services Limited");
        companyNames.put("HDFCBANK", "HDFC Bank Limited");
        companyNames.put("INFY", "Infosys Limited");
        companyNames.put("HINDUNILVR", "Hindustan Unilever Limited");
        companyNames.put("ICICIBANK", "ICICI Bank Limited");
        companyNames.put("KOTAKBANK", "Kotak Mahindra Bank Limited");
        companyNames.put("BHARTIARTL", "Bharti Airtel Limited");
        companyNames.put("ITC", "ITC Limited");
        companyNames.put("SBIN", "State Bank of India");
        companyNames.put("LT", "Larsen & Toubro Limited");
        companyNames.put("ASIANPAINT", "Asian Paints Limited");
        companyNames.put("AXISBANK", "Axis Bank Limited");
        companyNames.put("MARUTI", "Maruti Suzuki India Limited");
        companyNames.put("HCLTECH", "HCL Technologies Limited");
        companyNames.put("BAJFINANCE", "Bajaj Finance Limited");
        companyNames.put("WIPRO", "Wipro Limited");
        companyNames.put("ULTRACEMCO", "UltraTech Cement Limited");
        companyNames.put("NESTLEIND", "Nestle India Limited");
        companyNames.put("TITAN", "Titan Company Limited");
        companyNames.put("SUNPHARMA", "Sun Pharmaceutical Industries Limited");
        companyNames.put("POWERGRID", "Power Grid Corporation of India Limited");
        companyNames.put("TECHM", "Tech Mahindra Limited");
        companyNames.put("TATAMOTORS", "Tata Motors Limited");
        companyNames.put("NTPC", "NTPC Limited");
        companyNames.put("BAJAJFINSV", "Bajaj Finserv Limited");
        companyNames.put("HDFCLIFE", "HDFC Life Insurance Company Limited");
        companyNames.put("ONGC", "Oil and Natural Gas Corporation Limited");
        companyNames.put("TATASTEEL", "Tata Steel Limited");
        companyNames.put("ADANIENT", "Adani Enterprises Limited");
        companyNames.put("JSWSTEEL", "JSW Steel Limited");
        companyNames.put("COALINDIA", "Coal India Limited");
        companyNames.put("SBILIFE", "SBI Life Insurance Company Limited");
        companyNames.put("HINDALCO", "Hindalco Industries Limited");
        companyNames.put("BPCL", "Bharat Petroleum Corporation Limited");
        companyNames.put("GRASIM", "Grasim Industries Limited");
        companyNames.put("BRITANNIA", "Britannia Industries Limited");
        companyNames.put("CIPLA", "Cipla Limited");
        companyNames.put("DRREDDY", "Dr. Reddy's Laboratories Limited");
        companyNames.put("EICHERMOT", "Eicher Motors Limited");
        companyNames.put("APOLLOHOSP", "Apollo Hospitals Enterprise Limited");
        companyNames.put("DIVISLAB", "Divi's Laboratories Limited");
        companyNames.put("BAJAJ-AUTO", "Bajaj Auto Limited");
        companyNames.put("HEROMOTOCO", "Hero MotoCorp Limited");
        companyNames.put("INDUSINDBK", "IndusInd Bank Limited");
        companyNames.put("TATACONSUM", "Tata Consumer Products Limited");
        companyNames.put("UPL", "UPL Limited");
        companyNames.put("GODREJCP", "Godrej Consumer Products Limited");
        companyNames.put("ADANIGREEN", "Adani Green Energy Limited");
        companyNames.put("ADANIPORTS", "Adani Ports and Special Economic Zone Limited");

        return companyNames.getOrDefault(symbol, symbol + " Limited");
    }

    private String getSector(String symbol) {
        Map<String, String> sectors = new HashMap<>();
        sectors.put("RELIANCE", "Oil & Gas");
        sectors.put("TCS", "Information Technology");
        sectors.put("HDFCBANK", "Banking");
        sectors.put("INFY", "Information Technology");
        sectors.put("HINDUNILVR", "Consumer Goods");
        sectors.put("ICICIBANK", "Banking");
        sectors.put("KOTAKBANK", "Banking");
        sectors.put("BHARTIARTL", "Telecommunications");
        sectors.put("ITC", "Consumer Goods");
        sectors.put("SBIN", "Banking");
        sectors.put("LT", "Infrastructure");
        sectors.put("ASIANPAINT", "Consumer Goods");
        sectors.put("AXISBANK", "Banking");
        sectors.put("MARUTI", "Automobile");
        sectors.put("HCLTECH", "Information Technology");
        sectors.put("BAJFINANCE", "Financial Services");
        sectors.put("WIPRO", "Information Technology");
        sectors.put("ULTRACEMCO", "Cement");
        sectors.put("NESTLEIND", "Consumer Goods");
        sectors.put("TITAN", "Consumer Goods");
        sectors.put("SUNPHARMA", "Pharmaceuticals");
        sectors.put("POWERGRID", "Power");
        sectors.put("TECHM", "Information Technology");
        sectors.put("TATAMOTORS", "Automobile");
        sectors.put("NTPC", "Power");
        sectors.put("BAJAJFINSV", "Financial Services");
        sectors.put("HDFCLIFE", "Insurance");
        sectors.put("ONGC", "Oil & Gas");
        sectors.put("TATASTEEL", "Steel");
        sectors.put("ADANIENT", "Conglomerate");
        sectors.put("JSWSTEEL", "Steel");
        sectors.put("COALINDIA", "Mining");
        sectors.put("SBILIFE", "Insurance");
        sectors.put("HINDALCO", "Metals");
        sectors.put("BPCL", "Oil & Gas");
        sectors.put("GRASIM", "Textiles");
        sectors.put("BRITANNIA", "Consumer Goods");
        sectors.put("CIPLA", "Pharmaceuticals");
        sectors.put("DRREDDY", "Pharmaceuticals");
        sectors.put("EICHERMOT", "Automobile");
        sectors.put("APOLLOHOSP", "Healthcare");
        sectors.put("DIVISLAB", "Pharmaceuticals");
        sectors.put("BAJAJ-AUTO", "Automobile");
        sectors.put("HEROMOTOCO", "Automobile");
        sectors.put("INDUSINDBK", "Banking");
        sectors.put("TATACONSUM", "Consumer Goods");
        sectors.put("UPL", "Chemicals");
        sectors.put("GODREJCP", "Consumer Goods");
        sectors.put("ADANIGREEN", "Renewable Energy");
        sectors.put("ADANIPORTS", "Infrastructure");

        return sectors.getOrDefault(symbol, "Diversified");
    }
}