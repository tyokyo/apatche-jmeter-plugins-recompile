package kg.apc.jmeter.perfmon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import kg.apc.perfmon.client.Transport;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class NewAgentConnector
  implements PerfMonAgentConnector
{
  private static final Logger log = LoggingManager.getLoggerForClass();
  protected Transport transport;
  private Map<String, String> metrics = new HashMap();
  private String[] metricLabels;

  public void setTransport(Transport atransport)
  {
    this.transport = atransport;
  }

  public void connect() throws IOException {
    log.debug(this.metrics.toString());

    ArrayList labels = new ArrayList(this.metrics.keySet());
    this.metricLabels = ((String[])labels.toArray(new String[labels.size()]));

    ArrayList arr = new ArrayList(this.metrics.values());
    String[] m = (String[])arr.toArray(new String[arr.size()]);
    this.transport.startWithMetrics(m);
  }

  public void disconnect() {
    this.transport.disconnect();
  }

  public void generateSamples(PerfMonSampleGenerator collector) throws IOException {
    String[] data = this.transport.readMetrics();
    for (int n = 0; n < data.length; n++)
      if (!data[n].isEmpty())
        try {
          collector.generateSample(Double.parseDouble(data[n]), this.metricLabels[n]);
        } catch (NumberFormatException|ArrayIndexOutOfBoundsException e) {
          collector.generateErrorSample(this.metricLabels[n], e.toString());
        }
  }

  public void addMetric(String metric, String params, String label)
  {
    this.metrics.put(label, metric.toLowerCase() + ":" + params);
  }
}