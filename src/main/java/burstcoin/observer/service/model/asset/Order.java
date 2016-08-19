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

package burstcoin.observer.service.model.asset;

import java.io.Serializable;

public class Order
  implements Serializable
{
  private String asset;
  private String order;
  private String type;

  private String quantityQNT;
  private String priceNQT;
  private String accountRS;
  private String account;
  private long height;

  protected Order()
  {
  }

  public Order(String asset, String order, String type, String quantityQNT, String priceNQT, String accountRS, String account, long height)
  {
    this.asset = asset;
    this.order = order;
    this.type = type;
    this.quantityQNT = quantityQNT;
    this.priceNQT = priceNQT;
    this.accountRS = accountRS;
    this.account = account;
    this.height = height;
  }

  public String getAsset()
  {
    return asset;
  }

  public String getOrder()
  {
    return order;
  }

  public String getType()
  {
    return type;
  }

  public String getQuantityQNT()
  {
    return quantityQNT;
  }

  public String getPriceNQT()
  {
    return priceNQT;
  }

  public String getAccountRS()
  {
    return accountRS;
  }

  public String getAccount()
  {
    return account;
  }

  public long getHeight()
  {
    return height;
  }
}
