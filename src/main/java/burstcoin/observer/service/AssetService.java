package burstcoin.observer.service;


import burstcoin.observer.ObserverProperties;
import burstcoin.observer.bean.AssetBean;
import burstcoin.observer.event.AssetUpdateEvent;
import burstcoin.observer.service.model.State;
import burstcoin.observer.service.model.asset.Asset;
import burstcoin.observer.service.model.asset.Assets;
import burstcoin.observer.service.model.asset.Order;
import burstcoin.observer.service.model.asset.OrderType;
import burstcoin.observer.service.model.asset.Orders;
import burstcoin.observer.service.model.asset.Trade;
import burstcoin.observer.service.model.asset.Trades;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@Component
public class AssetService
{
  private static Log LOG = LogFactory.getLog(AssetService.class);

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private HttpClient httpClient;

  @Autowired
  private ApplicationEventPublisher publisher;

  private Timer timer = new Timer();

  @PostConstruct
  private void postConstruct()
  {
    LOG.info("Started repeating 'check assets' task.");
    startCheckAssetsTask();
  }

  private void startCheckAssetsTask()
  {
    timer.schedule(new TimerTask()
    {
      @Override
      public void run()
      {
        try
        {
          LOG.info("START import asset data from " + ObserverProperties.getWalletUrl());
          Map<String, Asset> assetLookup = createAssetLookup();
          Map<OrderType, Map<Asset, List<Order>>> orderLookup = createOrderLookup(assetLookup);
          Map<Asset, List<Trade>> tradeLookup = getTradeLookup(assetLookup);

          State state = getState();
          LOG.info("FINISH import asset data!");

          List<AssetBean> assetBeans = new ArrayList<>();
          for(Asset asset : assetLookup.values())
          {
            long volume7Days = 0L;
            long volume30Days = 0L;
            Long lastPrice = null;
            String s = "";
            List<Trade> trades = tradeLookup.get(asset);
            if(trades != null && !trades.isEmpty())
            {
              Iterator<Trade> iterator = trades.iterator();
              boolean withinLast30Days = true;

              while(withinLast30Days && iterator.hasNext())
              {
                Trade trade = iterator.next();
                if(lastPrice == null)
                {
                  lastPrice = Long.valueOf(trade.getPriceNQT());

                  if(lastPrice < 100000000)
                  {
                    s = String.valueOf(100000000 + lastPrice);
                    s = "0." + s.substring(1, s.length());
                  }
                  else
                  {
                    s = String.valueOf(lastPrice);
                    s = s.substring(0, s.length() - 8) + "." + s.substring(s.length() - 8, s.length());
                  }
                  while(s.lastIndexOf("0") == s.length() - 1)
                  {
                    s = s.substring(0, s.length() - 1);
                  }
                  if(s.lastIndexOf(".") == s.length() - 1)
                  {
                    s = s.substring(0, s.length() - 1);
                  }
                }


                Integer bidOrderBlock = Integer.valueOf(trade.getBidOrderHeight());
                Integer askOrderBlock = Integer.valueOf(trade.getAskOrderHeight());
                int block = bidOrderBlock >= askOrderBlock ? bidOrderBlock : askOrderBlock;
                withinLast30Days = state.getNumberOfBlocks() - 360 * 30 < block;

                if(withinLast30Days)
                {

                  long volume = Long.valueOf(trade.getPriceNQT()) * Long.valueOf(trade.getQuantityQNT());
                  volume30Days += volume;

                  if(state.getNumberOfBlocks() - 360 * 7 < block)
                  {
                    volume7Days += volume;
                  }
                }
              }
            }

            List<Order> sellOrders = orderLookup.get(OrderType.ASK).get(asset) != null ? orderLookup.get(OrderType.ASK).get(asset) : new ArrayList<>();
            List<Order> buyOrders = orderLookup.get(OrderType.BID).get(asset) != null ? orderLookup.get(OrderType.BID).get(asset) : new ArrayList<>();

            if(!(buyOrders.isEmpty() && sellOrders.isEmpty() && asset.getNumberOfTrades() < 2))
            {

              assetBeans.add(new AssetBean(asset.getAsset(), asset.getName(), asset.getDescription(), asset.getAccountRS(), asset.getAccount(),
                                           asset.getQuantityQNT(), asset.getDecimals(), asset.getNumberOfAccounts(), asset.getNumberOfTransfers(),
                                           asset.getNumberOfTrades(), buyOrders.size(), sellOrders.size(),
                                           formatAmountNQT(volume7Days, 8), formatAmountNQT(volume30Days, 8) , s));
            }
          }
          Collections.sort(assetBeans, new Comparator<AssetBean>()
          {
            @Override
            public int compare(AssetBean o1, AssetBean o2)
            {
              return Long.valueOf(o2.getVolume30Days()).compareTo(Long.valueOf(o1.getVolume30Days()));
            }
          });
          Collections.sort(assetBeans, new Comparator<AssetBean>()
          {
            @Override
            public int compare(AssetBean o1, AssetBean o2)
            {
              return Long.valueOf(o2.getVolume7Days()).compareTo(Long.valueOf(o1.getVolume7Days()));
            }
          });

          publisher.publishEvent(new AssetUpdateEvent(assetBeans));
        }
        catch(Exception e)
        {
          LOG.error("Failed update assets!");
        }
      }
    }, 200, ObserverProperties.getAssetRefreshInterval());
  }

