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
import burstcoin.observer.event.AssetInfoUpdateEvent;
import burstcoin.observer.model.State;
import burstcoin.observer.model.asset.Asset;
import burstcoin.observer.model.asset.AssetInfo;
import burstcoin.observer.model.asset.Order;
import burstcoin.observer.model.asset.OrderType;
import burstcoin.observer.model.asset.Trade;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Controller
public class AssetController
{
  private List<AssetInfo> assetInfos = new ArrayList<>();


  @EventListener
  public void handleMessage(AssetInfoUpdateEvent event)
  {
    Map<String, Asset> assetLookup = event.getAssetLookup();
    Map<OrderType, Map<Asset, List<Order>>> orderLookup = event.getOrderLookup();
    Map<Asset, List<Trade>> tradeLookup = event.getTradeLookup();
    State state = event.getState();


    assetInfos = new ArrayList<>();
    for(Asset asset : assetLookup.values())
    {
      long volume7Days = 0L;
      long volume30Days = 0L;
      Long lastPrice = null;

      List<Trade> trades = tradeLookup.get(asset);
      if(trades != null && !trades.isEmpty())
      {
        Iterator<Trade> iterator = trades.iterator();
        boolean withinLast30Days = true;

        while(withinLast30Days && iterator.hasNext())
        {
          Trade trade = iterator.next();
//          if(lastPrice == null)
//          {
//            lastPrice = Long.valueOf(trade.getPriceNQT());
//
//            System.out.println("---------------------------------");
//            System.out.println("trade price: " + trade.getPriceNQT());
//            System.out.println("trade amount: " + trade.getQuantityQNT());
//            System.out.println("asset decimals: " + asset.getDecimals());
//            int sub = trade.getPriceNQT().length() - asset.getDecimals();
////            if(asset.getDecimals() > 0)
////            {
////              sub = trade.getPriceNQT().length() - asset.getDecimals();
////            }
//            if(trade.getPriceNQT().length() > sub)
//            {
//              System.out.println("price1: " + trade.getPriceNQT().substring(0, sub));
//            }
//            else
//            {
//              System.out.println("price1: " + "> 1");
//            }
////            System.out.println("price1: " + trade.getPriceNQT().substring(0, trade.getPriceNQT().length() - asset.getDecimals()));
//          }

          Integer bidOrderBlock = Integer.valueOf(trade.getBidOrderHeight());
          Integer askOrderBlock = Integer.valueOf(trade.getAskOrderHeight());
          int block = bidOrderBlock >= askOrderBlock ? bidOrderBlock : askOrderBlock;
          withinLast30Days = state.getNumberOfBlocks() - 360 * 30 < block;

          if(withinLast30Days)
          {

            long volume = Long.valueOf(trade.getPriceNQT()) * Long.valueOf(trade.getQuantityQNT());
            volume30Days += volume;

            if(state.getNumberOfBlocks() - 360 * 7 < block)
            {
              volume7Days += volume;
            }
          }
        }
      }

      List<Order> sellOrders = orderLookup.get(OrderType.ASK).get(asset) != null ? orderLookup.get(OrderType.ASK).get(asset) : new ArrayList<>();
      List<Order> buyOrders = orderLookup.get(OrderType.BID).get(asset) != null ? orderLookup.get(OrderType.BID).get(asset) : new ArrayList<>();

      if(!(buyOrders.isEmpty() && sellOrders.isEmpty() && asset.getNumberOfTrades() < 2))
      {
        assetInfos.add(new AssetInfo(asset.getAsset(), asset.getName(), asset.getDescription(), asset.getAccountRS(), asset.getAccount(),
                                     asset.getQuantityQNT(), asset.getDecimals(), asset.getNumberOfAccounts(), asset.getNumberOfTransfers(),
                                     asset.getNumberOfTrades(), buyOrders.size(), sellOrders.size(),
                                     formatAmountNQT(volume7Days, 8), formatAmountNQT(volume30Days, 8),
                                     "N/A")); // lastPrice != null ? formatAmountNQT(lastPrice, asset.getDecimals() > 0 ? asset.getDecimals() : 8) : "N/A")
      }
    }
    Collections.sort(assetInfos, new Comparator<AssetInfo>()
    {
      @Override
      public int compare(AssetInfo o1, AssetInfo o2)
      {
        return Long.valueOf(o2.getVolume7Days()).compareTo(Long.valueOf(o1.getVolume7Days()));
      }
    });
  }

  private String formatAmountNQT(Long amount, int decimals)
  {
    String amountStr = String.valueOf(amount);
//    return amountStr;
    return amount != null && amountStr.length() >= decimals ? amountStr.substring(0, amountStr.length() - decimals) : "" + amount;
  }

  @RequestMapping("/asset")
  public String pool(Model model)
  {
    // todo interval
    model.addAttribute("refreshContent", "240; URL=" + ObserverProperties.getObserverUrl() + "/asset");
    if(assetInfos != null)
    {
      model.addAttribute("assetInfos", assetInfos);
    }

    return "asset";
  }

  @RequestMapping(value = "/asset/json", produces = "application/json")
  @ResponseBody
  public List<AssetInfo> json()
  {
    return assetInfos != null ? assetInfos : new ArrayList<>();
  }

  @RequestMapping(value = "/asset/jsonp", produces = "application/json")
  @ResponseBody
  public MappingJacksonValue jsonp(@RequestParam String callback)
  {
    callback = callback == null || callback.equals("") ? "callback" : callback;
    MappingJacksonValue value = new MappingJacksonValue(assetInfos != null ? assetInfos : new ArrayList<>());
    value.setJsonpFunction(callback);
    return value;
  }
}