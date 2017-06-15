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
import burstcoin.observer.bean.NavigationPoint;
import burstcoin.observer.bean.NodeListBean;
import burstcoin.observer.bean.NodeStats;
import burstcoin.observer.event.NodeUpdateEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
public class NodeController
  extends BaseController
{
  @Autowired
  private ObjectMapper objectMapper;

  private List<NodeListBean> nodeBeans;
  private Date lastUpdate;
  private List<List> geoData;
  private NodeStats nodeStats;
  private List<List> mapData;

  @PostConstruct
  public void init()
  {
    nodeBeans = new ArrayList<>();
    lastUpdate = new Date();
    geoData = new ArrayList<>();
    mapData = new ArrayList<>();
    nodeStats = new NodeStats(0,0,"");
  }

  @EventListener
  public void handleMessage(NodeUpdateEvent event)
  {
    nodeBeans = event.getNodes();
    lastUpdate = event.getLastUpdate();
    geoData = event.getGeoData();
    nodeStats = event.getNodeStats();
    mapData = event.getMapData();
  }

  @RequestMapping("/node")
  public String pool(Model model)
  {
    addNavigationBean(NavigationPoint.NODE, model);

    model.addAttribute("lastUpdate", (new Date().getTime() - lastUpdate.getTime()) / 1000);
    model.addAttribute("refreshContent", ObserverProperties.getNodeRefreshInterval() / 1000 + 1);
    model.addAttribute("interval", ObserverProperties.getNodeRefreshInterval() / 1000);

    model.addAttribute("nodeBeans", nodeBeans);
    model.addAttribute("geoData", geoData);
    model.addAttribute("mapData", mapData);
    model.addAttribute("nodeStats", nodeStats);

    model.addAttribute("googleMapsApiKey", ObserverProperties.getNodeGoogleMapsApiKey());

    return "node";
  }

  @RequestMapping(value = "/node/json", produces = "application/json")
  @ResponseBody
  public List<NodeListBean> json()
  {
    return nodeBeans;
  }

  @RequestMapping(value = "/node/jsonp", produces = "application/json")
  @ResponseBody
  public MappingJacksonValue jsonp(@RequestParam String callback)
  {
    callback = callback == null || callback.equals("") ? "callback" : callback;
    MappingJacksonValue value = new MappingJacksonValue(nodeBeans);
    value.setJsonpFunction(callback);
    return value;
  }
}

