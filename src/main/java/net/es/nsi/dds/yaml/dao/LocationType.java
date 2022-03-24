package net.es.nsi.dds.yaml.dao;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 *
 * @author hacksaw
 */
@lombok.Builder
@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationType {
    protected String name;
    protected Float longitude;
    protected Float latitude;
    protected Float altitude;
    protected String unlocode;
}
