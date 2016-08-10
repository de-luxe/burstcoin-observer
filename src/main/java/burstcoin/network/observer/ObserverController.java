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

package burstcoin.network.observer;

import burstcoin.network.ObserverProperties;
import burstcoin.network.observer.event.MiningInfoUpdateEvent;
import burstcoin.network.observer.model.InfoBean;
import burstcoin.network.observer.model.MiningInfo;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
public class ObserverController
{
  private static Map<String, MiningInfo> compareMiningInfoLookup;
  private static MiningInfo referenceMiningInfo;

  @EventListener
  public void handleMessage(MiningInfoUpdateEvent event)
  {
    compareMiningInfoLookup = event.getCompareMiningInfoLookup();
    referenceMiningInfo = event.getReferenceMiningInfo();
  }

  @RequestMapping("/")
  public String index(Model model)
  {
    List<InfoBean> infoBeans = getInfoBeans();

    model.addAttribute("infoBeans", infoBeans);

    return "index";
  }

  @RequestMapping(value = "/json", produces = "application/json")
  @ResponseBody
  public List<InfoBean> json()
  {
    return getInfoBeans();
  }

  private List<InfoBean> getInfoBeans()
  {
    List<InfoBean> infoBeans = new ArrayList<>();
    if(referenceMiningInfo != null)
    {
      infoBeans.add(new InfoBean(String.valueOf(referenceMiningInfo.getHeight()),
                                 ObserverProperties.getReferenceWalletServer().replace("http://", "").replace("https://", ""),
                                 referenceMiningInfo.getBaseTarget(), referenceMiningInfo.getGenerationSignature().substring(0, 25) + "...",
                                 String.valueOf(referenceMiningInfo.getTargetDeadline())));

      for(Map.Entry<String, MiningInfo> entry : compareMiningInfoLookup.entrySet())
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
    }

    Collections.sort(infoBeans, new Comparator<InfoBean>()
    {
      @Override
      public int compare(InfoBean o1, InfoBean o2)
      {
        return o2.getHeight().compareTo(o1.getHeight());
      }
    });
    return infoBeans;
  }
}
