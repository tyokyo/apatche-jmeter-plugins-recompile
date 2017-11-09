package kg.apc.jmeter.perfmon;

import org.apache.jmeter.samplers.SampleResult;

public class PerfMonSampleResult extends SampleResult
{
  private final long ts;

  public PerfMonSampleResult()
  {
    this.ts = System.currentTimeMillis();
  }

  public void setValue(double value)
  {
    setStartTime(this.ts);
    setEndTime(this.ts +(long)value * 1000);
  }

  @Deprecated
  public double getValue() {
    return getTime() / 1000.0D;
  }

  public static double getValue(SampleResult res)
  {
    return res.getTime() / 1000.0D;
  }
}