package burstcoin.observer.service;


import burstcoin.observer.ObserverProperties;
import burstcoin.observer.event.AssetInfoUpdateEvent;
import burstcoin.observer.model.State;
import burstcoin.observer.model.asset.Asset;
import burstcoin.observer.model.asset.Assets;
import burstcoin.observer.model.asset.Order;
import burstcoin.observer.model.asset.OrderType;
import burstcoin.observer.model.asset.Orders;
import burstcoin.observer.model.asset.Trade;
import burstcoin.observer.model.asset.Trades;
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
import java.util.HashMap;
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
        LOG.info("START import asset data from " + ObserverProperties.getWalletUrl());
        Map<String, Asset> assetLookup = createAssetLookup();
        Map<OrderType, Map<Asset, List<Order>>> orderLookup = createOrderLookup(assetLookup);
        Map<Asset, List<Trade>> tradeLookup = createTradeLookup(assetLookup);

        State state = getState();
        LOG.info("FINISH import asset data!");


        publisher.publishEvent(new AssetInfoUpdateEvent(state, assetLookup, orderLookup, tradeLookup));

      }
    }, 2000, 1000 * 60 * 10 /* every 10 min. */);
  }

  private Map<String, Asset> createAssetLookup()
  {
    Map<String, Asset> assetLookup = null;
    try
    {
      ContentResponse response = httpClient.POST(ObserverProperties.getWalletUrl() + "/burst")
        .param("requestType", "getAllAssets")
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

  private Map<Asset, List<Trade>> createTradeLookup(Map<String, Asset> assetLookup)
  {
    Map<Asset, List<Trade>> tradeLookup = new HashMap<>();
    try
    {
      InputStreamResponseListener listener = new InputStreamResponseListener();

      Request request = httpClient.POST(ObserverProperties.getWalletUrl() + "/burst")
        .param("requestType", "getAllTrades")
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
    return tradeLookup;
  }


  private State getState()
  {
    State state = null;
    try
    {
      InputStreamResponseListener listener = new InputStreamResponseListener();

      Request request = httpClient.POST(ObserverProperties.getWalletUrl() + "/burst")
        .param("requestType", "getState")
        .param("includeCounts", "true")
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
      ContentResponse response = httpClient.POST(ObserverProperties.getWalletUrl() + "/burst")
        .param("requestType", "getAllOpenAskOrders")
        .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
        .send();

      Orders askOrders = objectMapper.readValue(response.getContentAsString(), Orders.class);
      LOG.info("received '" + askOrders.getOpenOrders().size() + "' askOrders in '" + askOrders.getRequestProcessingTime() + "' ms");

      addOrders(OrderType.ASK, orderLookup, askOrders, assetLookup);

      response = httpClient.POST(ObserverProperties.getWalletUrl() + "/burst")
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
