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

package burstcoin.observer.service.model.node;

import burstcoin.observer.service.model.IsResponse;

public class Peer
  extends IsResponse
{
  protected long lastUpdated; // 84020113,
  protected long downloadedVolume; //  11421691,
  protected boolean blacklisted; //  false,
  protected String announcedAddress; //  "burst.ninja",
  protected String application; //  "NRS",
  protected int weight; // 0,
  protected long uploadedVolume; //  29180702,
  protected int state; //  1,
  protected String version; //  "1.2.7",
  protected String platform; //  "FreeBSD",
  protected boolean shareAddress; //  true

  public long getLastUpdated()
  {
    return lastUpdated;
  }

  public long getDownloadedVolume()
  {
    return downloadedVolume;
  }

  public boolean isBlacklisted()
  {
    return blacklisted;
  }

  public String getAnnouncedAddress()
  {
    return announcedAddress;
  }

  public String getApplication()
  {
    return application;
  }

  public int getWeight()
  {
    return weight;
  }

  public long getUploadedVolume()
  {
    return uploadedVolume;
  }

  public int getState()
  {
    return state;
  }

  public String getVersion()
  {
    return version;
  }

  public String getPlatform()
  {
    return platform;
  }

  public boolean isShareAddress()
  {
    return shareAddress;
  }
}
