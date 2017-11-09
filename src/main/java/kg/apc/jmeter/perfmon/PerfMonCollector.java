package kg.apc.jmeter.perfmon;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import kg.apc.jmeter.JMeterPluginsUtils;
import kg.apc.jmeter.vizualizers.CorrectedResultCollector;
import kg.apc.perfmon.client.Transport;
import kg.apc.perfmon.client.TransportFactory;
import kg.apc.perfmon.metrics.MetricParams;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class PerfMonCollector extends CorrectedResultCollector
  implements Runnable, PerfMonSampleGenerator
{
  private static boolean autoGenerateFiles = JMeterUtils.getPropDefault("forcePerfmonFile", "false").trim().equalsIgnoreCase("true");
  private static final String PERFMON = "PerfMon";
  private static final Logger log = LoggingManager.getLoggerForClass();
  public static final String DATA_PROPERTY = "metricConnections";
  private int interval;
  private Thread workerThread = null;
  private Map<Object, PerfMonAgentConnector> connectors = new ConcurrentHashMap();
  private HashMap<String, Long> oldValues = new HashMap();
  private static String autoFileBaseName = null;
  private static int counter = 0;
  private static final LinkedList<String> filesList = new LinkedList();
  private static String workerHost = null;

  private static synchronized String getAutoFileName()
  {
    String ret = "";
    counter += 1;
    if (autoFileBaseName == null) {
      Calendar now = Calendar.getInstance();
      SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss");
      autoFileBaseName = "perfMon_" + formatter.format(now.getTime());
    }
    ret = ret + autoFileBaseName;
    if (counter > 1) {
      ret = ret + "_" + counter;
    }
    ret = ret + ".csv";

    return ret;
  }

  public PerfMonCollector() {
    this.interval = (JMeterUtils.getPropDefault("jmeterPlugin.perfmon.interval", 1000) / 1000);
  }

  public void setData(CollectionProperty rows) {
    setProperty(rows);
  }

  public JMeterProperty getMetricSettings() {
    return getProperty("metricConnections");
  }

  public void sampleOccurred(SampleEvent event)
  {
  }

  public synchronized void run()
  {
    while (true) {
      processConnectors();
      try {
        wait(this.interval * 1000);
      } catch (InterruptedException ex) {
        log.debug("Monitoring thread was interrupted", ex);
      }
    }
  }

  private static synchronized boolean isWorkingHost(String host)
  {
    if (workerHost == null) {
      workerHost = host;
      return true;
    }
    return host.equals(workerHost);
  }

  public void testStarted(String host)
  {
    if (!isWorkingHost(host)) {
      return;
    }

    if ((getProperty("filename") == null) || (getProperty("filename").getStringValue().trim().length() == 0)) {
      if (autoGenerateFiles)
        setupSaving(getAutoFileName());
      else {
        try {
          File tmpFile = File.createTempFile("perfmon_", ".jtl");
          tmpFile.delete();
          setupSaving(tmpFile.getAbsolutePath());
        } catch (IOException ex) {
          log.info("PerfMon metrics will not be recorded! Please run the test with -JforcePerfmonFile=true", ex);
        }
      }
    }

    log.debug("PerfMon metrics will be stored in " + getPropertyAsString("filename"));
    if ((!getSaveConfig().saveAsXml()) && (getSaveConfig().saveFieldNames()))
      filesList.add(getPropertyAsString("filename"));
    else {
      log.warn("Perfmon file saving setting is not CSV with header line, cannot upload it to BM.Sense: " + getPropertyAsString("filename"));
    }
    initiateConnectors();

    this.workerThread = new Thread(this);
    this.workerThread.start();

    super.testStarted(host);
  }

  private void setupSaving(String fileName) {
    SampleSaveConfiguration config = getSaveConfig();
    JMeterPluginsUtils.doBestCSVSetup(config);
    setSaveConfig(config);
    setFilename(fileName);
    log.info("PerfMon metrics will be stored in " + new File(fileName).getAbsolutePath());
  }

  public void testEnded(String host)
  {
    if (this.workerThread == null) {
      return;
    }
    workerHost = null;
    this.workerThread.interrupt();
    shutdownConnectors();

    autoFileBaseName = null;
    counter = 0;
    super.testEnded(host);
  }

  private void initiateConnectors() {
    this.oldValues.clear();
    JMeterProperty prop = getMetricSettings();
    this.connectors.clear();
    if (!(prop instanceof CollectionProperty)) {
      log.warn("Got unexpected property: " + prop);
      return;
    }
    CollectionProperty rows = (CollectionProperty)prop;

    for (int i = 0; i < rows.size(); i++) {
      Object val = rows.get(i).getObjectValue();
      if ((val instanceof ArrayList)) {
        ArrayList row = (ArrayList)val;
        String host = ((JMeterProperty)row.get(0)).getStringValue();
        int port = ((JMeterProperty)row.get(1)).getIntValue();
        String metric = ((JMeterProperty)row.get(2)).getStringValue();
        String params = ((JMeterProperty)row.get(3)).getStringValue();
        initiateConnector(host, port, i, metric, params);
      }
    }

    for (this.connectors.keySet().iterator(); this.connectors.keySet().iterator().hasNext(); ) { Object key = this.connectors.keySet().iterator().next();
      try {
        ((PerfMonAgentConnector)this.connectors.get(key)).connect();
      } catch (IOException ex) {
        log.error("Error connecting to agent", ex);
        this.connectors.put(key, new UnavailableAgentConnector(ex));
      } }
  }

  private void initiateConnector(String host, int port, int index, String metric, String params)
  {
    InetSocketAddress addr = new InetSocketAddress(host, port);
    String stringKey = addr.toString() + "#" + index;
    String labelHostname = host;

    String useHostnameProp = JMeterUtils.getProperty("jmeterPlugin.perfmon.label.useHostname");
    if ((useHostnameProp != null) && (Boolean.parseBoolean(useHostnameProp))) {
      labelHostname = JMeterPluginsUtils.getShortHostname(host);
    }

    MetricParams paramsParsed = MetricParams.createFromString(params);
    String label ="";
    if (paramsParsed.getLabel().isEmpty()) {
        label = labelHostname + " " + metric;
      if (!params.isEmpty())
        label = label + " " + params;
    }
    else {
      label = labelHostname + " " + metric + " " + paramsParsed.getLabel();

      String[] tokens = params.split("(?<!\\\\):");

      params = "";

      for (String token : tokens) {
        if (!token.startsWith("label=")) {
          if (params.length() != 0) {
            params = params + ":";
          }
          params = params + token;
        }
      }
    }
    try
    {
      if (this.connectors.containsKey(addr)) {
        ((PerfMonAgentConnector)this.connectors.get(addr)).addMetric(metric, params, label);
      } else {
        PerfMonAgentConnector connector = getConnector(host, port);
        connector.addMetric(metric, params, label);
        this.connectors.put(addr, connector);
      }
    } catch (IOException e) {
      log.error("Problems creating connector", e);
      this.connectors.put(stringKey, new UnavailableAgentConnector(e));
    }
  }

  protected PerfMonAgentConnector getConnector(String host, int port) throws IOException {
    log.debug("Trying new connector");
    SocketAddress addr = new InetSocketAddress(host, port);
    try
    {
      Transport transport = TransportFactory.TCPInstance(addr);
      if (!transport.test())
        throw new IOException("Agent is unreachable via TCP");
    }
    catch (IOException e) {
      log.info("Can't connect TCP transport for host: " + addr.toString(), e);
      boolean useUDP = JMeterUtils.getPropDefault("jmeterPlugin.perfmon.useUDP", false);
      if (!useUDP)
        throw e;
      try
      {
        log.debug("Connecting UDP");
        Transport transport = TransportFactory.UDPInstance(addr);
        if (!transport.test())
          throw new IOException("Agent is unreachable via UDP");
      }
      catch (IOException ex) {
        log.info("Can't connect UDP transport for host: " + addr.toString(), ex);
        throw ex;
      }
    }
    Transport transport=TransportFactory.UDPInstance(addr);
    NewAgentConnector conn = new NewAgentConnector();
    conn.setTransport(transport);
    transport.setInterval(this.interval);
    return conn;
  }

  private void shutdownConnectors() {
    log.debug("Shutting down connectors");
    Iterator it = this.connectors.keySet().iterator();
    while (it.hasNext()) {
      Object key = it.next();
      PerfMonAgentConnector conn = (PerfMonAgentConnector)this.connectors.get(key);
      log.debug("Shutting down " + conn.toString());

      it.remove();
      conn.disconnect();
    }
  }

  private void processConnectors() {
    for (Iterator localIterator = this.connectors.keySet().iterator(); localIterator.hasNext(); ) { Object key = localIterator.next();
      PerfMonAgentConnector connector = (PerfMonAgentConnector)this.connectors.get(key);
      try {
        connector.generateSamples(this);
      } catch (IOException e) {
        log.error(e.getMessage());
        this.connectors.put(key, new UnavailableAgentConnector(e));
      }
    }
  }

  public void generateSample(double value, String label)
  {
    PerfMonSampleResult res = new PerfMonSampleResult();
    res.setSampleLabel(label);
    res.setValue(value);
    res.setSuccessful(true);
    SampleEvent e = new SampleEvent(res, "PerfMon");
    super.sampleOccurred(e);
  }

  public void generateErrorSample(String label, String errorMsg)
  {
    PerfMonSampleResult res = new PerfMonSampleResult();
    res.setSampleLabel(label);
    res.setValue(-1.0D);
    res.setResponseMessage(errorMsg);
    res.setSuccessful(false);
    SampleEvent e = new SampleEvent(res, "PerfMon");
    super.sampleOccurred(e);
    log.error("Perfmon plugin error: " + errorMsg);
  }

  public void generate2Samples(long[] values, String label1, String label2)
  {
    generate2Samples(values, label1, label2, 1.0D);
  }

  public void generate2Samples(long[] values, String label1, String label2, double dividingFactor)
  {
    if ((this.oldValues.containsKey(label1)) && (this.oldValues.containsKey(label2))) {
      generateSample((values[0] - ((Long)this.oldValues.get(label1)).longValue()) / dividingFactor, label1);
      generateSample((values[1] - ((Long)this.oldValues.get(label2)).longValue()) / dividingFactor, label2);
    }
    this.oldValues.put(label1, Long.valueOf(values[0]));
    this.oldValues.put(label2, Long.valueOf(values[1]));
  }

  public static LinkedList<String> getFiles() {
    return filesList;
  }

  public static void clearFiles() {
    filesList.clear();
  }
}