package receivers;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.event.DepthEvent;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerStatistics;
import utils.TimeUtils;

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
    private static String apiKey;
    private static String apiSecret;
    private BigDecimal price;
    private String timeOfEvent;
    private static Properties properties = new Properties();
    private static List<String> pairs;

    public BinanceReceiver() {
    }

    public static BinanceReceiver getInstance() {
        loadProps();
        initProps();
        initClients();
        if (INSTANCE == null)
            synchronized (BinanceReceiver.class) {
                INSTANCE = new BinanceReceiver();
            }
        return INSTANCE;
    }

    private static void loadProps() {
        InputStream inputStream = BinanceReceiver.class.getClassLoader().getResourceAsStream("binance.properties");
        try {
            properties.load(inputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void initClients() {
        apiRestClient = BinanceApiClientFactory.newInstance(apiKey, apiSecret).newRestClient();
        webSocketClient = BinanceApiClientFactory.newInstance(apiKey, apiSecret).newWebSocketClient();
    }

    private static void initProps() {
        pairs = Arrays.asList(properties.getProperty("binance.pairs").split(","));
        apiKey = properties.getProperty("binance.api_key");
        apiSecret = properties.getProperty("binance.api_secret");
    }


    //The Aggregate Trade Streams push trade information that is aggregated for a single taker order.
    public void getAggregatedTradeStream(String pair) {
        webSocketClient.onAggTradeEvent(pair,
                response -> {
                    timeOfEvent = TimeUtils.millisToHour(response.getEventTime());
                    price = new BigDecimal(response.getPrice());
                    getPrice(response.getSymbol());
                });
    }

    // get streaming candlesticks data for particular pair
    // possible values for interval are - ONE_MINUTE, THREE_MINUTES, FIVE_MINUTES ,
    // FIFTEEN_MINUTES, HALF_HOURLY, HOURLY, TWO_HOURLY, FOUR_HOURLY, SIX_HOURLY,
    // EIGHT_HOURLY, TWELVE_HOURLY, DAILY, THREE_DAILY, WEEKLY, MONTHLY
    public void getCandleSticks(String pair, String interval) {
        webSocketClient.onCandlestickEvent(pair, CandlestickInterval.valueOf(interval),
                response -> System.out.println(String.format("The highest candlestick for the desire interval %s : %s ",
                        interval,response.getHigh())));
    }

    public void getPrice(String pair) {
        System.out.println(String.format("Price for pair %s at %s is : %.8f ", pair, timeOfEvent, price));
    }

    // get statistics for lowest and highest price per currency pair for last 24 hours
    public void get24HrPriceStatistics(String currencyPair) {
        TickerStatistics tickerStatistics = apiRestClient.get24HrPriceStatistics(currencyPair.toUpperCase());
        System.out.println(String.format("Highest price for last 24 hours for %s : %s ",
                currencyPair, tickerStatistics.getHighPrice()));
        System.out.println(String.format("Lowest price for last 24 hours for %s : %s ",
                currencyPair, tickerStatistics.getLowPrice()));
        System.out.println(String.format("Average price for last 24 hours for %s : %s",
                currencyPair,tickerStatistics.getWeightedAvgPrice()));
    }


    public static void main(String[] args) {

//        BinanceReceiver binanceReceiver = BinanceReceiver.getInstance();
//        for (int i = 0; i < pairs.size(); i++)
//            binanceReceiver.getCandleSticks(pairs.get(i),"DAILY");


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
