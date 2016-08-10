package burstcoin.network.observer.model;


public class InfoBean
{
  private String height;
  private String domain;
  private String baseTarget;
  private String generationSignature;
  private String targetDeadline;  // only if pool
  private String type;

  public InfoBean(String domain)
  {
    this.domain = domain;
    this.type = "N/A";
    this.height = "";
    this.baseTarget = "";
    this.generationSignature = "";
    this.targetDeadline = "";
  }

  public InfoBean(String height, String domain, String baseTarget, String generationSignature, String targetDeadline)
  {
    this.height = height;
    this.domain = domain;
    this.baseTarget = baseTarget;
    this.generationSignature = generationSignature;
    this.targetDeadline = targetDeadline;
    this.type = targetDeadline.equals("0") ? "Wallet" : "Pool";
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
}
