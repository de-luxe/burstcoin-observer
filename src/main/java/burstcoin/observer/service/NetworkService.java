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
import burstcoin.observer.bean.NetworkBean;
import burstcoin.observer.bean.NetworkState;
import burstcoin.observer.event.NetworkUpdateEvent;
import burstcoin.observer.service.model.MiningInfo;
import burstcoin.observer.service.network.GetMiningInfoTask;
import burstcoin.observer.service.network.MiningInfoEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

@Component
public class NetworkService
{
  private static Log LOG = LogFactory.getLog(NetworkService.class);

  @Autowired
  private ApplicationEventPublisher publisher;

  @Autowired
  private BeanFactory beanFactory;

  @Autowired
  private JavaMailSender mailSender;

  @Autowired
  @Qualifier("networkTaskExecutor")
  private SimpleAsyncTaskExecutor taskExecutor;

  private Timer timer = new Timer();
  // blockHeight -> genSig -> domains
  private Map<Long, Map<String, Set<String>>> genSigLookup = new HashMap<>();
  private boolean forkMailSend = false;
  private Long lastBlockWithSameGenSigMailSend = 0L;
  private Map<String, NetworkState> previousStateLookup = new HashMap<>();

  private static final Map<String, MiningInfo> miningInfoLookup = new HashMap<>();

