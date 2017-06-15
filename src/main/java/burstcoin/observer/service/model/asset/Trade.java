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

package burstcoin.observer.service.model.asset;

import java.io.Serializable;


public class Trade
  implements Serializable
{
  private String asset;
  private String block;

  private String seller;
  private String quantityQNT;
  private String bidOrder;
  private String sellerRS;
  private String buyer;
  private String priceNQT;
  private String askOrder;
  private String buyerRS;
  private int decimals;
  private String name;
  private String askOrderHeight;
  private String bidOrderHeight;
  private String tradeType;
  private long timestamp;
  private long height;

  protected Trade()
  {
  }

  public Trade(String seller, String quantityQNT, String bidOrder, String sellerRS, String buyer, String priceNQT, String askOrder, String buyerRS,
               int decimals, String name, String block, String asset, String askOrderHeight, String bidOrderHeight, String tradeType, long timestamp,
               long height)
  {
    this.seller = seller;
    this.quantityQNT = quantityQNT;
    this.bidOrder = bidOrder;
    this.sellerRS = sellerRS;
    this.buyer = buyer;
    this.priceNQT = priceNQT;
    this.askOrder = askOrder;
    this.buyerRS = buyerRS;
    this.decimals = decimals;
    this.name = name;
    this.block = block;
    this.asset = asset;
    this.askOrderHeight = askOrderHeight;
    this.bidOrderHeight = bidOrderHeight;
    this.tradeType = tradeType;
    this.timestamp = timestamp;
    this.height = height;
  }

  public String getSeller()
  {
    return seller;
  }

  public String getQuantityQNT()
  {
    return quantityQNT;
  }

  public String getBidOrder()
  {
    return bidOrder;
  }

  public String getSellerRS()
  {
    return sellerRS;
  }

  public String getBuyer()
  {
    return buyer;
  }

  public String getPriceNQT()
  {
    return priceNQT;
  }

  public String getAskOrder()
  {
    return askOrder;
  }

  public String getBuyerRS()
  {
    return buyerRS;
  }

  public int getDecimals()
  {
    return decimals;
  }

  public String getName()
  {
    return name;
  }

  public String getBlock()
  {
    return block;
  }

  public String getAsset()
  {
    return asset;
  }

  public String getAskOrderHeight()
  {
    return askOrderHeight;
  }

  public String getBidOrderHeight()
  {
    return bidOrderHeight;
  }

  public String getTradeType()
  {
    return tradeType;
  }

  public long getTimestamp()
  {
    return timestamp;
  }

  public long getHeight()
  {
    return height;
  }


}
