package receivers;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.event.DepthEvent;
import com.binance.api.client.domain.market.TickerStatistics;

import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class BinanceReceiver {


    private static BinanceApiWebSocketClient webSocketClient;
    private static BinanceApiRestClient apiRestClient;
    private static BinanceReceiver INSTANCE;
    private static String apiKeyTest = "fZau1JSiwe2IHTOyMWWBn85NNRJr0QOeEdSBOcImRCTZD1VEoqiTnKIM2gdLolDL";
    private static String apiSecretTest = "uTuHpM9L0gRHi7o64CbeXGFAFDnFA2cnMBV29f1tTxRO7eLkKQYWAhSSk7rApoDG";
    private String apikey;
    private String apiSecret;
    private String identity;
    private static List<String> apiKeys = Arrays.asList("fZau1JSiwe2IHTOyMWWBn85NNRJr0QOeEdSBOcImRCTZD1VEoqiTnKIM2gdLolDL",
            "H12KXTJfRYp03Lp2RGBBSumg7jjI5YCHcJcGcN9b4f1GMCJqX9tQg7eo2HIA25Sr");
    private static List<String> apiSecrets = Arrays.asList("uTuHpM9L0gRHi7o64CbeXGFAFDnFA2cnMBV29f1tTxRO7eLkKQYWAhSSk7rApoDG",
            "4RsMJtYrAaHtGvVX6unyDbRk7G1HQP47L8MBzzZL8RUsjgy6GlI6pa0vH26rR5rm");

    private List<BigDecimal> prices = new ArrayList<>();
    private BigDecimal price;
    private String timeOfEvent;
    private static Properties properties = new Properties();
    private static List<String> pairs;



    public static BinanceReceiver getInstance() {
        initClients();
        loadProps();
        initProps();
        if (INSTANCE == null)
            synchronized (BinanceReceiver.class) {
                INSTANCE = new BinanceReceiver();
            }
        return INSTANCE;
    }

    private static void loadProps(){
        InputStream inputStream = BinanceReceiver.class.getClassLoader().getResourceAsStream("binance.properties");
        try{
            properties.load(inputStream);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
    public static void initClients() {
        apiRestClient = BinanceApiClientFactory.newInstance(apiKeyTest, apiSecretTest).newRestClient();
        webSocketClient = BinanceApiClientFactory.newInstance(apiKeyTest, apiSecretTest).newWebSocketClient();
    }

    public static void initProps(){
        pairs = Arrays.asList(properties.getProperty("binance.pairs").split(","));
    }

    public BinanceReceiver(){};

    public BinanceReceiver(String key, String secret) {
        this.apikey = key;
        this.apiSecret = secret;
        initClients(key, secret);
    }

    public static void initClients(String apikey, String apiSecret) {
        apiRestClient = BinanceApiClientFactory.newInstance(apikey, apiSecret).newRestClient();
        webSocketClient = BinanceApiClientFactory.newInstance(apikey, apiSecret).newWebSocketClient();
    }


    //The Aggregate Trade Streams push trade information that is aggregated for a single taker order.
    public void getAggregatedTradeStream(String pair) {
        webSocketClient.onAggTradeEvent(pair,
                response -> {
                    timeOfEvent = millisToHour(response.getEventTime());
                    price = new BigDecimal(response.getPrice());
                    prices.add(price);
                    getPrice(response.getSymbol());
                });
    }


    public void getPrice(String pair) {
        System.out.println(String.format("Price for pair %s at %s is : %.8f ", pair, timeOfEvent, price));
    }

    public void get24HrPriceStatistics(String currencyPair) {
        TickerStatistics tickerStatistics = apiRestClient.get24HrPriceStatistics(currencyPair);
        System.out.println(String.format("Highest price : %s ", tickerStatistics.getHighPrice()));
        System.out.println(String.format("Lowest price for last 24 hours : %s ", tickerStatistics.getLowPrice()));
        System.out.println(String.format("%s", tickerStatistics.getWeightedAvgPrice()));
    }

    public String millisToHour(long milliseconds) {
        Instant instant = Instant.ofEpochMilli(milliseconds);
        return instant.toString();
    }


    public static void main(String[] args) {

        BinanceReceiver binanceReceiver = BinanceReceiver.getInstance();
//        List<String> pairs = Arrays.asList("trxeth", "eoseth");
        for(int i=0; i < pairs.size(); i++)
            binanceReceiver.getAggregatedTradeStream(pairs.get(i));


//        binanceReceiver.getAggregatedTradeStream("ethbtc");

//        BinanceApiWebSocketClient client = BinanceApiClientFactory.newInstance(apikey, apiSecret).newWebSocketClient();
//        BinanceReceiver binanceReceiver = null;
//        for (int i = 0; i < apiKeys.size(); i++) {
//            binanceReceiver = new BinanceReceiver(apiKeys.get(i), apiSecrets.get(i));
//            binanceReceiver.getAggregatedTradeStream(pairs.get(i));
//        }

//
//        List<String> samples = new ArrayList<>();
//        client.onAggTradeEvent("ethusdt", response
//                -> System.out.println(response.getPrice()));


//
//        // Listen for changes in the order book in ETH/BTC
//        client.onDepthEvent("ethusdt", response -> System.out.println(response));
//
//        // Obtain 1m candlesticks in real-time for ETH/BTC
//        client.onCandlestickEvent("ethbtc", CandlestickInterval.ONE_MINUTE, response -> System.out.println(response));

//        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(apikey,apiSecret);
//        BinanceApiRestClient client = factory.newRestClient();
//        TickerStatistics tickerStatistics =  client.get24HrPriceStatistics("ETHBTC");
////        System.out.println(tickerStatistics.toString());
//        List<Candlestick> candlesticks = client.getCandlestickBars("ETHBTC", CandlestickInterval.TWELVE_HOURLY).;
//        System.out.println(candlesticks);
    }

}
