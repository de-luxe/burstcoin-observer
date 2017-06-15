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

public class Asset
{

  private String asset;

  private String quantityQNT;
  private int numberOfAccounts;
  private String accountRS;
  private int decimals;
  private int numberOfTransfers;
  private String name;
  private String description;
  private int numberOfTrades;
  private String account;

  protected Asset()
  {
  }

  public Asset(String quantityQNT, int numberOfAccounts, String accountRS, int decimals, int numberOfTransfers, String name, String description,
               int numberOfTrades, String asset, String account)
  {
    this.quantityQNT = quantityQNT;
    this.numberOfAccounts = numberOfAccounts;
    this.accountRS = accountRS;
    this.decimals = decimals;
    this.numberOfTransfers = numberOfTransfers;
    this.name = name;
    this.description = description;
    this.numberOfTrades = numberOfTrades;
    this.asset = asset;
    this.account = account;
  }

  public String getQuantityQNT()
  {
    return quantityQNT;
  }

  public int getNumberOfAccounts()
  {
    return numberOfAccounts;
  }

  public String getAccountRS()
  {
    return accountRS;
  }

  public int getDecimals()
  {
    return decimals;
  }

  public int getNumberOfTransfers()
  {
    return numberOfTransfers;
  }

  public String getName()
  {
    return name;
  }

  public String getDescription()
  {
    return description;
  }

  public int getNumberOfTrades()
  {
    return numberOfTrades;
  }

  public String getAsset()
  {
    return asset;
  }

  public String getAccount()
  {
    return account;
  }

  @Override
  public boolean equals(Object o)
  {
    if(this == o)
    {
      return true;
    }
    if(!(o instanceof Asset))
    {
      return false;
    }

    Asset asset1 = (Asset) o;

    return asset.equals(asset1.asset);

  }

  @Override
  public int hashCode()
  {
    return asset.hashCode();
  }
}
