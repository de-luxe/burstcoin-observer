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


import java.util.Date;

public class NodeListBean
{
  private String announcedAddress;
  private String ip;
  private String version;
  private String platform;

  private String country;
  private String region;
  private String city;

  private Date lastUpdate;
  private String updated;

  private String isp;

  public NodeListBean(Date lastUpdate, String announcedAddress, String ip, String version, String platform, String country, String region, String city, String isp)
  {
    this.lastUpdate = lastUpdate;
    this.announcedAddress = announcedAddress;
    this.ip = ip;
    this.version = version;
    this.platform = platform;
    this.country = country;
    this.region = region;
    this.city = city;
    this.isp = isp;
  }

  public String getUpdated()
  {
    return updated;
  }

  public void setUpdated(String updated)
  {
    this.updated = updated;
  }

  public Date getLastUpdate()
  {
    return lastUpdate;
  }

  public String getAnnouncedAddress()
  {
    return announcedAddress;
  }

  public String getIp()
  {
    return ip;
  }

  public String getVersion()
  {
    return version;
  }

  public String getPlatform()
  {
    return platform;
  }

  public String getCountry()
  {
    return country;
  }

  public String getRegion()
  {
    return region;
  }

  public String getCity()
  {
    return city;
  }

  public String getIsp()
  {
    return isp;
  }
}
