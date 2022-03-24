package net.es.nsi.dds.yaml.dao;

@lombok.Builder
@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class ClientType {
  public static final int MAX_CONN_PER_ROUTE = 10;
  public static final int MAX_CONN_TOTAL = 20;

  @lombok.Builder.Default
  private int maxConnPerRoute = MAX_CONN_PER_ROUTE;

  @lombok.Builder.Default
  private int maxConnTotal = MAX_CONN_TOTAL;

  @lombok.Builder.Default
  private boolean secure = false;
}