package net.es.nsi.dds.yaml.dao;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author hacksaw
 */
@ApiModel(value="resource", description="This is a simple API version resource.")
public class Resource {
    @ApiModelProperty(value = "The API name identifier.", required=true)
    private String id;                      // The API name identifier.

    @ApiModelProperty(value = "A URI reference to the resource.", required=true)
    private String href;                    // A URI reference to the resource.

    @ApiModelProperty(value = "Version of the resource.", required=true)
    private String version;                 // Version of the resource.

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{ id: ");
        sb.append(this.getId());
        sb.append("\n  href:");
        sb.append(this.getHref());
        sb.append("\n  version:");
        sb.append(this.getVersion());
        sb.append("/n}");

        return sb.toString();
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the href
     */
    public String getHref() {
        return href;
    }

    /**
     * @param href the href to set
     */
    public void setHref(String href) {
        this.href = href;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }
}