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

package burstcoin.observer.service;

import burstcoin.observer.ObserverProperties;
import burstcoin.observer.bean.NetworkBean;
import burstcoin.observer.bean.NodeListBean;
import burstcoin.observer.bean.NodeStats;
import burstcoin.observer.event.NetworkUpdateEvent;
import burstcoin.observer.event.NodeUpdateEvent;
import burstcoin.observer.service.model.BlockchainStatus;
import burstcoin.observer.service.model.node.Peer;
import burstcoin.observer.service.model.node.PeerInfo;
import burstcoin.observer.service.model.node.Peers;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class NodeService
{
  private static Log LOG = LogFactory.getLog(NodeService.class);

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private HttpClient httpClient;

  @Autowired
  private ApplicationEventPublisher publisher;

  private List<NetworkBean> networkBeans;

  private Timer timer = new Timer();
  private Map<String, PeerInfo> peerInfoLookup;

  @EventListener
  public void handleMessage(NetworkUpdateEvent event)
  {
    if(event.getNetworkBeans() != null && !event.getNetworkBeans().isEmpty())
    {
      networkBeans = event.getNetworkBeans();
    }
  }

  @PostConstruct
  private void postConstruct()
  {
    LOG.info("Started repeating 'check nodes' task.");
    peerInfoLookup = new HashMap<>();
    startCheckNodesTask();
  }

  public void startCheckNodesTask()
  {
    timer.schedule(new TimerTask()
    {
      @Override
      public void run()
      {
        try
        {
          while(networkBeans == null || networkBeans.isEmpty())
          {
            Thread.sleep(1000 * 60); // wait another minute
          }

          Map<String, Peer> peerLookup = new HashMap<>();
          for(NetworkBean networkBean : networkBeans)
          {
            if("Wallet".equals(networkBean.getType()))
            {
              addMissingPeers(networkBean.getUrl(), peerLookup);
            }
          }

          for(String ip : peerLookup.keySet())
          {
            if(!peerInfoLookup.containsKey(ip))
            {
              PeerInfo peerInfo = getPeerInfo(ip);
              if(peerInfo != null)
              {
                peerInfoLookup.put(ip, peerInfo);
              }

              // max 150 requests per minute
              Thread.sleep(1000 * 60 / 155);
            }
          }

          BlockchainStatus blockchainStatus = getBlockchainStatus();
          long blockChainTimeInMs = blockchainStatus.getTime() * 1000;
          long blockZeroTime = new Date().getTime() - blockChainTimeInMs;

          Map<String, Integer> nodesByCountry = new HashMap<>();
          List<NodeListBean> nodeBeans = new ArrayList<>();

          List<List> mapData = new ArrayList<>();
          mapData.add(Arrays.asList("Lat", "Long", "Name"));

          for(Map.Entry<String, PeerInfo> entry : peerInfoLookup.entrySet())
          {
            String ip = entry.getKey();
            PeerInfo peerInfo = entry.getValue();

            Peer peer = peerLookup.get(ip);
            if(peer != null)
            {
              // todo lastUpdate is not accurate, as it reflects the lastUpdate form first found wallet
              Date lastUpdate = new Date(blockZeroTime + peer.getLastUpdated() * 1000);
              long minLastActivity = new Date().getTime() - (1000 * 60 * 60); // min last updated 1h ago

              // only add peers that were updated in last 12h
              if(minLastActivity <= lastUpdate.getTime())
              {
                if(!nodesByCountry.containsKey(peerInfo.getCountry()))
                {
                  nodesByCountry.put(peerInfo.getCountry(), 0);
                }
                nodesByCountry.put(peerInfo.getCountry(), nodesByCountry.get(peerInfo.getCountry()) + 1);

                nodeBeans.add(new NodeListBean(lastUpdate, peer.getAnnouncedAddress(), ip,
                                               peer.getVersion() != null ? peer.getVersion() : "N/A", peer.getPlatform(),
                                               peerInfo.getCountry() != null ? peerInfo.getCountry() : "N/A", peerInfo.getRegionName(), peerInfo.getCity(),
                                               peerInfo.getIsp() != null ? peerInfo.getIsp() : "N/A"));

                mapData.add(Arrays.asList(Float.valueOf(peerInfo.getLat()), Float.valueOf(peerInfo.getLon()), peer.getAnnouncedAddress()));
              }
            }
          }

          Collections.sort(nodeBeans, new Comparator<NodeListBean>()
          {
            @Override
            public int compare(NodeListBean o1, NodeListBean o2)
            {
              return o2.getIsp().compareTo(o1.getIsp());
            }
          });

          Collections.sort(nodeBeans, new Comparator<NodeListBean>()
          {
            @Override
            public int compare(NodeListBean o1, NodeListBean o2)
            {
              return o1.getCountry().compareTo(o2.getCountry());
            }
          });

          Collections.sort(nodeBeans, new Comparator<NodeListBean>()
          {
            @Override
            public int compare(NodeListBean o1, NodeListBean o2)
            {
              return o2.getVersion().compareTo(o1.getVersion());
            }
          });

          // quickfix to put N/A at the end
          List<NodeListBean> atTheEnd = new ArrayList<>();
          for(NodeListBean nodeBean : nodeBeans)
          {
            if(nodeBean.getVersion().equals("N/A"))
            {
              atTheEnd.add(nodeBean);
            }
          }
          nodeBeans.removeAll(atTheEnd);
          nodeBeans.addAll(atTheEnd);

          // google geo chart data
          List<List> geoData = new ArrayList<>();
          geoData.add(Arrays.asList("Country", "Nodes"));
          for(Map.Entry<String, Integer> entry : nodesByCountry.entrySet())
          {
            geoData.add(Arrays.asList(entry.getKey(), entry.getValue()));
          }

          // create wellKnownsPeers string
          String wellKnownPeers = "nxt.wellKnownPeers=";
          Set<String> nodeNames = new HashSet<>();
          for(NodeListBean nodeBean : nodeBeans)
          {
            nodeNames.add(nodeBean.getIp());
          }
          for(String name : nodeNames)
          {
            wellKnownPeers += name + "; ";
          }
          NodeStats nodeStats = new NodeStats(peerInfoLookup.size(), nodeBeans.size(), wellKnownPeers);

          Date now = new Date();
          DecimalFormat f = new DecimalFormat("00");
          for(NodeListBean nodeBean : nodeBeans)
          {
            long diff = now.getTime() - nodeBean.getLastUpdate().getTime();
            long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (1000 * 60) % 60;
            long diffHours = diff / (1000 * 60 * 60) % 60;
            // not 100% accurate
            nodeBean.setUpdated(f.format(diffHours < 0 ? diffHours * -1 : diffHours)
                                + ":" + f.format(diffMinutes < 0 ? diffMinutes * -1 : diffMinutes)
                                + ":" + f.format(diffSeconds < 0 ? diffSeconds * -1 : diffSeconds));
          }

          publisher.publishEvent(new NodeUpdateEvent(nodeBeans, nodeStats, geoData, mapData));
        }
        catch(Exception e)
        {
          LOG.error("Failed update nodes!", e);
        }
      }
    }, ObserverProperties.getNetworkRefreshInterval(), ObserverProperties.getNodeRefreshInterval());
  }

  private BlockchainStatus getBlockchainStatus()
  {
    BlockchainStatus result = null;
    try
    {
      ContentResponse response;
      response = httpClient.newRequest(ObserverProperties.getWalletUrl() + "/burst?requestType=getBlockchainStatus")
        .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
        .send();

      result = objectMapper.readValue(response.getContentAsString(), BlockchainStatus.class);
    }
    catch(TimeoutException timeoutException)
    {
      LOG.warn("Unable to get blockchain status caused by connectionTimeout, currently '" + (ObserverProperties.getConnectionTimeout() / 1000)
               + " sec.' try increasing it!");
    }
    catch(Exception e)
    {
      LOG.trace("Unable to get blockchain status info from wallet: " + e.getMessage());
    }
    return result;
  }

  private PeerInfo getPeerInfo(String ip)
  {
    PeerInfo peerInfo = null;
    try
    {
      InputStreamResponseListener listener = new InputStreamResponseListener();

      // reformat ip6
      ip = ip.replace("[", "").replace("]", "");

      Request request = httpClient.newRequest("http://ip-api.com/json/" + URLEncoder.encode(ip, "UTF-8"))
        .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS);
      request.send(listener);

      Response response = listener.get(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS);

      if(response.getStatus() == 200)
      {
        try (InputStream responseContent = listener.getInputStream())
        {
          peerInfo = objectMapper.readValue(responseContent, PeerInfo.class);

          LOG.info("received peerInfo for '" + ip + "'.");
        }
        catch(Exception e)
        {
          LOG.error("Failed to receive peerInfo for '" + ip + "'");
        }
      }
    }
    catch(InterruptedException | ExecutionException | TimeoutException e)
    {
      LOG.error("Failed to receive peerInfo for '" + ip + "'", e);
      return null;
    }
    catch(Exception e)
    {
      LOG.error("Failed to receive peerInfo for '" + ip + "'", e);
      return null;
    }
    return peerInfo;
  }

  private void addMissingPeers(String server, Map<String, Peer> peerLookup)
  {
    try
    {
      InputStreamResponseListener listener = new InputStreamResponseListener();

      Request request = httpClient.newRequest(server + "/burst?requestType=getPeers&active=true")
        .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS);
      request.send(listener);

      Response response = listener.get(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS);
      Peers peers = null;
      if(response.getStatus() == 200)
      {
        try (InputStream responseContent = listener.getInputStream())
        {
          peers = objectMapper.readValue(responseContent, Peers.class);

          LOG.info("received '" + peers.getPeers().size() + "' peers from '" + server + "' in '" + peers.getRequestProcessingTime() + "' ms");
        }
        catch(Exception e)
        {
          LOG.error("Failed to receive peers from '" + server + "'");
        }
      }

      if(peers != null)
      {
        for(String peerIp : peers.getPeers())
        {
          InputStreamResponseListener peerListener = new InputStreamResponseListener();

          Request peerRequest = httpClient.newRequest(server + "/burst?requestType=getPeer&peer=" + peerIp)
            .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS);
          peerRequest.send(peerListener);

          Peer peer = null;
          try (InputStream content = peerListener.getInputStream())
          {
            peer = objectMapper.readValue(content, Peer.class);

            LOG.info("received info for peer '" + peerIp + "' from '" + server + "' in '" + peers.getRequestProcessingTime() + "' ms");
          }
          catch(Exception e)
          {
            LOG.error("Failed to receive peers from '" + server + "'");
          }

          if(peer != null)
          {
            if(!peerLookup.containsKey(peerIp))
            {
              peerLookup.put(peerIp, peer);
            }
            else
            {
              Peer existing = peerLookup.get(peerIp);
              if(existing.getLastUpdated() < peer.getLastUpdated())
              {
                peerLookup.put(peerIp, peer);
              }
            }
          }
        }
      }
    }
    catch(Exception e)
    {
      LOG.warn("Error: Failed to 'getPeers': " + e.getMessage());
    }
  }
}
