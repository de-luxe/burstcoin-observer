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

package burstcoin.observer.bean;

public class PoolBean
{
  private String accountId;
  private String accountRS;
  private String name;
  private String description;
  // pool balance
  private String balance;
  // accounts with reward assignment
  private Integer assignedMiners;
  // number of miners that found a block
  private int successfulMiners;
  // number of blocks found
  private int foundBlocks;

  private String earnedAmount;


  protected PoolBean()
  {
  }

  public PoolBean(String accountId, String accountRS, String name, String description, String balance, int assignedMiners, int foundBlocks,
                  int successfulMiners, String earnedAmount)
  {
    this.accountId = accountId;
    this.accountRS = accountRS;
    this.name = name;
    this.description = description;
    this.balance = balance;
    this.assignedMiners = assignedMiners;
    this.foundBlocks = foundBlocks;
    this.successfulMiners = successfulMiners;
    this.earnedAmount = earnedAmount;
  }

  public String getAccountId()
  {
    return accountId;
  }

  public String getAccountRS()
  {
    return accountRS;
  }

  public String getName()
  {
    return name;
  }

  public String getDescription()
  {
    return description;
  }

  public String getBalance()
  {
    return balance;
  }

  public Integer getAssignedMiners()
  {
    return assignedMiners;
  }

  public int getSuccessfulMiners()
  {
    return successfulMiners;
  }

  public int getFoundBlocks()
  {
    return foundBlocks;
  }

  public String getEarnedAmount()
  {
    return earnedAmount;
  }
}
