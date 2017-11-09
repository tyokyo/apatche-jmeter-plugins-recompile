package kg.apc.jmeter.perfmon;

public abstract interface PerfMonSampleGenerator
{
  public abstract void generate2Samples(long[] paramArrayOfLong, String paramString1, String paramString2, double paramDouble);

  public abstract void generate2Samples(long[] paramArrayOfLong, String paramString1, String paramString2);

  public abstract void generateSample(double paramDouble, String paramString);

  public abstract void generateErrorSample(String paramString1, String paramString2);
}