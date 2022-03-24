package net.es.nsi.dds.yaml.controller;

import net.es.nsi.dds.yaml.dao.Error;
import com.google.common.base.Strings;
import java.util.Date;
import static javax.ws.rs.core.Response.Status.NOT_MODIFIED;
import net.es.nsi.dds.yaml.dao.ResourceResponse;
import org.apache.http.client.utils.DateUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author hacksaw
 */
public class Common {
  public static ResponseEntity<?> toResponseEntity(HttpHeaders headers, ResourceResponse rr) {
    if (rr == null) {
      return new ResponseEntity<>(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    switch (rr.getStatus()) {
      case NOT_MODIFIED:
        return new ResponseEntity<>(headers, HttpStatus.NOT_MODIFIED);

        default:
          Error.ErrorBuilder eb = Error.builder().error(rr.getStatus().getReasonPhrase());
          rr.getError().ifPresent(e -> eb.error_description(e));
          return new ResponseEntity<>(eb.build(), HttpStatus.valueOf(rr.getStatus().getStatusCode()));
    }
  }

  public static long parseIfModfiedSince(String ifModifiedSince) {
    long ifms = 0;
    if (!Strings.isNullOrEmpty(ifModifiedSince)) {
      Date lastModified = DateUtils.parseDate(ifModifiedSince);
      if (lastModified != null) {
        ifms = lastModified.getTime();
      }
    }

    return ifms;
  }
}