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

package burstcoin.observer.controller;

import burstcoin.observer.ObserverProperties;
import burstcoin.observer.bean.AssetBean;
import burstcoin.observer.bean.AssetCandleStickBean;
import burstcoin.observer.bean.NavigationPoint;
import burstcoin.observer.event.AssetUpdateEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class AssetController
  extends BaseController
{
  private List<AssetBean> assetBeans;
  private Date lastUpdate;
  private List<AssetCandleStickBean> assetCandleStickBeans;

  @PostConstruct
  public void init()
  {
    assetBeans = new ArrayList<>();
    lastUpdate = new Date();
    assetCandleStickBeans = new ArrayList<>();
  }

  @EventListener
  public void handleMessage(AssetUpdateEvent event)
  {
    assetCandleStickBeans = event.getAssetCandleStickBeans();
    assetBeans = event.getAssetBeans();
    lastUpdate = event.getLastUpdate();
  }

  @RequestMapping("/asset")
  public String pool(Model model)
  {
    addNavigationBean(NavigationPoint.ASSET, model);

    model.addAttribute("lastUpdate", (new Date().getTime() - lastUpdate.getTime()) / 1000);
    model.addAttribute("refreshContent", ObserverProperties.getAssetRefreshInterval() / 1000 + 1);
    model.addAttribute("interval", ObserverProperties.getAssetRefreshInterval() / 1000);
    if(assetBeans != null)
    {
      model.addAttribute("assetBeans", assetBeans);
    }
    if(assetCandleStickBeans != null)
    {
      model.addAttribute("assetCandleStickBeans", assetCandleStickBeans);
    }

    return "asset";
  }

  @RequestMapping(value = "/asset/json", produces = "application/json")
  @ResponseBody
  public List<AssetBean> json()
  {
    return assetBeans != null ? assetBeans : new ArrayList<>();
  }

  @RequestMapping(value = "/asset/jsonp", produces = "application/json")
  @ResponseBody
  public MappingJacksonValue jsonp(@RequestParam String callback)
  {
    callback = callback == null || callback.equals("") ? "callback" : callback;
    MappingJacksonValue value = new MappingJacksonValue(assetBeans != null ? assetBeans : new ArrayList<>());
    value.setJsonpFunction(callback);
    return value;
  }
}