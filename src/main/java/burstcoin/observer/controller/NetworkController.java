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

package burstcoin.observer.controller;

import burstcoin.observer.ObserverProperties;
import burstcoin.observer.event.NetworkMiningInfoUpdateEvent;
import burstcoin.observer.model.InfoBean;
import burstcoin.observer.model.MiningInfo;
import burstcoin.observer.model.navigation.NavigationPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class NetworkController
  extends BaseController
{
  @Autowired
  private JavaMailSender mailSender;

  private List<InfoBean> infoBeans;

  // blockHeight -> genSig -> domains
  private Map<Long, Map<String, Set<String>>> genSigLookup = new HashMap<>();
  private Long lastBlockWithSameGenSig;
  private boolean forkMailSend = false;
  private Long lastBlockWithSameGenSigMailSend = 0L;

  @EventListener
  public void handleMessage(NetworkMiningInfoUpdateEvent event)
  {
    Map<String, MiningInfo> miningInfoLookup = event.getMiningInfoLookup();

    infoBeans = new ArrayList<>();
    if(miningInfoLookup != null)
    {
      for(Map.Entry<String, MiningInfo> entry : miningInfoLookup.entrySet())
      {
        MiningInfo miningInfo = entry.getValue();

        String domain = entry.getKey().replace("http://", "").replace("https://", "");
        if(miningInfo != null && miningInfo.getGenerationSignature() != null)
        {
          infoBeans.add(new InfoBean(String.valueOf(miningInfo.getHeight()), domain, miningInfo.getBaseTarget(),
                                     miningInfo.getGenerationSignature().substring(0, 25) + "...",
                                     String.valueOf(miningInfo.getTargetDeadline())));
        }
        else
        {
          infoBeans.add(new InfoBean(domain));
        }
      }
      Collections.sort(infoBeans, new Comparator<InfoBean>()
      {
        @Override
        public int compare(InfoBean o1, InfoBean o2)
        {
          return o1.getBaseTarget().compareTo(o2.getBaseTarget());
        }
      });
      Collections.sort(infoBeans, new Comparator<InfoBean>()
      {
        @Override
        public int compare(InfoBean o1, InfoBean o2)
        {
          return o2.getHeight().compareTo(o1.getHeight());
        }
      });
    }

    // update genSig Lookup
    int numberOfNotAvailableDomains = 0;
    for(InfoBean infoBean : infoBeans)
    {
      if(infoBean.getAvailable())
      {
        Long height = Long.valueOf(infoBean.getHeight());
        if(!genSigLookup.containsKey(height))
        {
          genSigLookup.put(height, new HashMap<>());
        }
        Map<String, Set<String>> subMap = genSigLookup.get(height);
        if(!subMap.containsKey(infoBean.getGenerationSignature()))
        {
          subMap.put(infoBean.getGenerationSignature(), new HashSet<>());
        }
        Set<String> domains = subMap.get(infoBean.getGenerationSignature());
        domains.add(infoBean.getDomain());
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
        if(infoBeans.size() - numberOfNotAvailableDomains == genSigLookup.get(nextHeight).values().iterator().next().size())
        {
          lastBlockWithSameGenSig = nextHeight;
        }
      }
    }

    boolean appStartedAfterForkHappened = lastBlockWithSameGenSig == null;

    boolean sendMail = false;
    for(InfoBean infoBean : infoBeans)
    {
      if(infoBean.getAvailable())
      {
        Long height = Long.valueOf(infoBean.getHeight());
        Set<String> domainsWithSameGenSigForBlock = genSigLookup.get(height).get(infoBean.getGenerationSignature());
        if(genSigLookup.get(height).size() > 1 && domainsWithSameGenSigForBlock.size() < (infoBeans.size() - numberOfNotAvailableDomains) / 2)
        {
          infoBean.setFork(true);
          sendMail = true;
        }
      }
    }

    if(sendMail && !forkMailSend)
    {
      if(ObserverProperties.isEnableForkNotify()
         && appStartedAfterForkHappened || (lastBlockWithSameGenSigMailSend != null  && !lastBlockWithSameGenSig.equals(lastBlockWithSameGenSigMailSend)))
      {
        forkMailSend = true;
        // ensure only one mail send per lastBlockWithSameGenSig e.g. if forked wallet pops off blocks over and over again
        lastBlockWithSameGenSigMailSend = lastBlockWithSameGenSig;

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(ObserverProperties.getMailReceiver());
        mailMessage.setReplyTo("do-not-reply@burst-team.us");
        mailMessage.setFrom("do-not-reply@burst-team.us");
        mailMessage.setSubject("Burstcoin-Observer - Fork after block: " + lastBlockWithSameGenSig);
        mailMessage.setText("Please check: " + ObserverProperties.getObserverUrl());
        mailSender.send(mailMessage);
      }
    }
    else if(!sendMail && !appStartedAfterForkHappened)
    {
      forkMailSend = false;
    }
  }

  @RequestMapping({"/", "/network"})
  public String index(Model model)
  {
    addNavigationBean(NavigationPoint.NETWORK, model);
    model.addAttribute("refreshContent", ObserverProperties.getNetworkRefreshInterval() / 1000 + 1 + "; URL=" + ObserverProperties.getObserverUrl());
    model.addAttribute("interval", ObserverProperties.getNetworkRefreshInterval() / 1000);
    if(infoBeans != null)
    {
      model.addAttribute("infoBeans", infoBeans);
    }
    model.addAttribute("lastBlockWithSameGenSig", "Last Block height with same generation signature: " + lastBlockWithSameGenSig);

    return "network";
  }

  @RequestMapping(value = "/network/json", produces = "application/json")
  @ResponseBody
  public List<InfoBean> json()
  {
    return infoBeans != null ? infoBeans : new ArrayList<>();
  }

  @RequestMapping(value = "/network/jsonp", produces = "application/json")
  @ResponseBody
  public MappingJacksonValue jsonp(@RequestParam String callback)
  {
    callback = callback == null || callback.equals("") ? "callback" : callback;
    MappingJacksonValue value = new MappingJacksonValue(infoBeans != null ? infoBeans : new ArrayList<>());
    value.setJsonpFunction(callback);
    return value;
  }
}
