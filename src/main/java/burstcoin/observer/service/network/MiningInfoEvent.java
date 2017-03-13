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
