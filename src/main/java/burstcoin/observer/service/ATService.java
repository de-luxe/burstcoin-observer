/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 by luxe - https://github.com/de-luxe - BURST-LUXE-RED2-G6JW-H4HG5
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package burstcoin.observer.service;

import burstcoin.observer.ObserverProperties;
import burstcoin.observer.controller.CrowdfundController;
import burstcoin.observer.event.ATDataUpdateEvent;
import burstcoin.observer.model.State;
import burstcoin.observer.model.at.AutomatedTransaction;
import burstcoin.observer.model.at.AutomatedTransactionIds;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@Component
public class ATService
{
  private static Log LOG = LogFactory.getLog(ATService.class);

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private HttpClient httpClient;

  @Autowired
  private ApplicationEventPublisher publisher;

  @Autowired
  private ApplicationContext context;

  private Timer timer = new Timer();

  @PostConstruct
  private void postConstruct()
  {
    LOG.info("Started repeating 'check at' task.");
    startCheckAutomatedTransactionsTask();
  }

  private void startCheckAutomatedTransactionsTask()
  {
    timer.schedule(new TimerTask()
    {
      @Override
      public void run()
      {
        LOG.info("START import at data");
        Map<String, AutomatedTransaction> atLookup = new HashMap<>();
        for(String atId : getAutomatedTransactionIds())
        {
          AutomatedTransaction automatedTransaction = getAutomatedTransaction(atId);
          atLookup.put(atId, automatedTransaction);
        }
        State state = getState();
        LOG.info("FINISH import at data!");

        // todo no idea why this currently does not work
        // publisher.publishEvent(new ATDataUpdateEvent(state, atLookup));
        // workaround:
        context.getBean(CrowdfundController.class).handleMessage(new ATDataUpdateEvent(state, atLookup));
      }
    }, 200, ObserverProperties.getCrowdfundRefreshInterval());
  }

  private List<String> getAutomatedTransactionIds()
  {
    List<String> atIds = null;
    try
    {
      ContentResponse response = httpClient.newRequest(ObserverProperties.getWalletUrl() + "/burst?requestType=getATIds")
        .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
        .send();

      AutomatedTransactionIds automatedTransactionIds = objectMapper.readValue(response.getContentAsString(), AutomatedTransactionIds.class);
//      LOG.info("received '" + automatedTransactionIds.getAtIds().size() + "' at's in '" + automatedTransactionIds.getRequestProcessingTime() + "' ms");

      atIds = automatedTransactionIds.getAtIds();
    }
    catch(Exception e)
    {
      LOG.warn("Error: Failed to 'getATIds': " + e.getMessage());
    }
    return atIds;
  }

  private AutomatedTransaction getAutomatedTransaction(String atId)
  {
    AutomatedTransaction automatedTransaction = null;
    try
    {
      ContentResponse response = httpClient.POST(ObserverProperties.getWalletUrl() + "/burst")
        .param("requestType", "getAT")
        .param("at", atId)
        .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
        .send();

      automatedTransaction = objectMapper.readValue(response.getContentAsString(), AutomatedTransaction.class);
//      LOG.info("received '" + automatedTransaction.getName() + "' ats in '" + automatedTransaction.getRequestProcessingTime() + "' ms");
    }
    catch(Exception e)
    {
      LOG.warn("Error: Failed to 'getAT': " + e.getMessage());
    }
    return automatedTransaction;
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
}
