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

package burstcoin.observer.bean;

public class NetworkBean
{
  private String height;
  private String domain;
  private String url;
  private String baseTarget;
  private String generationSignature;
  private String targetDeadline;
  private String type;
  private String https;

  private NetworkState state;

  private Boolean available;

  public NetworkBean(String domain)
  {
    this.domain = domain;
    this.type = "N/A";
    this.height = "";
    this.baseTarget = "";
    this.generationSignature = "";
    this.targetDeadline = "";
    this.available = false;
  }

  public NetworkBean(String height, String domain, String url, String baseTarget, String generationSignature, String targetDeadline, String https)
  {
    this.height = height;
    this.domain = domain;
    this.url = url;
    this.baseTarget = baseTarget;
    this.generationSignature = generationSignature;
    this.https = https;
    this.state = NetworkState.OK;
    this.available = true;

    // week impl. wallet with pool in domain will show up as pool
    this.type = targetDeadline.equals("0") ? domain.contains("faucet") ? "Faucet" : domain.contains("pool") ? "Pool" : "Wallet" : "Pool";
    if(domain.contains("neon"))
    {
      this.type = "Pool";
    }
    this.targetDeadline = targetDeadline.equals("0") ? "Pool".equals(type) ? "Unlimited" : "N/A" : targetDeadline;
  }

  public String getHeight()
  {
    return height;
  }

  public String getDomain()
  {
    return domain;
  }

  public String getBaseTarget()
  {
    return baseTarget;
  }

  public String getGenerationSignature()
  {
    return generationSignature;
  }

  public String getTargetDeadline()
  {
    return targetDeadline;
  }

  public String getType()
  {
    return type;
  }

  public NetworkState getState()
  {
    return state;
  }

  public void setState(NetworkState state)
  {
    this.state = state;
  }

  public String getUrl()
  {
    return url;
  }

  public String getHttps()
  {
    return https;
  }

  public Boolean getAvailable()
  {
    return available;
  }
}
