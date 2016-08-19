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
import burstcoin.observer.bean.CrowdfundBean;
import burstcoin.observer.bean.CrowdfundState;
import burstcoin.observer.controller.CrowdfundController;
import burstcoin.observer.event.CrowdfundUpdateEvent;
import burstcoin.observer.service.model.State;
import burstcoin.observer.service.model.at.AutomatedTransaction;
import burstcoin.observer.service.model.at.AutomatedTransactionIds;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@Component
public class ATService
{
  private static Log LOG = LogFactory.getLog(ATService.class);

  private static final String CROWDFUND_AT_CODE = "350003000000002501000000350004020000002102000000030000004f350103040000003304030400000035250105000000"
                                                  + "1b050000004a3506030600000035070304000000320a0301070000000200000000000000330204060000001a240000000"
                                                  + "1070000000100000000000000320b033203043502030400000033040304000000352501050000001b05000000f23507030"
                                                  + "4000000320b033203041a7c";

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private HttpClient httpClient;

  @Autowired
  private ApplicationEventPublisher publisher;

  @Autowired
  private ApplicationContext context;

  private Timer timer = new Timer();

  private List<String> blacklistedAtRS;

  @PostConstruct
  public void init()
  {
    blacklistedAtRS = new ArrayList<>();
    blacklistedAtRS.add("BURST-VGHL-Z8N5-SMJB-4GJNN");
    blacklistedAtRS.add("BURST-Q9E6-4BVM-GXPU-BMSSU");
    blacklistedAtRS.add("BURST-S59U-5GQ5-ZGVH-7T8X3");
    blacklistedAtRS.add("BURST-8QLW-LULL-HUC9-FYVQX");
    blacklistedAtRS.add("BURST-VBF4-SBVZ-M4FU-74DQQ");
    blacklistedAtRS.add("BURST-PZGK-23GF-F3SB-4R44P");
    blacklistedAtRS.add("BURST-653T-WXLE-WMGK-9NN69");
    blacklistedAtRS.add("BURST-5LRG-YCVL-Z9LL-4C2BX");
    blacklistedAtRS.add("BURST-LT2E-ZKB7-J48V-8HPGG");

    LOG.info("Started repeating 'check at' task.");
    startCheckAutomatedTransactionsTask();
  }

  private void startCheckAutomatedTransactionsTask()
  {
    timer.schedule(new TimerTask()
    {
      @Override
      public void run()
      {
        try
        {
          LOG.info("START import at data");
          Map<String, AutomatedTransaction> atLookup = new HashMap<>();
          for(String atId : getAutomatedTransactionIds())
          {
            AutomatedTransaction automatedTransaction = getAutomatedTransaction(atId);
            atLookup.put(atId, automatedTransaction);
          }
          State state = getState();

          LOG.info("FINISH import at data!");
          Long currentBlock = state.getNumberOfBlocks();

          List<CrowdfundBean> crowdfundBeans = new ArrayList<>();

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
                String targetAmountInt = targetAmount.length() > 8 ? targetAmount.substring(0, targetAmount.length() - 8) : targetAmount;

                String decisionHex = at.getMachineData().substring(16, 32);
                Integer decision = Integer.valueOf(getATLong(decisionHex));

                String transactionHex = at.getMachineData().substring(8, 16);
                Integer transaction = Integer.valueOf(getATLong(transactionHex));

                Long ends = transaction + decision - currentBlock;
                Long running = currentBlock - transaction;

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

                // skip active that never got started/funded while running
                if((ends > 0 || !CrowdfundState.ACTIVE.equals(cfState)) && !blacklistedAtRS.contains(at.getAtRS()))
                {

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

                  double percent = getPercentageCorrect(Integer.valueOf(targetAmountInt), Integer.valueOf(current));
                  int round = Math.round((float) percent);
                  round = round > 100 ? 100 : round;

                  crowdfundBeans.add(new CrowdfundBean(at.getAt(), at.getAtRS(), at.getCreatorRS(), at.getName(),
                                                       at.getDescription().length() > 9
                                                       ? at.getDescription().substring(0, at.getDescription().length() - 9)
                                                       : at.getDescription(),
                                                       cfState, targetAmountInt, current,
                                                       round + "", percent, ends.equals(currentBlock) ? "N/A" : String.valueOf(ends)));
                }
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

              // todo no idea why this currently does not work
              // publisher.publishEvent(new ATDataUpdateEvent(state, atLookup));
              // workaround:
              context.getBean(CrowdfundController.class).handleMessage(new CrowdfundUpdateEvent(crowdfundBeans));
            }
          }
        }
        catch(Exception e)
        {
          LOG.error("Failed update crowdfund data");
        }
      }
    }, 200, ObserverProperties.getCrowdfundRefreshInterval());
  }

  private List<String> getAutomatedTransactionIds()
  {
    List<String> atIds = null;
    try
    {
      ContentResponse response = httpClient.newRequest(ObserverProperties.getWalletUrl() + "/burst?requestType=getATIds")
        .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
        .send();

      AutomatedTransactionIds automatedTransactionIds = objectMapper.readValue(response.getContentAsString(), AutomatedTransactionIds.class);
//      LOG.info("received '" + automatedTransactionIds.getAtIds().size() + "' at's in '" + automatedTransactionIds.getRequestProcessingTime() + "' ms");

      atIds = automatedTransactionIds.getAtIds();
    }
    catch(Exception e)
    {
      LOG.warn("Error: Failed to 'getATIds': " + e.getMessage());
    }
    return atIds;
  }

  private AutomatedTransaction getAutomatedTransaction(String atId)
  {
    AutomatedTransaction automatedTransaction = null;
    try
    {
      ContentResponse response = httpClient.POST(ObserverProperties.getWalletUrl() + "/burst")
        .param("requestType", "getAT")
        .param("at", atId)
        .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
        .send();

      automatedTransaction = objectMapper.readValue(response.getContentAsString(), AutomatedTransaction.class);
//      LOG.info("received '" + automatedTransaction.getName() + "' ats in '" + automatedTransaction.getRequestProcessingTime() + "' ms");
    }
    catch(Exception e)
    {
      LOG.warn("Error: Failed to 'getAT': " + e.getMessage());
    }
    return automatedTransaction;
  }

  private State getState()
  {
    State state = null;
    try
    {
      InputStreamResponseListener listener = new InputStreamResponseListener();

      Request request = httpClient.newRequest(ObserverProperties.getWalletUrl() + "/burst?requestType=getState&includeCounts=true")
        .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS);
      request.send(listener);

      Response response = listener.get(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS);

      // Look at the response
      if(response.getStatus() == 200)
      {
        // Use try-with-resources to close input stream.
        try (InputStream responseContent = listener.getInputStream())
        {
          state = objectMapper.readValue(responseContent, State.class);
//          LOG.info("received '" + trades.getTrades().size() + "' trades in '" + trades.getRequestProcessingTime() + "' ms");
        }
        catch(Exception e)
        {
          LOG.error("Failed to receive faucet account transactions.");
        }
      }
    }
    catch(Exception e)
    {
      LOG.warn("Error: Failed to 'getAllTrades': " + e.getMessage());
    }
    return state;
  }

  public static float getPercentageCorrect(int questions, int correct)
  {
    float proportionCorrect = ((float) correct) / ((float) questions);
    return proportionCorrect * 100;
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
