package net.es.nsi.dds.yaml.dao;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 *
 * @author hacksaw
 */
@lombok.Builder
@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ServiceType {
  public static final String RTT_AND_PL = "rtt_and_pl";
  public static final String VALID_CERT = "valid_cert";
  public static final String DISCOVERY = "discovery";
  public static final String PROVIDER = "provider";
  public static final String REQUESTER = "requester";
  public static final String DDS = "dds";

  private String type;

  private String name;

  private String hostname;

  @lombok.Builder.Default
  private int port = 0;

  private String template;
  
  private String url;

  @lombok.Builder.Default
  private boolean secure = false;
}
