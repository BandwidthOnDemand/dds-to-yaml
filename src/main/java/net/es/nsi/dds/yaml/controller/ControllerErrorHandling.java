package net.es.nsi.dds.yaml.controller;

import com.google.common.base.Strings;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import javax.servlet.http.HttpServletRequest;
import net.es.nsi.dds.yaml.dao.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Exception
 * HTTP Status Code
 * ConversionNotSupportedException	500 (Internal Server Error)
 * HttpMediaTypeNotAcceptableException	406 (Not Acceptable)
 * HttpMediaTypeNotSupportedException	415 (Unsupported MediaType)
 * HttpMessageNotReadableException	400 (Bad Request)
 * HttpMessageNotWritableException	500 (Internal Server Error)
 * HttpRequestMethodNotSupportedException	405 (Method Not Allowed)
 * MissingServletRequestParameterException	400 (Bad Request)
 * NoSuchRequestHandlingMethodException 404 (Not Found)
 * TypeMismatchException	400 (Bad Request)
 *
 * @author hacksaw
 */
@ControllerAdvice
public class ControllerErrorHandling extends ResponseEntityExceptionHandler {
  private static final Logger LOG = LogManager.getLogger(ControllerErrorHandling.class);

  @ExceptionHandler(MalformedURLException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ResponseEntity<?> handleMalformedUrlException(HttpServletRequest request, MalformedURLException ex) {
    LOG.error("handleMalformedUrlException: received a MalformedURLException for request {}", request.getRequestURL());
    LOG.error(getHeaders(request));
    Error error = Error.builder()
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .error_description(ex.getMessage())
            .build();
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NoSuchElementException.class)
  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  @ResponseBody
  public ResponseEntity<?> handleResourceNotFoundException(HttpServletRequest request, NoSuchElementException ex) {
    LOG.error("handleResourceNotFoundException: {}", request.getRequestURL());
    LOG.error(getHeaders(request));
    Error error = Error.builder()
            .error(HttpStatus.NOT_FOUND.getReasonPhrase())
            .error_description(ex.getMessage())
            .build();
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler({URISyntaxException.class, Exception.class})
  @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ResponseEntity<?> handleException(HttpServletRequest request, Exception ex) {
    LOG.error("handleException: recieved Exception for request {}", request.getRequestURL());
    LOG.error(getHeaders(request));
    Error error = Error.builder()
            .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
            .error_description(ex.getMessage())
            .build();
    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Return a string containing the servlet request headers.
   *
   * @param request
   * @return
   */
  private String getHeaders(HttpServletRequest request) {
    StringBuilder sb = new StringBuilder("\n");
    sb.append(request.getMethod());
    sb.append(" ");
    sb.append(request.getRequestURL());
    sb.append("\n");

    Enumeration<String> headers = request.getHeaderNames();
    while (headers.hasMoreElements()) {
      String header = headers.nextElement();
      if (!Strings.isNullOrEmpty(header)) {
        sb.append(header);
        sb.append(": ");
        sb.append(request.getHeader(header));
        sb.append("\n");
      }
    }
    return sb.toString();
  }

}
