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
import burstcoin.observer.event.ATDataUpdateEvent;
import burstcoin.observer.model.State;
import burstcoin.observer.model.at.AutomatedTransaction;
import burstcoin.observer.model.at.CrowdfundBean;
import burstcoin.observer.model.at.CrowdfundState;
import burstcoin.observer.model.navigation.NavigationPoint;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CrowdfundController
  extends BaseController
{
  private static final String CROWDFUND_AT_CODE = "350003000000002501000000350004020000002102000000030000004f350103040000003304030400000035250105000000"
                                                  + "1b050000004a3506030600000035070304000000320a0301070000000200000000000000330204060000001a240000000"
                                                  + "1070000000100000000000000320b033203043502030400000033040304000000352501050000001b05000000f23507030"
                                                  + "4000000320b033203041a7c";

  private List<CrowdfundBean> crowdfundBeans;

  @PostConstruct
  public void init()
  {

  }

  public void handleMessage(ATDataUpdateEvent event)
  {
    State state = event.getState();
    Long currentBlock = state.getNumberOfBlocks();

    crowdfundBeans = new ArrayList<>();


    Map<String, AutomatedTransaction> atLookup = event.getAtLookup();

    // different by code?!
    Map<String, List<AutomatedTransaction>> atByCodeLookup = new HashMap<>();
    for(AutomatedTransaction automatedTransaction : atLookup.values())
    {
      if(!atByCodeLookup.containsKey(automatedTransaction.getMachineCode()))
      {
        atByCodeLookup.put(automatedTransaction.getMachineCode(), new ArrayList<>());
      }
      atByCodeLookup.get(automatedTransaction.getMachineCode()).add(automatedTransaction);
    }

    for(Map.Entry<String, List<AutomatedTransaction>> entry : atByCodeLookup.entrySet())
    {
      // filter for crowdfund
      if(entry.getKey().contains(CROWDFUND_AT_CODE))
      {
        for(AutomatedTransaction at : entry.getValue())
        {
          // target amount
          String targetAmountHex = at.getMachineData().substring(48, 64);
          String targetAmount = getATLong(targetAmountHex);
          String targetAmountInt = targetAmount.length() > 8 ?  targetAmount.substring(0, targetAmount.length() - 8): targetAmount;

          String decisionHex = at.getMachineData().substring(16, 32);
          Integer decision = Integer.valueOf(getATLong(decisionHex));

          String transactionHex = at.getMachineData().substring(8, 16);
          Integer transaction = Integer.valueOf(getATLong(transactionHex));

          Long ends = transaction + decision - currentBlock;
          Long diff = currentBlock - transaction;

          String fundedHex = at.getMachineData().substring(7 * 16, 7 * 16 + 16);
          String funded = getATLong(fundedHex);
          CrowdfundState cfState = CrowdfundState.ACTIVE;
          switch(funded)
          {
            case "2":
              cfState = CrowdfundState.NOT_FUNDED;
              break;
            case "1":
              cfState = CrowdfundState.FUNDED;
              break;
          }


          String current = "0";
          if(at.getBalanceNQT().length() > 8)
          {
            current = at.getBalanceNQT().substring(0, at.getBalanceNQT().length() - 8);
          }
          if(CrowdfundState.FUNDED.equals(cfState) || CrowdfundState.NOT_FUNDED.equals(cfState))
          {
            String gatheredAmountHex = at.getMachineData().substring(32, 48);
            String gatheredAmount = getATLong(gatheredAmountHex);
            if(gatheredAmount.length() > 8)
            {
              current = gatheredAmount.substring(0, gatheredAmount.length() - 8);
            }
          }

          double percent = getPercentageCorrect( Integer.valueOf(targetAmountInt),Integer.valueOf(current) );
          int round = Math.round((float)percent);
          round = round > 100 ? 100 : round;

          crowdfundBeans.add(new CrowdfundBean(at.getAtRS(), at.getCreatorRS(), at.getName(), at.getDescription(), cfState, targetAmountInt, current,
                                               round + "", percent, diff + ""));
        }

        Collections.sort(crowdfundBeans, new Comparator<CrowdfundBean>()
        {
          @Override
          public int compare(CrowdfundBean o1, CrowdfundBean o2)
          {
            return Long.valueOf(o2.getCurrentAmount()).compareTo(Long.valueOf(o1.getCurrentAmount()));
          }
        });
        Collections.sort(crowdfundBeans, new Comparator<CrowdfundBean>()
        {
          @Override
          public int compare(CrowdfundBean o1, CrowdfundBean o2)
          {
            return o1.getState().compareTo(o2.getState());
          }
        });
      }
    }
  }

  public static float getPercentageCorrect(int questions, int correct) {
    float proportionCorrect = ((float) correct) / ((float) questions);
    return proportionCorrect * 100;
  }

  @RequestMapping({"/crowdfund"})
  public String index(Model model)
  {
    addNavigationBean(NavigationPoint.CROWDFUND, model);
    model.addAttribute("refreshContent",
                       ObserverProperties.getCrowdfundRefreshInterval() / 1000 + 1 + "; URL=" + ObserverProperties.getObserverUrl() + "/crowdfund");
    model.addAttribute("interval", ObserverProperties.getCrowdfundRefreshInterval() / 1000);
    if(crowdfundBeans != null)
    {
      model.addAttribute("crowdfundBeans", crowdfundBeans);
    }

    return "crowdfund";
  }

  @RequestMapping(value = "/crowdfund/json", produces = "application/json")
  @ResponseBody
  public List<CrowdfundBean> json()
  {
    return crowdfundBeans != null ? crowdfundBeans : new ArrayList<>();
  }

  @RequestMapping(value = "/crowdfund/jsonp", produces = "application/json")
  @ResponseBody
  public MappingJacksonValue jsonp(@RequestParam String callback)
  {
    callback = callback == null || callback.equals("") ? "callback" : callback;
    MappingJacksonValue value = new MappingJacksonValue(crowdfundBeans != null ? crowdfundBeans : new ArrayList<>());
    value.setJsonpFunction(callback);
    return value;
  }


  public static String getATLong(String hex)
  {
    ByteBuffer bf = ByteBuffer.allocate(8);
    bf.order(ByteOrder.LITTLE_ENDIAN);
    bf.put(parseHexString(hex));
    return toUnsignedLong(bf.getLong(0));
  }

  //
  // nxt.util.Convert methods
  //

  public static final BigInteger two64 = new BigInteger("18446744073709551616");

  public static byte[] parseHexString(String hex)
  {
    byte[] bytes = new byte[hex.length() / 2];
    for(int i = 0; i < bytes.length; i++)
    {
      int char1 = hex.charAt(i * 2);
      char1 = char1 > 0x60 ? char1 - 0x57 : char1 - 0x30;
      int char2 = hex.charAt(i * 2 + 1);
      char2 = char2 > 0x60 ? char2 - 0x57 : char2 - 0x30;
      if(char1 < 0 || char2 < 0 || char1 > 15 || char2 > 15)
      {
        throw new NumberFormatException("Invalid hex number: " + hex);
      }
      bytes[i] = (byte) ((char1 << 4) + char2);
    }
    return bytes;
  }

  public static String toUnsignedLong(long objectId)
  {
    if(objectId >= 0)
    {
      return String.valueOf(objectId);
    }
    BigInteger id = BigInteger.valueOf(objectId).add(two64);
    return id.toString();
  }
}
