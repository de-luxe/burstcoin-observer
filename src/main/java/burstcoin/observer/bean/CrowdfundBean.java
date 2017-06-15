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


public class CrowdfundBean
{
  private String at;
  private String atRS;
  private String creatorRS;
  private double percent;

  private String name;
  private String description;
  private String ratio;

  private String state;

  private String targetAmount;
  private String currentAmount;

  // remaining / finished ago
  private String blocksAgo;
  private String blocksToGo;

  public CrowdfundBean(String at, String atRS, String creatorRS, String name, String description, CrowdfundState state, String targetAmount,
                       String currentAmount,
                       String ratio, double percent, String blocks)
  {
    this.at = at;
    this.atRS = atRS;
    this.creatorRS = creatorRS;
    this.percent = percent;
    this.name = name;
    this.description = description;
    this.ratio = ratio;

    switch(state)
    {
      case ACTIVE:
        this.state = "Active";
        break;
      case FUNDED:
        this.state = "Funded";
        break;
      case NOT_FUNDED:
        this.state = "Not Funded";
        break;
    }

    this.targetAmount = targetAmount;
    this.currentAmount = currentAmount;
    this.blocksAgo = CrowdfundState.ACTIVE.equals(state) ? "0" : blocks;
    this.blocksToGo = CrowdfundState.ACTIVE.equals(state) ? blocks : "0";
  }

  public String getAt()
  {
    return at;
  }

  public String getAtRS()
  {
    return atRS;
  }

  public String getCreatorRS()
  {
    return creatorRS;
  }

  public String getName()
  {
    return name.length() > 22 ? name.substring(0, 19) + "..." : name;
  }

  public String getFullName()
  {
    return name;
  }

  public String getDescription()
  {
    return description;
  }

  public String getState()
  {
    return state;
  }

  public String getTargetAmount()
  {
    return targetAmount;
  }

  public String getCurrentAmount()
  {
    return currentAmount;
  }

  public String getBlocksAgo()
  {
    return blocksAgo;
  }

  public String getBlocksToGo()
  {
    return blocksToGo;
  }

  public String getRatio()
  {
    return ratio;
  }

  public double getPercent()
  {
    return percent;
  }
}
