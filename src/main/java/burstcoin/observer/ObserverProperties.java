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

package burstcoin.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ObserverProperties
{
  private static final Logger LOG = LoggerFactory.getLogger(ObserverProperties.class);
  private static final String STRING_LIST_PROPERTY_DELIMITER = ",";
  private static final Properties PROPS = new Properties();

  static
  {
    try
    {
      PROPS.load(new FileInputStream(System.getProperty("user.dir") + "/observer.properties"));
    }
    catch(IOException e)
    {
      LOG.error(e.getMessage());
    }
  }

  private static URL observerUrl;
  private static String walletUrl;
  private static Integer networkRefreshInterval;
  private static Integer connectionTimeout;
  private static List<String> networkServers;

  private static Boolean enableProxy;
  private static Boolean useSocksProxy;
  private static Integer proxyPort;
  private static String proxyHost;

  public static boolean isEnableProxy()
  {
    if(enableProxy == null)
    {
      enableProxy = asBoolean("burstcoin.observer.enableProxy", false);
    }
    return enableProxy;
  }

  public static boolean isUseSocksProxy()
  {
    if(useSocksProxy == null)
    {
      useSocksProxy = asBoolean("burstcoin.observer.useSocksProxy", true);
    }
    return useSocksProxy;
  }

  public static Integer getProxyPort()
  {
    if(proxyPort == null)
    {
      proxyPort = asInteger("burstcoin.observer.proxyPort", 9050);
    }
    return proxyPort;
  }

  public static Integer getObserverPort()
  {
    if(observerUrl == null)
    {
      parseObserverUrl();
    }
    return observerUrl.getPort();
  }

  public static String getObserverUrl()
  {
    if(observerUrl == null)
    {
      parseObserverUrl();
    }
    return observerUrl.toString();
  }

  public static String getWalletUrl()
  {
    if(walletUrl == null)
    {
      walletUrl = asString("burstcoin.observer.walletUrl", "http://localhost:8125");
    }
    return walletUrl;
  }

  public static String getProxyHost()
  {
    if(proxyHost == null)
    {
      proxyHost = asString("burstcoin.observer.proxyHost", "127.0.0.1");
    }
    return proxyHost;
  }

  public static List<String> getNetworkServerUrls()
  {
    if(networkServers == null)
    {
      networkServers = asStringList("burstcoin.observer.network.serverUrls", new ArrayList<>());
      if(networkServers.isEmpty())
      {
        LOG.error("Error: property 'burstcoin.observer.network.serverUrls' required!");
      }
    }
    return networkServers;
  }

  public static Integer getNetworkRefreshInterval()
  {
    if(networkRefreshInterval == null)
    {
      networkRefreshInterval = asInteger("burstcoin.observer.network.refreshInterval", 8000);
    }
    return networkRefreshInterval;
  }

  public static long getConnectionTimeout()
  {
    if(connectionTimeout == null)
    {
      connectionTimeout = asInteger("burstcoin.observer.connectionTimeout", 12000);
    }
    return connectionTimeout;
  }


  private static void parseObserverUrl()
  {
    String hostString = asString("burstcoin.observer.url", "http://localhost:1111");
    try
    {
      observerUrl = new URL(hostString);
    }
    catch(MalformedURLException e)
    {
      LOG.error("Could not parse 'burstcoin.observer.url' should be like 'http://host:port': " + e.getMessage());
    }
  }

  private static int asInteger(String key, int defaultValue)
  {
    String integerProperty = PROPS.containsKey(key) ? String.valueOf(PROPS.getProperty(key)) : null;
    Integer value = null;
    if(!StringUtils.isEmpty(integerProperty))
    {
      try
      {
        value = Integer.valueOf(integerProperty);
      }
      catch(NumberFormatException e)
      {
        LOG.error("value of property: '" + key + "' should be a numeric (int) value.");
      }
    }
    return value != null ? value : defaultValue;
  }

  private static List<String> asStringList(String key, List<String> defaultValue)
  {
    String stringListProperty = PROPS.containsKey(key) ? String.valueOf(PROPS.getProperty(key)) : null;
    List<String> value = null;
    if(!StringUtils.isEmpty(stringListProperty))
    {
      try
      {
        value = Arrays.asList(stringListProperty.trim().split(STRING_LIST_PROPERTY_DELIMITER));
      }
      catch(NullPointerException | NumberFormatException e)
      {
        LOG.error("property: '" + key + "' value should be 'string(s)' separated by '" + STRING_LIST_PROPERTY_DELIMITER + "' (comma).");
      }
    }

    return value != null ? value : defaultValue;
  }

  private static Boolean asBoolean(String key, boolean defaultValue)
  {
    String booleanProperty = PROPS.containsKey(key) ? String.valueOf(PROPS.getProperty(key)) : null;
    Boolean value = null;
    if(!StringUtils.isEmpty(booleanProperty))
    {
      try
      {
        value = Boolean.valueOf(booleanProperty);
      }
      catch(Exception e)
      {
        LOG.error("property: '" + key + "' value should be of type 'boolean' (e.g. 'true' or 'false').");
      }
    }
    return value != null ? value : defaultValue;
  }

  private static String asString(String key, String defaultValue)
  {
    String value = PROPS.containsKey(key) ? String.valueOf(PROPS.getProperty(key)) : defaultValue;
    return StringUtils.isEmpty(value) ? defaultValue : value;
  }
}
