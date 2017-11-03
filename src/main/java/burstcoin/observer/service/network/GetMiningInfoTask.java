/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 by luxe - https://github.com/de-luxe - BURST-LUXE-RED2-G6JW-H4HG5
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

package burstcoin.observer.service.network;

import burstcoin.observer.ObserverProperties;
import burstcoin.observer.service.model.MiningInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Scope("prototype")
public class GetMiningInfoTask
  implements Runnable
{
  private static final Logger LOG = LoggerFactory.getLogger(GetMiningInfoTask.class);

  @Autowired
  @Qualifier("ProxyHttpClient")
  private HttpClient httpClient;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ApplicationEventPublisher publisher;

  private String networkServerUrl;



  public GetMiningInfoTask(String networkServerUrl)
  {
    this.networkServerUrl = networkServerUrl;
  }

  @Override
  public void run()
  {
    MiningInfo result = null;
    try
    {
      ContentResponse response;

      response = httpClient.newRequest(networkServerUrl + "/burst?requestType=getMiningInfo")
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
      LOG.warn("Unable to get mining info from '"+networkServerUrl+"' ... " + e.getMessage());
    }

    publisher.publishEvent(new MiningInfoEvent(result, networkServerUrl, UUID.randomUUID()));
  }
}
