package net.es.nsi.dds.yaml.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import net.es.nsi.common.constants.Nsi;
import net.es.nsi.common.jaxb.NsaParser;
import net.es.nsi.common.jaxb.nsa.InterfaceType;
import net.es.nsi.common.jaxb.nsa.NsaType;
import net.es.nsi.common.util.Decoder;
import net.es.nsi.dds.lib.client.DocumentsResult;
import net.es.nsi.dds.lib.jaxb.dds.DocumentType;
import net.es.nsi.dds.yaml.dao.DeviceType;
import net.es.nsi.dds.yaml.dao.Error;
import net.es.nsi.dds.yaml.dao.LocationType;
import net.es.nsi.dds.yaml.dao.NagiosMessage;
import net.es.nsi.dds.yaml.dao.NsaEnum;
import net.es.nsi.dds.yaml.dao.NsiProperties;
import net.es.nsi.dds.yaml.dao.ServiceType;
import net.es.nsi.dds.yaml.dao.ServiceType.ServiceTypeBuilder;
import net.es.nsi.dds.yaml.dao.YamlProperties;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@SwaggerDefinition(
        basePath = "/api",
        info = @Info(
                description = "This API provides access to programmable NSI-DDS Services features.",
                version = "v1",
                title = "NSI-DDS Services API",
                termsOfService = "Copyright (c) 2016, The Regents of the University "
                    + "of California, through Lawrence Berkeley National Laboratory "
                    + "(subject to receipt of any required approvals from the U.S. Dept. of "
                    + "Energy).  All rights reserved.",
                contact = @Contact(
                        name = "SEG Development Team",
                        email = "seg@es.net",
                        url = "https://github.com/esnet/sense-rm"),
                license = @License(
                        name = "Lawrence Berkeley National Labs BSD variant license",
                        url = "https://spdx.org/licenses/BSD-3-Clause-LBNL.html"))
)

/**
 *
 * @author hacksaw
 */
@RestController
@RequestMapping("/api")
@Api(tags = "Services API")
public class Controller extends ControllerErrorHandling {

  private final Logger LOG = LogManager.getLogger(getClass());

  @Autowired
  private NsiProperties nsiProperties;

  @Autowired
  private YamlProperties yamlProperties;

  @Autowired
  private DdsClientProvider client;

