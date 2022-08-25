package net.es.nsi.dds.yaml.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author hacksaw
 */
@RestController
public class RestErrorController implements ErrorController {
    private static final Logger LOG = LogManager.getLogger(ControllerErrorHandling.class);
    static final String PATH = "/error";

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @RequestMapping(value = "/error")
    public ResponseEntity<?> error() {
        LOG.error("RestErrorController: not found");
        net.es.nsi.dds.yaml.dao.Error error = net.es.nsi.dds.yaml.dao.Error.builder()
            .error(HttpStatus.NOT_FOUND.getReasonPhrase())
            .error_description("Requested resource not found")
            .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    public String getErrorPath() {
            return PATH;
    }
}
