package burstcoin.observer.service;


import burstcoin.observer.ObserverProperties;
import burstcoin.observer.event.PoolInfoUpdateEvent;
import burstcoin.observer.model.Account;
import burstcoin.observer.model.AccountIds;
import burstcoin.observer.model.Block;
import burstcoin.observer.model.BlockchainStatus;
import burstcoin.observer.model.Blocks;
import burstcoin.observer.model.PoolInfo;
import burstcoin.observer.model.RewardRecipient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class PoolService
{
  private static Log LOG = LogFactory.getLog(PoolService.class);
  private static final String SOLO_KEY = "Solo-Miners";
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private HttpClient httpClient;

  @Autowired
  private ApplicationEventPublisher publisher;

  //  @Autowired
  private Timer timer = new Timer();

  // temp
  private List<Block> blocks;

  @PostConstruct
  private void postConstruct()
  {
    LOG.info("Started repeating 'check pools' task.");
    startCheckPoolsTask();
  }

  private void startCheckPoolsTask()
  {
    timer.schedule(new TimerTask()
    {
      @Override
      public void run()
      {
        blocks = getBlocks(10 /*last 10 days*/);

        final Map<String, Integer> countLookup = new HashMap<>();
        Map<String, Long> rewardLookup = new HashMap<>();

        for(Block block : PoolService.this.blocks)
        {
          String generatorRS = block.getGeneratorRS();
          Long reward = Long.valueOf(block.getBlockReward());
          Long fee = Long.valueOf(block.getTotalFeeNQT());

          if(!countLookup.containsKey(generatorRS))
          {
            countLookup.put(generatorRS, 1);
            rewardLookup.put(generatorRS, reward + fee);
          }
          else
          {
            Integer count = countLookup.get(generatorRS) + 1;
            countLookup.put(generatorRS, count);

            Long rewards = rewardLookup.get(generatorRS) + reward + fee;
            rewardLookup.put(generatorRS, rewards);
          }
        }

        Map<String, Set<String>> rewardAssignmentLookup = new HashMap<>();

        rewardAssignmentLookup.put(SOLO_KEY, new HashSet<>());

        for(Block block : blocks)
        {

          if(block != null)
          {
            final String numericGeneratorAccountId = block.getGenerator();

            // check if numericGeneratorAccountId is known already
            boolean generatorKnown = false;
            Iterator<Map.Entry<String, Set<String>>> iterator = rewardAssignmentLookup.entrySet().iterator();
            while(!generatorKnown && iterator.hasNext())
            {
              generatorKnown = iterator.next().getValue().contains(numericGeneratorAccountId);
            }

            if(!generatorKnown)
            {
              RewardRecipient rewardRecipient = getRewardRecipient(numericGeneratorAccountId);
              final String rewardRecipientAccounId = rewardRecipient.getRewardRecipient();
              if(numericGeneratorAccountId.equals(rewardRecipientAccounId))
              {
                // solo
                rewardAssignmentLookup.get(SOLO_KEY).add(rewardRecipientAccounId);
              }
              else
              {
                // pool - get all reward Assignments of new found pool;
                rewardAssignmentLookup.put(rewardRecipientAccounId, getAccountsWithRewardRecipient(rewardRecipientAccounId));
              }
            }
          }
        }
        onRewardAssignmentLookup(rewardAssignmentLookup);
      }
    }, 2000, 1000 * 60 * 5 /* every 5 min. */);
  }

  private void onRewardAssignmentLookup(Map<String, Set<String>> assignmentLookup)
  {
    // generatorRs -> foundBlocks
    Map<String, Integer> countLookup = new HashMap<>();

    // generatorRS -> reward+fee
    Map<String, Long> rewardLookup = new HashMap<>();

    for(Block block : blocks)
    {
      String generatorId = block.getGenerator();
      Long reward = Long.valueOf(block.getBlockReward() + "00000000");
      Long fee = Long.valueOf(block.getTotalFeeNQT());

      // one id for all solo miners
      if(assignmentLookup.get(SOLO_KEY).contains(generatorId))
      {
        generatorId = SOLO_KEY;
      }

      if(!countLookup.containsKey(generatorId))
      {
        countLookup.put(generatorId, 1);
        rewardLookup.put(generatorId, reward + fee);
      }
      else
      {
        Integer count = countLookup.get(generatorId) + 1;
        countLookup.put(generatorId, count);

        Long rewards = rewardLookup.get(generatorId) + reward + fee;
        rewardLookup.put(generatorId, rewards);
      }
    }

    Map<String, Account> accountLookup = new HashMap<>();
    for(String poolAccount : assignmentLookup.keySet())
    {
      if(!SOLO_KEY.equals(poolAccount))
      {
        Account account = getAccount(poolAccount);
        accountLookup.put(account.getAccount(), account);
      }
    }

    List<PoolInfo> pools = new ArrayList<>();

    // create models
    for(String poolAccountId : assignmentLookup.keySet())
    {
      if(SOLO_KEY.equals(poolAccountId))
      {
        pools.add(new PoolInfo(poolAccountId, poolAccountId, "Solo-Miners", "", "N/A",
                               assignmentLookup.get(poolAccountId).size(),
                               countLookup.get(SOLO_KEY), assignmentLookup.get(poolAccountId).size(), formatAmountNQT(rewardLookup.get(SOLO_KEY))));

      }
      else
      {
        Account account = accountLookup.get(poolAccountId);

        Integer minedBlocks = 0;
        int numberOfPoolBlockFinder = 0;
        Long earnedReward = 0L;
        for(String poolMinerAccountId : assignmentLookup.get(poolAccountId))
        {
          if(countLookup.containsKey(poolMinerAccountId))
          {
            numberOfPoolBlockFinder += 1;
            minedBlocks += countLookup.get(poolMinerAccountId);
            earnedReward += rewardLookup.get(poolMinerAccountId);
          }
        }
        pools.add(new PoolInfo(poolAccountId, account.getAccountRS(), account.getName(), account.getDescription(),
                               formatAmountNQT(Long.valueOf(account.getBalanceNQT())),
                               assignmentLookup.get(poolAccountId).size(),
                               minedBlocks, numberOfPoolBlockFinder, formatAmountNQT(earnedReward)));
      }
    }

    Collections.sort(pools, new Comparator<PoolInfo>()
    {
      @Override
      public int compare(PoolInfo o1, PoolInfo o2)
      {
        return Integer.compare( o2.getFoundBlocks(), o1.getFoundBlocks());
      }
    });

    publisher.publishEvent(new PoolInfoUpdateEvent(pools));
  }

  private String formatAmountNQT(Long amount)
  {
    String amountStr = String.valueOf(amount);
    return amount != null && amountStr.length() > 8 ? amountStr.substring(0, amountStr.length() - 8) : "0";
  }

  private List<Block> getBlocks(int days)
  {
    List<Block> allBlocks = new ArrayList<>();
    BlockchainStatus blockchainStatus = getBlockchainStatus();

    if(blockchainStatus != null)
    {
      int limit = 360 * days;
      int offset = 0;
      // getBlocks, max. 100 per request
      if(offset + limit > blockchainStatus.getNumberOfBlocks())
      {
        limit = blockchainStatus.getNumberOfBlocks() - offset;
      }
      final int steps = limit / 100;
      final int lastStepLimit = limit % 100;
      for(int step = 0; step <= steps; step++)
      {
        allBlocks.addAll(getBlocks(offset + step * 100, offset + step * 100 + (step == steps ? lastStepLimit : 100)));
      }

      LOG.info("Blocks form " + allBlocks.get(allBlocks.size() - 1).getHeight() + " to " + allBlocks.get(0).getHeight() + " received.");
    }
    return allBlocks;
  }

  private Account getAccount(String accountId)
  {
    Account result = null;
    try
    {
      ContentResponse response;
      response = httpClient.newRequest(ObserverProperties.getWalletUrl() + "/burst?requestType=getAccount&account=" + accountId)
        .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
        .send();

      result = objectMapper.readValue(response.getContentAsString(), Account.class);
    }
    catch(TimeoutException timeoutException)
    {
      LOG.warn("Unable to get account caused by connectionTimeout, currently '" + (ObserverProperties.getConnectionTimeout() / 1000)
               + " sec.' try increasing it!");
    }
    catch(Exception e)
    {
      LOG.trace("Unable to get mining info from wallet: " + e.getMessage());
    }
    return result;
  }

  private RewardRecipient getRewardRecipient(String account)
  {
    RewardRecipient result = null;
    try
    {
      ContentResponse response;
      response = httpClient.newRequest(ObserverProperties.getWalletUrl() + "/burst?requestType=getRewardRecipient&account=" + account)
        .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
        .send();

      result = objectMapper.readValue(response.getContentAsString(), RewardRecipient.class);
    }
    catch(TimeoutException timeoutException)
    {
      LOG.warn("Unable to get reward recipient caused by connectionTimeout, currently '" + (ObserverProperties.getConnectionTimeout() / 1000)
               + " sec.' try increasing it!");
    }
    catch(Exception e)
    {
      LOG.trace("Unable to get mining info from wallet: " + e.getMessage());
    }
    return result;
  }

  private Set<String> getAccountsWithRewardRecipient(String poolAccount)
  {
    AccountIds result = null;
    try
    {
      ContentResponse response;
      response = httpClient.newRequest(ObserverProperties.getWalletUrl() + "/burst?requestType=getAccountsWithRewardRecipient&account=" + poolAccount)
        .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
        .send();

      result = objectMapper.readValue(response.getContentAsString(), AccountIds.class);
    }
    catch(TimeoutException timeoutException)
    {
      LOG.warn("Unable to get accounts with reward recipient caused by connectionTimeout, currently '" + (ObserverProperties.getConnectionTimeout() / 1000)
               + " sec.' try increasing it!");
    }
    catch(Exception e)
    {
      LOG.trace("Unable to get mining info from wallet: " + e.getMessage());
    }
    return result != null ? result.getAccounts() : new HashSet<>();
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
      LOG.trace("Unable to get mining info from wallet: " + e.getMessage());
    }
    return result;
  }

  private List<Block> getBlocks(int firstIndex, int lastIndex)
  {
    Blocks result = null;
    try
    {
      ContentResponse response;
      response = httpClient.newRequest(ObserverProperties.getWalletUrl()
                                       + "/burst?requestType=getBlocks"
                                       + "&firstIndex=" + firstIndex
                                       + "&lastIndex=" + lastIndex
                                       + "&includeTransactions=false")
        .timeout(ObserverProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
        .send();

      result = objectMapper.readValue(response.getContentAsString(), Blocks.class);
    }
    catch(TimeoutException timeoutException)
    {
      LOG.warn("Unable to get blockchain status caused by connectionTimeout, currently '" + (ObserverProperties.getConnectionTimeout() / 1000)
               + " sec.' try increasing it!");
    }
    catch(Exception e)
    {
      LOG.trace("Unable to get mining info from wallet (maybe devV2): " + e.getMessage());
    }
    return result != null ? result.getBlocks() : new ArrayList<>();
  }
}
