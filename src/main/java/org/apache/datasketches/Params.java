package org.apache.datasketches;

public class Params {
  public static final String LG_K = "lgK";
  public static final String LG_K_DEFAULT = "12";

  public static final String OFFHEAP = "offheap";
  public static final String OFFHEAP_DEFAULT = "false";

  public static final String TGT_HLL_TYPE = "tgtHllType";
  public static final String TGT_HLL_TYPE_DEFAULT = "HLL_6";

  public static final String LG_MIN_U = "lgMinU";
  public static final String LG_MIN_U_DEFAULT = "0";

  public static final String LG_MAX_U = "lgMaxU";
  public static final String LG_MAX_U_DEFAULT = "23";

  public static final String U_PPO = "uPPO";
  public static final String U_PPO_DEFAULT = "16";

  public static final String LG_MIN_T = "lgMinT";
  public static final String LG_MIN_T_DEFAULT = "4";

  public static final String LG_MAX_T = "lgMaxT";
  public static final String LG_MAX_T_DEFAULT = "24";

  public static final String LG_MIN_BP_U = "lgMinBpU";
  public static final String LG_MIN_BP_U_DEFAULT = "4";

  public static final String LG_MAX_BP_U = "lgMaxBpU";
  public static final String LG_MAX_BP_U_DEFAULT = "20";

  public static final String V_IN = "vIn";
  public static final String V_IN_DEFAULT = "0";

  public static final String UNIQUES = "uniques";
  public static final String UNIQUES_DEFAULT = "1";

  public static final String TRIALS = "trials";
  public static final String TRIALS_DEFAULT = "1";

  public static final String[] parseSystemPropertyParams(final String propertyName, final String defaultValue) {
    return System.getProperty(propertyName, defaultValue).split(",");
  }
}
