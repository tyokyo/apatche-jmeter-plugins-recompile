package com.blazemeter.jmeter.reporters;

import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class FlushingResultCollector extends ResultCollector
{
  private static final Logger log = LoggingManager.getLoggerForClass();

  public FlushingResultCollector()
  {
    getSaveConfig().setFieldNames(true);
  }

  public void testEnded(String host)
  {
    super.testEnded(host);
    try
    {
      ResultCollector.class.getDeclaredMethod("flushFile", new Class[0]);
      //flushFile();
    } catch (NoSuchMethodException e) {
      log.warn("Cannot flush PrintWriter to file");
    }
  }
}