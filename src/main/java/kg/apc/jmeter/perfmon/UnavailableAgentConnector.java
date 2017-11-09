package kg.apc.jmeter.perfmon;

import java.io.IOException;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

class UnavailableAgentConnector
  implements PerfMonAgentConnector
{
  private static final Logger log = LoggingManager.getLoggerForClass();
  private IOException cause;

  UnavailableAgentConnector(IOException e)
  {
    this.cause = e;
  }

  public void setMetricType(String metric) {
    log.debug("Dropped setMetric call");
  }

  public void setParams(String params) {
    log.debug("Dropped setParams call");
  }

  public void connect() throws IOException {
    log.debug("Dropped connect call");
  }

  public void disconnect() {
    log.debug("Dropped disconnect call");
  }

  public String getLabel(boolean translateHost) {
    return this.cause.toString();
  }

  public void generateSamples(PerfMonSampleGenerator collector) throws IOException {
    collector.generateErrorSample(getLabel(false), this.cause.toString());
  }

  public void addMetric(String metric, String params, String label) {
    log.debug("Dropped addMetric call");
  }
}