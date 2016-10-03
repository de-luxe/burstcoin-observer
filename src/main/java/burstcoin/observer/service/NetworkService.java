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
import burstcoin.observer.event.NetworkUpdateEvent;
import burstcoin.observer.service.model.MiningInfo;
import burstcoin.observer.bean.NetworkBean;
import burstcoin.observer.bean.NetworkState;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
  private JavaMailSender mailSender;

  private Timer timer = new Timer();
  // blockHeight -> genSig -> domains
  private Map<Long, Map<String, Set<String>>> genSigLookup = new HashMap<>();
  private Long lastBlockWithSameGenSig;
  private boolean forkMailSend = false;
  private Long lastBlockWithSameGenSigMailSend = 0L;
  private Map<String, NetworkState> previousStateLookup = new HashMap<>();

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
        try
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

          List<NetworkBean> networkBeans = new ArrayList<>();
          for(Map.Entry<String, MiningInfo> entry : miningInfoLookup.entrySet())
          {
            MiningInfo miningInfo = entry.getValue();

            String domain = entry.getKey().replace("http://", "").replace("https://", "");
            if(miningInfo != null && miningInfo.getGenerationSignature() != null)
            {
              networkBeans.add(new NetworkBean(String.valueOf(miningInfo.getHeight()), domain, entry.getKey(), miningInfo.getBaseTarget(),
                                               miningInfo.getGenerationSignature().substring(0, 25) + "...",
                                               String.valueOf(miningInfo.getTargetDeadline())));
            }
            else
            {
              networkBeans.add(new NetworkBean(domain));
            }
          }
          Collections.sort(networkBeans, new Comparator<NetworkBean>()
          {
            @Override
            public int compare(NetworkBean o1, NetworkBean o2)
            {
              return o1.getBaseTarget().compareTo(o2.getBaseTarget());
            }
          });
          Collections.sort(networkBeans, new Comparator<NetworkBean>()
          {
            @Override
            public int compare(NetworkBean o1, NetworkBean o2)
            {
              return o2.getHeight().compareTo(o1.getHeight());
            }
          });

          // update genSig Lookup
          int numberOfNotAvailableDomains = 0;
          for(NetworkBean networkBean : networkBeans)
          {
            if(networkBean.getAvailable())
            {
              Long height = Long.valueOf(networkBean.getHeight());
              if(!genSigLookup.containsKey(height))
              {
                genSigLookup.put(height, new HashMap<>());
              }
              Map<String, Set<String>> subMap = genSigLookup.get(height);
              if(!subMap.containsKey(networkBean.getGenerationSignature()))
              {
                subMap.put(networkBean.getGenerationSignature(), new HashSet<>());
              }
              Set<String> domains = subMap.get(networkBean.getGenerationSignature());
              domains.add(networkBean.getDomain());
            }
            else
            {
              // N/A
              numberOfNotAvailableDomains++;
            }
          }

          List<Long> order = new ArrayList<>(genSigLookup.keySet());
          Collections.sort(order);
          Collections.reverse(order);

          Iterator<Long> iterator = order.iterator();
          lastBlockWithSameGenSig = null;
          while(iterator.hasNext() && lastBlockWithSameGenSig == null)
          {
            Long nextHeight = iterator.next();
            if(genSigLookup.get(nextHeight).size() == 1) // only one known genSig for height
            {
              // number of domains with same genSig = all domains without N/A
              if(networkBeans.size() - numberOfNotAvailableDomains == genSigLookup.get(nextHeight).values().iterator().next().size())
              {
                lastBlockWithSameGenSig = nextHeight;
              }
            }
          }
          
          long maxHeight = 0;
          
          for(NetworkBean networkBean : networkBeans)
          {
            if(networkBean.getAvailable())
            {
              maxHeight = Math.max(Long.valueOf(networkBean.getHeight()), maxHeight);
            }
          }

          boolean appStartedAfterForkHappened = lastBlockWithSameGenSig == null;

          boolean sendMail = false;
          
          for(NetworkBean networkBean : networkBeans)
          {
            if(networkBean.getAvailable())
            {
              Long height = Long.valueOf(networkBean.getHeight());
              Set<String> domainsWithSameGenSigForBlock = genSigLookup.get(height).get(networkBean.getGenerationSignature());
              if(genSigLookup.get(height).size() > 1 && domainsWithSameGenSigForBlock.size() < (networkBeans.size() - numberOfNotAvailableDomains) / 2)
              {
                networkBean.setState(NetworkState.FORKED);
                sendMail = true;
              }
                            
              if (height + 4 < maxHeight) // when the wallet is 4 blocks behind -> stuck
              {
            	if (!networkBean.getState().equals(NetworkState.FORKED)) // if it's forked, then ignore the stuck-check, because forks may also be behind
            	{
  				  networkBean.setState(NetworkState.STUCK);
				
				  if (ObserverProperties.isEnableStuckNotify() &&
					  (!previousStateLookup.containsKey(networkBean.getDomain()) || !previousStateLookup.get(networkBean.getDomain()).equals(NetworkState.STUCK)) //send only once
				     )
				  {
  				    SimpleMailMessage mailMessage = new SimpleMailMessage();
				    mailMessage.setTo(ObserverProperties.getMailReceiver());
				    mailMessage.setReplyTo(ObserverProperties.getMailReplyTo());
				    mailMessage.setFrom(ObserverProperties.getMailSender());
				    mailMessage.setSubject("Burstcoin-Observer - Stuck at block: " + networkBean.getHeight().toString());
				    mailMessage.setText(networkBean.getDomain() + "\r\n" + "Please check: " + ObserverProperties.getObserverUrl());
				    mailSender.send(mailMessage);
				  }
            	}
              }
            }
          }

          if(sendMail && !forkMailSend)
          {
            if(ObserverProperties.isEnableForkNotify()
               && (appStartedAfterForkHappened || (lastBlockWithSameGenSigMailSend != null && !lastBlockWithSameGenSig
              .equals(lastBlockWithSameGenSigMailSend))))
            {
              forkMailSend = true;
              // ensure only one mail send per lastBlockWithSameGenSig e.g. if forked wallet pops off blocks over and over again
              lastBlockWithSameGenSigMailSend = lastBlockWithSameGenSig;

              SimpleMailMessage mailMessage = new SimpleMailMessage();
              mailMessage.setTo(ObserverProperties.getMailReceiver());
              mailMessage.setReplyTo(ObserverProperties.getMailReplyTo());
              mailMessage.setFrom(ObserverProperties.getMailSender());
              mailMessage.setSubject("Burstcoin-Observer - Fork after block: " + lastBlockWithSameGenSig);
              mailMessage.setText("Please check: " + ObserverProperties.getObserverUrl());
              mailSender.send(mailMessage);
            }
          }
          else if(!sendMail && !appStartedAfterForkHappened)
          {
            forkMailSend = false;
          }
          
          // store the network state for next check-loop 
          for(NetworkBean networkBean : networkBeans)
          {
            if(networkBean.getAvailable() && networkBean.getDomain() != null)
            {
              previousStateLookup.put(networkBean.getDomain(), networkBean.getState());
            }
          }
          
          publisher.publishEvent(new NetworkUpdateEvent(networkBeans, lastBlockWithSameGenSig));
        }
        catch(Exception e)
        {
          LOG.error("Failed receiving Network data.");
        }
      }
    }, 200, ObserverProperties.getNetworkRefreshInterval());
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
        LOG.info("Unable to get mining info from wallet for '" + server + "'");
      }
    }

    return result;
  }
}
