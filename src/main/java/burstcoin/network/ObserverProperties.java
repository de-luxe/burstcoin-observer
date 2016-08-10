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

package burstcoin.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
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

  private static String referenceWalletServer;
  private static String serverPort;
  private static List<String> compareWalletServers;
  private static Integer connectionTimeout;


  public static String getServerPort()
  {
    if(serverPort == null)
    {
      serverPort = asString("burstcoin.network.serverPort", "8080");
    }
    return serverPort;
  }

  public static String getReferenceWalletServer()
  {
    if(referenceWalletServer == null)
    {
      referenceWalletServer = asString("burstcoin.network.referenceWalletServer", "http://localhost:8125");
    }
    return referenceWalletServer;
  }

  public static List<String> getCompareWalletServers()
  {
    if(compareWalletServers == null)
    {
      compareWalletServers = asStringList("burstcoin.network.compareWalletServers", new ArrayList<>());
      if(compareWalletServers.isEmpty())
      {
        LOG.error("Error: property 'burstcoin.network.compareWalletServers' required!  ");
      }
    }

    return compareWalletServers;
  }

  public static long getConnectionTimeout()
  {
    if(connectionTimeout == null)
    {
      connectionTimeout = asInteger("connectionTimeout", 12000);
    }
    return connectionTimeout;
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

  private static String asString(String key, String defaultValue)
  {
    String value = PROPS.containsKey(key) ? String.valueOf(PROPS.getProperty(key)) : defaultValue;
    return StringUtils.isEmpty(value) ? defaultValue : value;
  }
}
