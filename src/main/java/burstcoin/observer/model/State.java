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

package burstcoin.observer.model;

public class State
  extends IsResponse
{
  protected Integer numberOfPeers;
  protected Integer numberOfUnlockedAccounts;
  protected Integer numberOfTransfers;
  protected Integer numberOfOrders;
  protected Long numberOfTransactions;
  protected Long maxMemory;
  protected Boolean isScanning;
  protected String cumulativeDifficulty;
  protected Integer numberOfAssets;
  protected Long freeMemory;
  protected Integer availableProcessors;
  protected Long totalEffectiveBalanceNXT;
  protected Integer numberOfAccounts;
  protected Long numberOfBlocks;
  protected String version;
  protected Integer numberOfBidOrders;
  protected String lastBlock;
  protected Long totalMemory;
  protected String application;
  protected Integer numberOfAliases;
  protected Long lastBlockchainFeederHeight;
  protected Integer numberOfTrades;
  protected Long time;
  protected Integer numberOfAskOrders;
  protected String lastBlockchainFeeder;

  public State()
  {
  }

  public Integer getNumberOfPeers()
  {
    return numberOfPeers;
  }

  public Integer getNumberOfUnlockedAccounts()
  {
    return numberOfUnlockedAccounts;
  }

  public Integer getNumberOfTransfers()
  {
    return numberOfTransfers;
  }

  public Integer getNumberOfOrders()
  {
    return numberOfOrders;
  }

  public Long getNumberOfTransactions()
  {
    return numberOfTransactions;
  }

  public Long getMaxMemory()
  {
    return maxMemory;
  }

  public Boolean getIsScanning()
  {
    return isScanning;
  }

  public String getCumulativeDifficulty()
  {
    return cumulativeDifficulty;
  }

  public Integer getNumberOfAssets()
  {
    return numberOfAssets;
  }

  public Long getFreeMemory()
  {
    return freeMemory;
  }

  public Integer getAvailableProcessors()
  {
    return availableProcessors;
  }

  public Long getTotalEffectiveBalanceNXT()
  {
    return totalEffectiveBalanceNXT;
  }

  public Integer getNumberOfAccounts()
  {
    return numberOfAccounts;
  }

  public Long getNumberOfBlocks()
  {
    return numberOfBlocks;
  }

  public String getVersion()
  {
    return version;
  }

  public Integer getNumberOfBidOrders()
  {
    return numberOfBidOrders;
  }

  public String getLastBlock()
  {
    return lastBlock;
  }

  public Long getTotalMemory()
  {
    return totalMemory;
  }

  public String getApplication()
  {
    return application;
  }

  public Integer getNumberOfAliases()
  {
    return numberOfAliases;
  }

  public Long getLastBlockchainFeederHeight()
  {
    return lastBlockchainFeederHeight;
  }

  public Integer getNumberOfTrades()
  {
    return numberOfTrades;
  }

  public Long getTime()
  {
    return time;
  }

  public Integer getNumberOfAskOrders()
  {
    return numberOfAskOrders;
  }

  public String getLastBlockchainFeeder()
  {
    return lastBlockchainFeeder;
  }
}
