package net.es.nsi.dds.yaml.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hacksaw
 */
@lombok.Builder
@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DeviceType {
  private String nsaId;

  private String name;

  private String hostname;

  private String software;

  private LocationType location;

  @lombok.Builder.Default
  private boolean forced_add = false;

  @lombok.Builder.Default
  private Map<String, ServiceType> services = new HashMap<>();
}
