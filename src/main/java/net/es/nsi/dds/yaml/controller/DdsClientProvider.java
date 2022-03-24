package net.es.nsi.dds.yaml.controller;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.annotation.PostConstruct;
import net.es.nsi.dds.lib.client.DdsClient;
import net.es.nsi.dds.yaml.dao.ClientType;
import net.es.nsi.dds.yaml.dao.NsiProperties;
import net.es.nsi.dds.yaml.spring.SpringApplicationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author hacksaw
 */
@Component("ddsClientProvider")
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DdsClientProvider {
  private final Logger LOG = LogManager.getLogger(getClass());

  @Autowired
  private NsiProperties nsiProperties;

  private DdsClient client;

  public DdsClientProvider() {
  }

  @PostConstruct
  public void init() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
          KeyManagementException, UnrecoverableKeyException, NoSuchProviderException {
    // Make sure we have our euntile parameters.
    if (nsiProperties == null) {
      throw new KeyManagementException("reference to nsiProperties is null");
    }

    boolean secure = false;
    int maxcpr = ClientType.MAX_CONN_PER_ROUTE;
    int maxct = ClientType.MAX_CONN_TOTAL;
    if (nsiProperties.getClient() != null) {
      secure = nsiProperties.getClient().isSecure();
      maxcpr = nsiProperties.getClient().getMaxConnPerRoute();
      maxct = nsiProperties.getClient().getMaxConnTotal();
    }

    try {
      if (secure) {
        client = new DdsClient(maxcpr, maxct, nsiProperties.getSecure());
      } else {
        client = new DdsClient(maxcpr, maxct);
      }
    } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | KeyManagementException | UnrecoverableKeyException | NoSuchProviderException ex) {
      System.err.printf("[DdsClientProvider]: error %s", ex.getMessage());
      LOG.error("Exception caught initializing DdsClient()", ex);
      throw ex;
    }
  }

  public static DdsClientProvider getInstance() {
    DdsClientProvider instance = SpringApplicationContext.getBean("ddsClientProvider", DdsClientProvider.class);
    return instance;
  }

  public DdsClient get() {
    return client;
  }
}
