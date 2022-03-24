package net.es.nsi.dds.yaml.dao;

import com.fasterxml.jackson.annotation.JsonRootName;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;
import net.es.nsi.dds.lib.dao.SecureType;
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
@ConfigurationProperties(prefix = "nsi")
@Validated
@JsonRootName(value = "nsi")
public class NsiProperties {
  private final Logger LOG = LogManager.getLogger(getClass());

  // The URL of the local container runtime.
  @NotBlank(message = "ddsUrl cannot be null or empty")
  private String ddsUrl;
  private String proxy;

  // Configuration for client.
  private ClientType client;

  // Configuration for TLS/SSL settings.
  private SecureType secure;

  @PostConstruct
  private void postConstruct() {
    LOG.debug("[NsiProperties] ddsUrl {}", ddsUrl);
    LOG.debug("[NsiProperties] proxy {}", proxy);
    LOG.debug("[NsiProperties] client {}", client);
    LOG.debug("[NsiProperties] secure {}", secure);
  }
}