  /**
   * Ping - return a 200 OK if requesting entity has been properly authenticated.
   *
   * @return
   */
  @ApiOperation(
          value = "Simple healthcheck query that will return a 200 OK.",
          notes = "Returns Nagios compliant return code.")
  @ApiResponses(
          value = {
            @ApiResponse(
                    code = HttpConstants.OK_CODE,
                    message = HttpConstants.OK_MSG),
            @ApiResponse(
                    code = HttpConstants.UNAUTHORIZED_CODE,
                    message = HttpConstants.UNAUTHORIZED_MSG,
                    response = Error.class),})
  @RequestMapping(value = "/healthcheck", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseBody
  public ResponseEntity<?> healthcheck() {
    // We will populate some HTTP response headers.
    final HttpHeaders headers = new HttpHeaders();

    try {
      // We need the request URL to build fully qualified resource URLs.
      final URI location;
      try {
        location = ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri();
      } catch (Exception ex) {
        LOG.error("[Controller] Exception caught in GET of /", ex);
        NagiosMessage error = NagiosMessage.builder().returncode(NagiosMessage.CRITICAL).message("Status: CRITICAL, internal service error " + ex.getLocalizedMessage()).build();
        return new ResponseEntity<>(error, headers, HttpStatus.INTERNAL_SERVER_ERROR);
      }

      headers.add(HttpHeaders.CONTENT_LOCATION, location.toASCIIString());

      LOG.info("[Controller] GET operation = {}", location);

      // We have only one dependency and that is our NSI-DDS server.
      // Retrieve all the NSA documents from our configured NSI-DDS service.
      DocumentsResult ping = client.get().getPing(nsiProperties.getDdsUrl());

      NagiosMessage message;
      if (ping.getStatus() == Response.Status.OK) {
        message = NagiosMessage.builder().returncode(NagiosMessage.OK).message("Status: OK, functioning normally").build();
      } else {
        message = NagiosMessage.builder().returncode(NagiosMessage.CRITICAL).message("Status: CRITICAL, failed NSI-DDS service " + nsiProperties.getDdsUrl()).build();
      }
      return new ResponseEntity<>(message, headers, HttpStatus.OK);
    } catch (SecurityException ex) {
      LOG.error("[Controller] Exception caught", ex);
      NagiosMessage error = NagiosMessage.builder().returncode(NagiosMessage.CRITICAL).message("Status: CRITICAL, internal service error " + ex.getLocalizedMessage()).build();
      return new ResponseEntity<>(error, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * yaml - return a 200 OK if requesting entity has been properly authenticated.
   *
   * @return
   */
  @ApiOperation(
          value = "Return 200 OK and YAML file containing LibreNMS configuration from NSI-DDS information.",
          notes = "Return YAML file containing LibreNMS configuration from NSI-DDS information.")
  @ApiResponses(
          value = {
            @ApiResponse(
                    code = HttpConstants.OK_CODE,
                    message = HttpConstants.OK_MSG,
                    response = String.class)
            ,
            @ApiResponse(
                    code = HttpConstants.UNAUTHORIZED_CODE,
                    message = HttpConstants.UNAUTHORIZED_MSG,
                    response = Error.class),})
  @RequestMapping(value = "/yaml", method = RequestMethod.GET, produces = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @ResponseBody
  public ResponseEntity<?> yaml() {

    // We will populate some HTTP response headers.
    final HttpHeaders headers = new HttpHeaders();
    try {
      // We need the request URL to build fully qualified resource URLs.
      final URI location;
      try {
        location = ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri();
      } catch (Exception ex) {
        LOG.error("[Controller] Exception caught in GET of /", ex);
        Error error = Error.builder()
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .error_description(ex.getMessage())
                .build();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
      }

      headers.add(HttpHeaders.CONTENT_LOCATION, location.toASCIIString());

      LOG.info("[Controller] GET operation = {}", location);

      // Retrieve all the NSA documents from our configured NSI-DDS service.
      DocumentsResult documentsByType = client.get().getDocumentsByType(nsiProperties.getDdsUrl(), Nsi.NSI_DOC_TYPE_NSA_V1);

      if (documentsByType.getStatus() != Response.Status.OK) {
        LOG.error("[Controller] Error retrieving NSI-DDS documents, status = {}", documentsByType.getStatus());
        Error error = Error.builder()
              .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
              .error_description(documentsByType.getStatus().getReasonPhrase())
              .build();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return new ResponseEntity<>(error, headers, HttpStatus.INTERNAL_SERVER_ERROR);
      }

      // Convert the NSI-DDS documents to a list of devices for YAML conversion.
      Map<String, DeviceType> devices = getDevices(documentsByType);

      // Now wrap our devices in a Map for output.
      Map<String, Map<String, DeviceType>> outMap = new HashMap<>();
      outMap.put("devices", devices);

      // Initialize the Jackson processor to ourpur YAML.
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER));
      mapper.findAndRegisterModules();
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      String output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(outMap);

      LOG.debug("\n" + output);

      headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
      return new ResponseEntity<>(output, headers, HttpStatus.OK);
    } catch (IOException | JAXBException | SecurityException ex) {
      LOG.error("[Controller] Exception caught", ex);
      Error error = Error.builder()
              .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
              .error_description(ex.getMessage())
              .build();
      headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
      return new ResponseEntity<>(error, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Convert the NSA JAXB documents to out output devices.
   *
   * @param documents
   * @return
   */
  private Map<String, DeviceType> getDevices(DocumentsResult documents)
          throws IOException, JAXBException, SecurityException {

    // Populate our devices for YAML export.
    Map<String, DeviceType> devices = new HashMap<>();

    // Iterate over each NSA document and build a device + service profile for LibreNMS.
    for (DocumentType document : documents.getDocuments()) {
      // Decode the NSI-DDS entry for the NSA document (it is in base64/gzip encoded.
      InputStream decode = Decoder.decode(document.getContent().getContentTransferEncoding(),
              document.getContent().getContentType(), document.getContent().getValue());

      // Read the string document into a JAXB structure for access.
      NsaType nsa = NsaParser.getInstance().readDocument(decode);

      LOG.info("[Controller] processing {}", nsa.getId());

      // Skip tthis NSA if it is on the ignore list.
      if (yamlProperties.getIgnore().contains(nsa.getId())) {
        LOG.debug("[Controller] ignoring {}\n\n", nsa.getId());
        continue;
      }

      // Make a new device and populate the base information.
      DeviceType device = new DeviceType();

      // Striaght mapping from NSA document fields.
      device.setNsaId(nsa.getId());
      device.setName(nsa.getName());

      // Determine the software type of NSA to determine the way we will monitor it.
      NsaEnum software = NsaEnum.get(nsa.getSoftwareVersion());
      device.setSoftware(software.toString());

      // Find the host name for this NSA.
      device.setHostname(getHostname(nsa.getInterface()));

      // If location is present in the NSA document
      if (nsa.getLocation() != null) {
        LocationType loc = LocationType.builder()
                .name(nsa.getLocation().getName())
                .unlocode(nsa.getLocation().getUnlocode())
                .longitude(nsa.getLocation().getLongitude())
                .latitude(nsa.getLocation().getLatitude())
                .altitude(nsa.getLocation().getAltitude())
                .build();
        device.setLocation(loc);
      }

      // Set the forced add option if this NSA is identified in configuration.
      if (yamlProperties.getForce_add().contains(nsa.getId())) {
        device.setForced_add(true);
      } else {
        // We need to add rtt_and_pl ping service.
        ServiceType service = yamlProperties.getServices().get(ServiceType.RTT_AND_PL);
        if (service != null) {
          ServiceType newSrv = ServiceType.builder()
                  .name(service.getName())
                  .type(service.getType())
                  .hostname(device.getHostname())
                  .template(ServiceType.RTT_AND_PL)
                  .build();
          device.getServices().put(ServiceType.RTT_AND_PL, newSrv);
        }
      }

      // Add the certificate validation services.
      LinkedHashSet<String> hosts = new LinkedHashSet<>();
      for (InterfaceType i : nsa.getInterface()) {
        URL aURL = new URL(i.getHref());
        LOG.debug("[Controller] protocol {}, host {}, port {}, path {}",
                aURL.getProtocol(), aURL.getHost(), aURL.getPort(), aURL.getPath());
        if (aURL.getProtocol().equalsIgnoreCase("https")) {
          int port = aURL.getPort() > - 1 ? aURL.getPort() : 443;
          hosts.add(aURL.getHost() + ":" + port);
        }
      }

      for (String h : hosts) {
        List<String> splitByColon = Stream.of(h)
          .map(w -> w.split(":")).flatMap(Arrays::stream)
          .collect(Collectors.toList());
        String host = splitByColon.get(0);
        int port = Integer.parseInt(splitByColon.get(1));

        // We need to add valid_cert service.
        ServiceType service = yamlProperties.getServices().get(ServiceType.VALID_CERT);
        if (service != null) {
          ServiceType newSrv = ServiceType.builder()
                  .name(service.getName() + " " + h)
                  .type(service.getType())
                  .hostname(host)
                  .port(port)
                  .template(ServiceType.VALID_CERT)
                  .build();
          device.getServices().put(ServiceType.VALID_CERT + "_" + host.replace(".", "_") + "_" + port, newSrv);
        }
      }

      // Now add the Discovery service if needed.
      String url = yamlProperties.getDiscovery().get(nsa.getId());
      if (url != null) {
        LOG.debug("[Controller] found discovery URL {}", url);
        ServiceType service = yamlProperties.getServices().get(ServiceType.DISCOVERY);
        if (service != null) {
          URL aURL = new URL(url);
          ServiceTypeBuilder newSrv = ServiceType.builder()
                  .name(service.getName())
                  .type(service.getType())
                  .hostname(aURL.getHost())
                  .template(ServiceType.DISCOVERY)
                  .url(aURL.getPath());

          if (aURL.getProtocol().equalsIgnoreCase("https")) {
            newSrv = newSrv.port(aURL.getPort() > - 1 ? aURL.getPort() : 443).secure(true);
          } else {
            newSrv = newSrv.port(aURL.getPort() > - 1 ? aURL.getPort() : 80).secure(false);
          }
          device.getServices().put(ServiceType.DISCOVERY, newSrv.build());
        }
      }

      // Now we add interface specific service queries.
      if (software != NsaEnum.OPENNSA) {
        for (InterfaceType i : nsa.getInterface()) {
          URL aURL = new URL(i.getHref());
          LOG.debug("[Controller] interface {}, host {}, port {}, path {}",
                  aURL.getProtocol(), aURL.getHost(), aURL.getPort(), aURL.getPath());

          ServiceType service;
          String type;
          String urlSuffix = "";
          if (i.getType().equalsIgnoreCase(Nsi.NSI_CS_PROVIDER_V2)) {
            type = ServiceType.PROVIDER;
            service = yamlProperties.getServices().get(ServiceType.PROVIDER);
            urlSuffix = "?wsdl";
          } else if (i.getType().equalsIgnoreCase(Nsi.NSI_CS_REQUESTER_V2)) {
            type = ServiceType.REQUESTER;
            service = yamlProperties.getServices().get(ServiceType.REQUESTER);
            urlSuffix = "?wsdl";
          } else if (i.getType().equalsIgnoreCase(Nsi.NSI_DDS_V1_XML)) {
            type = ServiceType.DDS;
            service = yamlProperties.getServices().get(ServiceType.DDS);
            urlSuffix = "/ping";
          } else {
            continue;
          }

          if (service == null) {
            LOG.error("[Controller] no service templated defined for {}", type);
            continue;
          }

          ServiceTypeBuilder newSrv = ServiceType.builder()
                  .type(service.getType())
                  .name(service.getName())
                  .hostname(aURL.getHost())
                  .template(type)
                  .url(aURL.getPath() + urlSuffix);

          if (aURL.getProtocol().equalsIgnoreCase("https")) {
            newSrv = newSrv.port(aURL.getPort() > - 1 ? aURL.getPort() : 443).secure(true);
          } else {
            newSrv = newSrv.port(aURL.getPort() > - 1 ? aURL.getPort() : 80).secure(false);
          }

          device.getServices().put(type, newSrv.build());
        }
      }

      devices.put(nsa.getId(), device);

      LOG.debug("[Controller] \n\n");
    }

    return devices;
  }

  /**
   * An NSA does not in itself have a hostname associated with it, but has a list
   * of service interface endpoints composed of URL that point to host.  One problem
   * is that there could be interfaces on different hosts if a multi-server
   * deployment, or a multi-container deployment.  We are going count the interfaces
   * and use the one with the most endpoints.
   *
   * @param it
   * @return
   */
  private String getHostname(List<InterfaceType> it) {
    Map<String, Integer> count = new HashMap<>();

    // Iterate over each interface specified and count per hostname.
    for (InterfaceType i : it) {
      if (!Strings.isNullOrEmpty(i.getHref())) {
        try {
          URL aURL = new URL(i.getHref());
          if (count.containsKey(aURL.getHost())) {
            Integer newVal = count.get(aURL.getHost()) + 1;
            count.replace(aURL.getHost(), newVal);
          } else {
            count.put(aURL.getHost(), 1);
          }
        } catch (MalformedURLException ex) {
          LOG.error("[getHostname] invalid interface {}, {}", i.getType(), i.getHref());
        }
      }
    }

    Map<String, Integer> results = sortByValue(count);

    // Return the highest counted host name (if it is not numeric IP).
    String lastChance = null;
    for (String key : results.keySet()) {
      if (InetAddressUtils.isIPv4Address(key)|| InetAddressUtils.isIPv6Address(key)) {
        LOG.info("[getHostname] IP address ignoring {}", key);
        lastChance = key;
      } else {
        return key;
      }
    }

    return lastChance;
  }

  /**
   * Sort the provided map based on Integer values highest to lowest.
   *
   * @param map
   * @return
   */
  private static Map<String, Integer> sortByValue(Map<String, Integer> map)
  {
    // Create a list from elements of HashMap.
    List<Map.Entry<String, Integer>> list = new LinkedList<>(map.entrySet());

    // Sort the list
    Collections.sort(list, (Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)
            -> (o2.getValue()).compareTo(o1.getValue()));

    // put data from sorted list to hashmap
    Map<String, Integer> temp = new LinkedHashMap<>();
    list.forEach(aa -> {
      temp.put(aa.getKey(), aa.getValue());
    });
    return temp;
  }
}
