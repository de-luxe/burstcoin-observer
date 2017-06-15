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

/**
 * http://ip-api.com
 * {
 * "status": "success",
 * "country": "United States",
 * "countryCode": "US",
 * "region": "CA",
 * "regionName": "California",
 * "city": "San Francisco",
 * "zip": "94105",
 * "lat": "37.7898",
 * "lon": "-122.3942",
 * "timezone": "America\/Los_Angeles",
 * "isp": "Wikimedia Foundation",
 * "org": "Wikimedia Foundation",
 * "as": "AS14907 Wikimedia US network",
 * "query": "208.80.152.201"
 * }
 */
public class PeerInfo
{
  protected String status;//": "success",
  protected String country;//": "United States",
  protected String countryCode;//": "US",
  protected String region;//": "CA",
  protected String regionName;//": "California",
  protected String city;//": "San Francisco",
  protected String zip;//": "94105",
  protected String lat;//": "37.7898",
  protected String lon;//": "-122.3942",
  protected String timezone;//": "America\/Los_Angeles",
  protected String isp;//": "Wikimedia Foundation",
  protected String org;//": "Wikimedia Foundation",
  protected String as;//": "AS14907 Wikimedia US network",
  protected String query;//": "208.80.152.201"

  public String getStatus()
  {
    return status;
  }

  public String getCountry()
  {
    return country;
  }

  public String getCountryCode()
  {
    return countryCode;
  }

  public String getRegion()
  {
    return region;
  }

  public String getRegionName()
  {
    return regionName;
  }

  public String getCity()
  {
    return city;
  }

  public String getZip()
  {
    return zip;
  }

  public String getLat()
  {
    return lat;
  }

  public String getLon()
  {
    return lon;
  }

  public String getTimezone()
  {
    return timezone;
  }

  public String getIsp()
  {
    return isp;
  }

  public String getOrg()
  {
    return org;
  }

  public String getAs()
  {
    return as;
  }

  public String getQuery()
  {
    return query;
  }
}
