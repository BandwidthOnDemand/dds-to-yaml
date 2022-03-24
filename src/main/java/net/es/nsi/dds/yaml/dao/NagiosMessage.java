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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NagiosMessage {
  public static final int OK = 0;
  public static final int WARNING = 1;
  public static final int CRITICAL = 2;
  public static final int UNKNOWN = 3;

  private int returncode;
  private String message;
}
