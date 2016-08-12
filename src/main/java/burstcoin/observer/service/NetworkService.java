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
import burstcoin.observer.event.NetworkMiningInfoUpdateEvent;
import burstcoin.observer.model.MiningInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class NetworkService
{
  private static Log LOG = LogFactory.getLog(NetworkService.class);

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private HttpClient httpClient;

  @Autowired
  private ApplicationEventPublisher publisher;

  @Autowired
  private Timer timer;

  @PostConstruct
  private void postConstruct()
  {
    LOG.info("Started repeating 'check network' task.");
    startCheckNetworkTask();
  }

  private void startCheckNetworkTask()
  {
    timer.schedule(new TimerTask()
    {
      @Override
      public void run()
      {
        Map<String, MiningInfo> miningInfoLookup = new HashMap<>();

        // update compare wallets data
        for(String networkServerUrls : ObserverProperties.getNetworkServerUrls())
        {
          MiningInfo miningInfo = getMiningInfo(networkServerUrls);
          if(miningInfo != null)
          {
            miningInfoLookup.put(networkServerUrls, miningInfo);
          }
          else
          {
            miningInfoLookup.put(networkServerUrls, null);
          }
        }
        publisher.publishEvent(new NetworkMiningInfoUpdateEvent(miningInfoLookup));
      }
    }, ObserverProperties.getNetworkRefreshInterval());


  }

  private MiningInfo getMiningInfo(String server)
  {
    MiningInfo result = null;
    try
    {
      ContentResponse response;
      response = httpClient.newRequest(server + "/burst?requestType=getMiningInfo")
        .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
        .send();

      result = objectMapper.readValue(response.getContentAsString(), MiningInfo.class);
    }
    catch(TimeoutException timeoutException)
    {
      LOG.warn("Unable to get mining info caused by connectionTimeout, currently '" + (ObserverProperties.getConnectionTimeout() / 1000)
               + " sec.' try increasing it!");
    }
    catch(Exception e)
    {
      LOG.trace("Unable to get mining info from wallet (maybe devV2): " + e.getMessage());
    }

    if(result == null)
    {
      try
      {
        // maybe server is dev v2 pool api
        ContentResponse response = httpClient.newRequest(server + "/pool/getMiningInfo")
          .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
          .send();

        result = objectMapper.readValue(response.getContentAsString(), MiningInfo.class);
      }
      catch(TimeoutException timeoutException)
      {
        LOG.warn("Unable to get mining info caused by connectionTimeout, currently '" + (ObserverProperties.getConnectionTimeout() / 1000)
                 + " sec.' try increasing it!");
      }
      catch(Exception e)
      {
        LOG.warn("Unable to get mining info from wallet for '"+server+"': " + e.getMessage());
      }
    }

    return result;
  }
}