  private Date lastUpdate = new Date();

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
        // only start if previous is finished
        if(miningInfoLookup.isEmpty() || new Date().getTime() - (5 * 60 * 1000) > lastUpdate.getTime())
        {
          miningInfoLookup.clear();
          try
          {
            lastUpdate = new Date();
            for(String networkServerUrl : ObserverProperties.getNetworkServerUrls())
            {
              GetMiningInfoTask getMiningInfoTask = beanFactory.getBean(GetMiningInfoTask.class, networkServerUrl);
              taskExecutor.execute(getMiningInfoTask);
            }
          }
          catch(Exception e)
          {
            LOG.error("Failed receiving Network data.");
          }
        }
      }
    }, 1200, ObserverProperties.getNetworkRefreshInterval());
  }

  @EventListener
  public void onMiningInfoEvent(MiningInfoEvent miningInfoEvent)
  {
    synchronized(miningInfoLookup)
    {
      MiningInfo miningInfo = miningInfoEvent.getMiningInfo();
      if(miningInfo != null)
      {
        miningInfoLookup.put(miningInfoEvent.getNetworkServerUrl(), miningInfo);
      }
      else
      {
        miningInfoLookup.put(miningInfoEvent.getNetworkServerUrl(), null);
      }
      if(ObserverProperties.getNetworkServerUrls().size() == miningInfoLookup.size())
      {
        LOG.info("Received MiningInfo from all newtworkServerUrls ...");
        processMiningInfoLookup(miningInfoLookup);
        miningInfoLookup.clear();
      }
    }
  }

  private void processMiningInfoLookup(Map<String, MiningInfo> miningInfoLookup)
  {
    List<NetworkBean> networkBeans = new ArrayList<>();
    for(Map.Entry<String, MiningInfo> entry : miningInfoLookup.entrySet())
    {
      MiningInfo miningInfo = entry.getValue();

      String https = entry.getKey().contains("https://") ? "Yes" : "No";
      String domain = entry.getKey().replace("http://", "").replace("https://", "");
      if(miningInfo != null && miningInfo.getGenerationSignature() != null)
      {
        networkBeans.add(new NetworkBean(String.valueOf(miningInfo.getHeight()), domain, entry.getKey(), miningInfo.getBaseTarget(),
                                         miningInfo.getGenerationSignature().substring(0, 25) + "...",
                                         String.valueOf(miningInfo.getTargetDeadline()), https));
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
        return o1.getType().compareTo(o2.getType());
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
    Long lastBlockWithSameGenSig = null;
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

        if(height + 4 < maxHeight) // when the wallet is 4 blocks behind -> stuck
        {
          if(!networkBean.getState().equals(NetworkState.FORKED)) // if it's forked, then ignore the stuck-check, because forks may also be behind
          {
            networkBean.setState(NetworkState.STUCK);

            if(ObserverProperties.isEnableStuckNotify()
               && (!previousStateLookup.containsKey(networkBean.getDomain())
                   || !previousStateLookup.get(networkBean.getDomain()).equals(NetworkState.STUCK))) //send only once
            {
              sendMessage("Burstcoin-Observer - Stuck at block: " + networkBean.getHeight(),
                          networkBean.getDomain() + "\r\n" + "Please check: " + ObserverProperties.getObserverUrl());
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

        sendMessage("Burstcoin-Observer - Fork after block: " + lastBlockWithSameGenSig, "Please check: " + ObserverProperties.getObserverUrl());
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

    publisher.publishEvent(new NetworkUpdateEvent(networkBeans, lastBlockWithSameGenSig, createSenkeyChartData()));
  }

  // create multi senkey chart data
  private List<List> createSenkeyChartData()
  {
    List<List> multiSenkeyData = new ArrayList<>();
    int numberOfBlocks = 11;
    List<Long> orderedHeight = new ArrayList<>(genSigLookup.keySet());
    Collections.sort(orderedHeight);
    List<Long> heightSub = orderedHeight.subList((orderedHeight.size() > numberOfBlocks ? orderedHeight.size() - numberOfBlocks : 0), orderedHeight.size());

    Map<String, Long> sourcePerGenSigLookup = new HashMap<>();
    Map<Long, Map<String, String>> remainingSourcesWithoutTarget = new HashMap<>();
    for(Long source : heightSub)
    {
      remainingSourcesWithoutTarget.put(source, new HashMap<>());

      if(heightSub.indexOf(source) < heightSub.size() - 1)
      {
        Long target = heightSub.get(heightSub.indexOf(source) + 1);
        Map<String, Set<String>> sourceGenSigDomainLookup = genSigLookup.get(source);
        // domain -> gensig
        Map<String, String> sourceMapping = new HashMap<>();

        for(Map.Entry<String, Set<String>> sourceEntry : sourceGenSigDomainLookup.entrySet())
        {
          for(String domain : sourceEntry.getValue())
          {
            sourceMapping.put(domain, sourceEntry.getKey());
          }
        }

        Map<String, Set<String>> targetGenSigDomainLookup = genSigLookup.get(target);
        // domain -> gensig
        Map<String, String> targetMapping = new HashMap<>();
        for(Map.Entry<String, Set<String>> targetEntry : targetGenSigDomainLookup.entrySet())
        {
          for(String domain : targetEntry.getValue())
          {
            targetMapping.put(domain, targetEntry.getKey());
          }
        }

        // sourceGenSig -> targetGensig -> domains
        Map<String, Map<String, List<String>>> fromToCounter = new HashMap<>();


        // domain ->genSig
        for(Map.Entry<String, String> entry : sourceMapping.entrySet())
        {
          String domain = entry.getKey();
          String sourceGenSig = entry.getValue();
          String targetGenSig = targetMapping.get(domain);

          // remember source block height by genSig
          sourcePerGenSigLookup.put(sourceGenSig, source);

          // skip if from/to is not available
          if(targetGenSig != null)
          {
            if(!fromToCounter.containsKey(sourceGenSig))
            {
              fromToCounter.put(sourceGenSig, new HashMap<>());
            }
            if(!fromToCounter.get(sourceGenSig).containsKey(targetGenSig))
            {
              fromToCounter.get(sourceGenSig).put(targetGenSig, new ArrayList<>());
            }
            fromToCounter.get(sourceGenSig).get(targetGenSig).add(domain);
          }
          else
          {
            // no target found
            remainingSourcesWithoutTarget.get(source).put(entry.getKey(), entry.getValue());
          }
        }

        // check if we have a target for previous sources without target now
        List<Long> previousSources = new ArrayList<>(remainingSourcesWithoutTarget.keySet());
        // ignore current source
        if(previousSources.contains(source))
        {
          previousSources.remove(source);
        }
        Collections.sort(previousSources);
        for(Long previousSource : previousSources)
        {
          Iterator<Map.Entry<String, String>> iter = remainingSourcesWithoutTarget.get(previousSource).entrySet().iterator();

          while(iter.hasNext())
          {
            Map.Entry<String, String> entry = iter.next();
            String domain = entry.getKey();
            String sourceGenSig = entry.getValue();
            String targetGenSig = targetMapping.get(domain);

            // skip if from/to is not available
            if(targetGenSig != null)
            {
              if(!fromToCounter.containsKey(sourceGenSig))
              {
                fromToCounter.put(sourceGenSig, new HashMap<>());
              }
              if(!fromToCounter.get(sourceGenSig).containsKey(targetGenSig))
              {
                fromToCounter.get(sourceGenSig).put(targetGenSig, new ArrayList<>());
              }
              fromToCounter.get(sourceGenSig).get(targetGenSig).add(domain);

              iter.remove();
            }
          }

          if(remainingSourcesWithoutTarget.get(previousSource).isEmpty())
          {
            remainingSourcesWithoutTarget.remove(previousSource);
          }
        }

        for(String sourceGenSig : fromToCounter.keySet())
        {
          for(Map.Entry<String, List<String>> entry : fromToCounter.get(sourceGenSig).entrySet())
          {
            Long currentSource = sourcePerGenSigLookup.get(sourceGenSig);
            String sourceName = currentSource + " [" + sourceGenSig.substring(0, 4) + "..]";
            String targetName = target + " [" + entry.getKey().substring(0, 4) + "..]";

            String tooltip = entry.getValue().size() + " nodes,"
                             + " from " + currentSource + " [" + sourceGenSig.substring(0, 6) + "..],"
                             + " to " + target + " [" + entry.getKey().substring(0, 6) + "..]";

            multiSenkeyData.add(Arrays.asList(sourceName, targetName, entry.getValue().size(), tooltip));
          }
        }
      }
    }
    return multiSenkeyData;
  }

  private void sendMessage(String subject, String message)
  {
    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setTo(ObserverProperties.getMailReceiver());
    mailMessage.setReplyTo(ObserverProperties.getMailReplyTo());
    mailMessage.setFrom(ObserverProperties.getMailSender());
    mailMessage.setSubject(subject);
    mailMessage.setText(message);
    mailSender.send(mailMessage);
  }
}
