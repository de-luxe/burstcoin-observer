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
import org.springframework.context.event.EventListener;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
public class NetworkController
{
  private List<InfoBean> infoBeans;

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
          return o2.getHeight().compareTo(o1.getHeight());
        }
      });
    }

    // todo to determ there is a fork
    // 1. genSig for same block differ (for 4+ blocks)
    // 2. blockHeight differs (for 4+ blocks)
  }

  @RequestMapping({"/","/network"})
  public String index(Model model)
  {
    model.addAttribute("refreshContent", ObserverProperties.getNetworkRefreshInterval() / 1000 + 1 + "; URL=" + ObserverProperties.getObserverUrl());
    if(infoBeans != null)
    {
      model.addAttribute("interval", ObserverProperties.getNetworkRefreshInterval() / 1000);
      model.addAttribute("infoBeans", infoBeans);
    }
    return "index";
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
