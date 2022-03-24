package net.es.nsi.dds.yaml.dao;

import com.fasterxml.jackson.annotation.JsonRootName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 *
 * @author hacksaw
 */
@lombok.Data
@lombok.NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "yaml")
@Validated
@JsonRootName(value = "yaml")
public class YamlProperties {
  private final Logger LOG = LogManager.getLogger(getClass());

  // List of NSA documents to ignore.
  private List<String> ignore = new ArrayList<>();

  // NSA discovery URL for the network.
  private Map<String, String> discovery = new HashMap<>();

  // A list of NSA id that need the force_add flags set.
  private List<String> force_add = new ArrayList<>();

  private Map<String, ServiceType> services = new HashMap<>();

  @PostConstruct
  private void postConstruct() {
    for (String key : ignore) {
      LOG.debug("[YamlProperties] ignore {}", key);
    }

    for (String key : force_add) {
      LOG.debug("[YamlProperties] force_add {}", key);
    }

    for (String key : discovery.keySet()) {
      LOG.debug("[YamlProperties] discovery {} = {}", key, discovery.get(key));
    }

    for (String key : services.keySet()) {
      LOG.debug("[YamlProperties] service {} = {}", key, services.get(key));
    }
  }
}