  private String formatAmountNQT(Long amount, int decimals)
  {
    String amountStr = String.valueOf(amount);
    return amount != null && amountStr.length() >= decimals ? amountStr.substring(0, amountStr.length() - decimals) : "" + amount;
  }

  private Map<String, Asset> createAssetLookup()
  {
    Map<String, Asset> assetLookup = new HashMap<>();
    try
    {
      ContentResponse response = httpClient.newRequest(ObserverProperties.getWalletUrl() + "/burst?requestType=getAllAssets")
        .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
        .send();

      Assets assets = objectMapper.readValue(response.getContentAsString(), Assets.class);
      LOG.info("received '" + assets.getAssets().size() + "' assets in '" + assets.getRequestProcessingTime() + "' ms");
      assetLookup = new HashMap<>();
      for(Asset asset : assets.getAssets())
      {
        assetLookup.put(asset.getAsset(), asset);
      }
    }
    catch(Exception e)
    {
      LOG.warn("Error: Failed to 'getAllAssets': " + e.getMessage());
    }
    return assetLookup;
  }

  private Map<Asset, List<Trade>> getTradeLookup(Map<String, Asset> assetLookup)
  {
    Map<Asset, List<Trade>> tradeLookup = new HashMap<>();
    boolean hasMoreTransactions = true;
    int offset = 0;
    int transactionsPerRequest = 1999;
    while(hasMoreTransactions)
    {
      hasMoreTransactions = updateTradeLookup(tradeLookup, assetLookup, offset, transactionsPerRequest);
      offset += transactionsPerRequest;
    }
    return tradeLookup;
  }

