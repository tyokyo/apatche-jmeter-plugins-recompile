package kg.apc.jmeter.perfmon;

import java.io.IOException;

public abstract interface PerfMonAgentConnector
{
  public abstract void connect()
    throws IOException;

  public abstract void disconnect();

  public abstract void generateSamples(PerfMonSampleGenerator paramPerfMonSampleGenerator)
    throws IOException;

  public abstract void addMetric(String paramString1, String paramString2, String paramString3);
}

/* Location:           D:\soft\jd-gui\jmeter-plugins-perfmon-2.1.jar
 * Qualified Name:     kg.apc.jmeter.perfmon.PerfMonAgentConnector
 * JD-Core Version:    0.6.2
 */