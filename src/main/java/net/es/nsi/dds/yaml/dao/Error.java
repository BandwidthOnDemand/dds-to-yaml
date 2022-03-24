package net.es.nsi.dds.yaml.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author hacksaw
 */
@lombok.Builder
@ApiModel(value = "error", description = "Error structure for REST interface.")
@XmlRootElement(name = "error")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Error {

  private String error;               // A short error description.
  private String error_description;   // longer description, human-readable.
  private String error_uri;           // URI to a detailed error description on the API developer website.

  public Error() {}

  public Error(String error, String error_description, String error_uri) {
    this.error = error;
    this.error_description = error_description;
    this.error_uri = error_uri;
  }

  /**
   * @return the error
   */
  @ApiModelProperty(value = "A short error description.", required = true)
  public String getError() {
    return error;
  }

  /**
   * @param error the error to set
   */
  public void setError(String error) {
    this.error = error;
  }

  /**
   * @return the error_description
   */
  @ApiModelProperty(value = "A Longer human-readable description of error.", required = true)
  public String getError_description() {
    return error_description;
  }

  /**
   * @param error_description the error_description to set
   */
  public void setError_description(String error_description) {
    this.error_description = error_description;
  }

  /**
   * @return the error_uri
   */
  @ApiModelProperty(value = "URI to a detailed error description on the API developer website.", required = false)
  public String getError_uri() {
    return error_uri;
  }

  /**
   * @param error_uri the error_uri to set
   */
  public void setError_uri(String error_uri) {
    this.error_uri = error_uri;
  }

  @Override
  public String toString() {
    return String.format(
            "{ \n    \"error\" = \"%s\",\n     \"error_description\" = \"%s\",\n    \"error_uri\" = \"%s\"\n}",
            this.error, this.error_description, this.error_uri);
  }
}
