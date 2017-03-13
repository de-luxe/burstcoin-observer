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
      LOG.trace("Unable to get mining info from wallet (maybe devV2): " + e.getMessage());
    }

    if(result == null)
    {
      try
      {
        // maybe server is dev v2 pool api
        ContentResponse response = httpClient.newRequest(networkServerUrl + "/pool/getMiningInfo")
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
        LOG.info("Unable to get mining info from wallet for '" + networkServerUrl + "'");

      }
    }
    publisher.publishEvent(new MiningInfoEvent(result, networkServerUrl, UUID.randomUUID()));
  }
}
