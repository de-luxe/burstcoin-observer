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

package burstcoin.observer.event;

import burstcoin.observer.bean.NetworkBean;

import java.util.Date;
import java.util.List;

public class NetworkUpdateEvent
{
  private Date lastUpdate;
  private List<NetworkBean> networkBeans;
  private Long lastBlockWithSameGenSig;
  private List<List> multiSenkeyData;

  public NetworkUpdateEvent(List<NetworkBean> networkBeans, Long lastBlockWithSameGenSig, List<List> multiSenkeyData)
  {
    this.lastBlockWithSameGenSig = lastBlockWithSameGenSig;
    this.multiSenkeyData = multiSenkeyData;
    this.lastUpdate = new Date();
    this.networkBeans = networkBeans;
  }

  public List<List> getMultiSenkeyData()
  {
    return multiSenkeyData;
  }

  public Date getLastUpdate()
  {
    return lastUpdate;
  }

  public List<NetworkBean> getNetworkBeans()
  {
    return networkBeans;
  }

  public Long getLastBlockWithSameGenSig()
  {
    return lastBlockWithSameGenSig;
  }
}
