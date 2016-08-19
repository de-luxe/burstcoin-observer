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

package burstcoin.observer.service.model;

import java.util.List;

public class Block
{
  protected String previousBlockHash;
  protected int payloadLength;
  protected String totalAmountNQT;
  protected String generationSignature;
  protected String generator;
  protected String generatorPublicKey;
  protected String baseTarget;
  protected String payloadHash;
  protected String generatorRS;
  protected String blockReward;
  protected String nextBlock;
  protected int scoopNum;
  protected int numberOfTransactions;
  protected String blockSignature;
  protected List<String> transactions;
  protected String nonce;
  protected int version;
  protected String totalFeeNQT;
  protected String previousBlock;
  protected String block;
  protected long height;
  protected long timestamp;

  public Block()
  {
  }

  public String getPreviousBlockHash()
  {
    return previousBlockHash;
  }

  public int getPayloadLength()
  {
    return payloadLength;
  }

  public String getTotalAmountNQT()
  {
    return totalAmountNQT;
  }

  public String getGenerationSignature()
  {
    return generationSignature;
  }

  public String getGenerator()
  {
    return generator;
  }

  public String getGeneratorPublicKey()
  {
    return generatorPublicKey;
  }

  public String getBaseTarget()
  {
    return baseTarget;
  }

  public String getPayloadHash()
  {
    return payloadHash;
  }

  public String getGeneratorRS()
  {
    return generatorRS;
  }

  public String getBlockReward()
  {
    return blockReward;
  }

  public String getNextBlock()
  {
    return nextBlock;
  }

  public int getScoopNum()
  {
    return scoopNum;
  }

  public int getNumberOfTransactions()
  {
    return numberOfTransactions;
  }

  public String getBlockSignature()
  {
    return blockSignature;
  }

  public List<String> getTransactions()
  {
    return transactions;
  }

  public String getNonce()
  {
    return nonce;
  }

  public int getVersion()
  {
    return version;
  }

  public String getTotalFeeNQT()
  {
    return totalFeeNQT;
  }

  public String getPreviousBlock()
  {
    return previousBlock;
  }

  public String getBlock()
  {
    return block;
  }

  public long getHeight()
  {
    return height;
  }

  public long getTimestamp()
  {
    return timestamp;
  }
}
