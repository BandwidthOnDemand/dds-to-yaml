package net.es.nsi.dds.yaml.dao;

import com.google.common.base.Strings;

/**
 *
 * @author hacksaw
 */
public enum NsaEnum {
  OPENNSA("OpenNSA"), SAFNARI("nsi-safnari"), OSCARS("oscars"),
  SINETNSA("SinetNSA"), SINETAGGR("SinetAggr"), OTHER("other"),
  UNKNOWN("unknown");

  private String nsa;
  private NsaEnum(String nsa) {
      this.nsa = nsa;
  }

  @Override
  public String toString(){
      return nsa;
  }

  public static NsaEnum get(String version) {
    if (Strings.isNullOrEmpty(version)) {
      return UNKNOWN;
    } else if (version.contains("OpenNSA")) {
      return OPENNSA;
    } else if (version.contains("SNAPSHOT")) {
      return SAFNARI;
    } else if (version.contains("1.0.4")) {
      return OSCARS;
    } else if (version.contains("nsibridge-v1")) {
      return OSCARS;
    } else if (version.contains("undefined")) {
      return SINETNSA;
    }

    return OTHER;
  }
}
