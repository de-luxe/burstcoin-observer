package burstcoin.observer.bean;

import java.util.List;

public class AssetCandleStickBean
{
  private String asset;
  private List candleStickData;

  public AssetCandleStickBean(String asset, List candleStickData)
  {
    this.asset = asset;
    this.candleStickData = candleStickData;
  }

  public String getAsset()
  {
    return asset;
  }

  public void setCandleStickData(List candleStickData)
  {
    this.candleStickData = candleStickData;
  }

  public List getCandleStickData()
  {
    return candleStickData;
  }
}
