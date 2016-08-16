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

package burstcoin.observer.model.at;

public class AutomatedTransaction
{
  private String name;
  private String description;

  private String creator;
  private String creatorRS;

  private String at;
  private String atRS;

  private int atVersion;
  private boolean stopped;
  private String machineCode;
  private String machineData;
  private boolean frozen;
  private boolean finished;
  private boolean dead;
  private String balanceNQT;
  private long nextBlock;
  private int requestProcessingTime;
  private String minActivation;
  private String running;
  private String prevBalanceNQT;
  private long creationBlock;

  public AutomatedTransaction()
  {
  }

  public String getName()
  {
    return name;
  }

  public String getDescription()
  {
    return description;
  }

  public String getCreator()
  {
    return creator;
  }

  public String getCreatorRS()
  {
    return creatorRS;
  }

  public String getAt()
  {
    return at;
  }

  public String getAtRS()
  {
    return atRS;
  }

  public int getAtVersion()
  {
    return atVersion;
  }

  public boolean isStopped()
  {
    return stopped;
  }

  public String getMachineCode()
  {
    return machineCode;
  }

  public String getMachineData()
  {
    return machineData;
  }

  public boolean isFrozen()
  {
    return frozen;
  }

  public boolean isFinished()
  {
    return finished;
  }

  public boolean isDead()
  {
    return dead;
  }

  public String getBalanceNQT()
  {
    return balanceNQT;
  }

  public long getNextBlock()
  {
    return nextBlock;
  }

  public int getRequestProcessingTime()
  {
    return requestProcessingTime;
  }

  public String getMinActivation()
  {
    return minActivation;
  }

  public String getRunning()
  {
    return running;
  }

  public String getPrevBalanceNQT()
  {
    return prevBalanceNQT;
  }

  public long getCreationBlock()
  {
    return creationBlock;
  }
}