  private boolean updateTradeLookup(Map<Asset, List<Trade>> tradeLookup, Map<String, Asset> assetLookup, int offset, int transactionsPerRequest)
  {
    boolean hasMoreTrades = true;
    try
    {
      InputStreamResponseListener listener = new InputStreamResponseListener();

      Request request = httpClient.newRequest(ObserverProperties.getWalletUrl() + "/burst?requestType=getAllTrades")
        .param("firstIndex", String.valueOf(offset))
        .param("lastIndex", String.valueOf(offset + transactionsPerRequest))
        .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS);
      request.send(listener);

      Response response = listener.get(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS);

      // Look at the response
      if(response.getStatus() == 200)
      {
        // Use try-with-resources to close input stream.
        try (InputStream responseContent = listener.getInputStream())
        {
          Trades trades = objectMapper.readValue(responseContent, Trades.class);
          if(!trades.getTrades().isEmpty() && trades.getTrades().size() < transactionsPerRequest)
          {
            hasMoreTrades = false;
          }

          for(Trade trade : trades.getTrades())
          {
            Asset asset = assetLookup.get(trade.getAsset());
            if(!tradeLookup.containsKey(asset))
            {
              tradeLookup.put(asset, new ArrayList<>());
            }
            tradeLookup.get(asset).add(trade);
          }

          LOG.info("received '" + trades.getTrades().size() + "' trades in '" + trades.getRequestProcessingTime() + "' ms");
        }
        catch(Exception e)
        {
          LOG.error("Failed to receive faucet account transactions.");
        }
      }
    }
    catch(Exception e)
    {
      LOG.warn("Error: Failed to 'getAllTrades': " + e.getMessage());
    }
    return hasMoreTrades;
  }

  private State getState()
  {
    State state = null;
    try
    {
      InputStreamResponseListener listener = new InputStreamResponseListener();

      Request request = httpClient.newRequest(ObserverProperties.getWalletUrl() + "/burst?requestType=getState&includeCounts=true")
        .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS);
      request.send(listener);

      Response response = listener.get(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS);

      // Look at the response
      if(response.getStatus() == 200)
      {
        // Use try-with-resources to close input stream.
        try (InputStream responseContent = listener.getInputStream())
        {
          state = objectMapper.readValue(responseContent, State.class);


//          LOG.info("received '" + trades.getTrades().size() + "' trades in '" + trades.getRequestProcessingTime() + "' ms");
        }
        catch(Exception e)
        {
          LOG.error("Failed to receive faucet account transactions.");
        }
      }
    }
    catch(Exception e)
    {
      LOG.warn("Error: Failed to 'getAllTrades': " + e.getMessage());
    }
    return state;
  }

  private Map<OrderType, Map<Asset, List<Order>>> createOrderLookup(Map<String, Asset> assetLookup)
  {
    Map<OrderType, Map<Asset, List<Order>>> orderLookup = new HashMap<>();
    try
    {
      ContentResponse response = httpClient.newRequest(ObserverProperties.getWalletUrl() + "/burst")
        .param("requestType", "getAllOpenAskOrders")
        .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
        .send();

      Orders askOrders = objectMapper.readValue(response.getContentAsString(), Orders.class);
      LOG.info("received '" + askOrders.getOpenOrders().size() + "' askOrders in '" + askOrders.getRequestProcessingTime() + "' ms");

      addOrders(OrderType.ASK, orderLookup, askOrders, assetLookup);

      response = httpClient.newRequest(ObserverProperties.getWalletUrl() + "/burst")
        .param("requestType", "getAllOpenBidOrders")
        .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
        .send();

      Orders bidOrders = objectMapper.readValue(response.getContentAsString(), Orders.class);
      LOG.info("received '" + bidOrders.getOpenOrders().size() + "' bidOrders in '" + bidOrders.getRequestProcessingTime() + "' ms");

      addOrders(OrderType.BID, orderLookup, bidOrders, assetLookup);
    }
    catch(Exception e)
    {
      LOG.warn("Error: Failed to 'getAllOpenAskOrders' and 'getAllOpenBidOrders': " + e.getMessage());
    }
    return orderLookup;
  }

  private void addOrders(OrderType orderType, Map<OrderType, Map<Asset, List<Order>>> orderLookup, Orders orders, Map<String, Asset> assetLookup)
  {
    Map<Asset, List<Order>> askOrderLookup = new HashMap<>();
    for(Order order : orders.getOpenOrders())
    {
      Asset asset = assetLookup.get(order.getAsset());
      if(!askOrderLookup.containsKey(asset))
      {
        askOrderLookup.put(asset, new ArrayList<>());
      }
      askOrderLookup.get(asset).add(order);
    }
    orderLookup.put(orderType, askOrderLookup);
  }
}
