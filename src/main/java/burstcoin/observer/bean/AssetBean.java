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


public class AssetBean
{
  private String asset;
  private String name;
  private String description;

  private String accountRS;
  private String account;

  private String quantityQNT;
  private int decimals;

  private int numberOfAccounts;
  private int numberOfTransfers;
  private int numberOfTrades;

  private int numberOfBuyOrders;
  private int numberOfSellOrders;
  private String volume7Days;
  private String volume30Days;

  private String lastPrice;

  public AssetBean(String asset, String name, String description, String accountRS, String account, String quantityQNT, int decimals, int numberOfAccounts,
                   int numberOfTransfers, int numberOfTrades, int numberOfBuyOrders, int numberOfSellOrders, String volume7Days, String volume30Days,
                   String lastPrice)
  {
    this.asset = asset;
    this.name = name;
    this.description = description;
    this.accountRS = accountRS;
    this.account = account;
    this.quantityQNT = quantityQNT;
    this.decimals = decimals;
    this.numberOfAccounts = numberOfAccounts;
    this.numberOfTransfers = numberOfTransfers;
    this.numberOfTrades = numberOfTrades;
    this.numberOfBuyOrders = numberOfBuyOrders;
    this.numberOfSellOrders = numberOfSellOrders;
    this.volume7Days = volume7Days.equals("") ? "0" : volume7Days;
    this.volume30Days = volume30Days.equals("") ? "0" : volume30Days;

    this.lastPrice = lastPrice;
  }

  public String getLastPrice()
  {
    return lastPrice;
  }

  public String getVolume7Days()
  {
    return volume7Days;
  }

  public String getVolume30Days()
  {
    return volume30Days;
  }

  public String getAsset()
  {
    return asset;
  }

  public String getName()
  {
    return name;
  }

  public String getDescription()
  {
    return description;
  }

  public String getAccountRS()
  {
    return accountRS;
  }

  public String getAccount()
  {
    return account;
  }

  public String getQuantityQNT()
  {
    return quantityQNT;
  }

  public int getDecimals()
  {
    return decimals;
  }

  public int getNumberOfAccounts()
  {
    return numberOfAccounts;
  }

  public int getNumberOfTransfers()
  {
    return numberOfTransfers;
  }

  public int getNumberOfTrades()
  {
    return numberOfTrades;
  }

  public int getNumberOfBuyOrders()
  {
    return numberOfBuyOrders;
  }

  public int getNumberOfSellOrders()
  {
    return numberOfSellOrders;
  }
}
