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

package burstcoin.observer.service.network;

import burstcoin.observer.service.model.MiningInfo;
import org.springframework.context.ApplicationEvent;

import java.io.Serializable;
import java.util.UUID;


public class MiningInfoEvent
  extends ApplicationEvent implements Serializable
{
  private MiningInfo miningInfo;
  private String networkServerUrl;
  private UUID uuid;

  public MiningInfoEvent(MiningInfo miningInfo, String networkServerUrl, UUID uuid)
  {
    super(networkServerUrl);
    this.miningInfo = miningInfo;
    this.networkServerUrl = networkServerUrl;
    this.uuid = uuid;
  }

  public MiningInfo getMiningInfo()
  {
    return miningInfo;
  }

  public String getNetworkServerUrl()
  {
    return networkServerUrl;
  }

  public UUID getUuid()
  {
    return uuid;
  }
}
