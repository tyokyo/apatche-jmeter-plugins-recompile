package com.blazemeter.jmeter.threads;

import com.blazemeter.jmeter.reporters.FlushingResultCollector;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public abstract class AbstractDynamicThreadGroupModel extends AbstractThreadGroup
  implements TestStateListener
{
  private static final Logger log = LoggingManager.getLoggerForClass();
  protected static final long WAIT_TO_DIE = JMeterUtils.getPropDefault("jmeterengine.threadstop.wait", 5000);
  public static final String LOG_FILENAME = "LogFilename";
  public static final String TARGET_LEVEL = "TargetLevel";
  public static final String RAMP_UP = "RampUp";
  public static final String STEPS = "Steps";
  public static final String ITERATIONS = "Iterations";
  public static final String HOLD = "Hold";
  protected final Set<DynamicThread> threads = Collections.newSetFromMap(new ConcurrentHashMap());
  protected final ResultCollector logFile = new FlushingResultCollector();
  protected volatile boolean running = false;

  public void setLogFilename(String value) {
    setProperty("LogFilename", value);
  }

  public String getLogFilename() {
    return getPropertyAsString("LogFilename");
  }

  protected void saveLogRecord(String marker, String threadName, String arrivalID) {
    SampleResult res = new SampleResult();
    res.sampleStart();
    res.setSampleLabel(arrivalID);
    res.setResponseMessage(marker);
    res.setThreadName(threadName);
    res.sampleEnd();
    SampleEvent evt = new SampleEvent(res, getName());
    this.logFile.sampleOccurred(evt);
  }

  public void testStarted()
  {
    testStarted("");
  }

  public void testStarted(String s)
  {
    this.logFile.setFilename(getLogFilename());
    this.logFile.testStarted(s);
  }

  public void testEnded()
  {
    testEnded("");
  }

  public void testEnded(String s)
  {
    this.logFile.testEnded(s);
  }

  public int numberOfActiveThreads()
  {
    return this.threads.size();
  }

  public int getNumberOfThreads()
  {
    return this.threads.size();
  }

  public void addThread(DynamicThread threadWorker) {
    this.threads.add(threadWorker);
  }

  public void setTargetLevel(String value) {
    setProperty("TargetLevel", value.trim());
  }

  public String getTargetLevel() {
    return getPropertyAsString("TargetLevel", "1");
  }

  public int getTargetLevelAsInt() {
    return getPropertyAsInt("TargetLevel", 1);
  }

  public void setRampUp(String value) {
    setProperty("RampUp", value.trim());
  }

  public String getRampUp() {
    return getPropertyAsString("RampUp", "");
  }

  public long getRampUpSeconds() {
    String val = getRampUp();
    if (val.isEmpty()) {
      return 0L;
    }
    return Math.round(Double.parseDouble(val) * getUnitFactor());
  }

  public double getUnitFactor()
  {
    return 1.0D;
  }

  public void setSteps(String value) {
    setProperty("Steps", value.trim());
  }

  public String getSteps() {
    return getPropertyAsString("Steps", "");
  }

  public long getStepsAsLong() {
    String val = getSteps();
    if (val.isEmpty()) {
      return 0L;
    }
    return Long.parseLong(val);
  }

  public void setHold(String value)
  {
    setProperty("Hold", value.trim());
  }

  public String getHold() {
    return getPropertyAsString("Hold", "1");
  }

  public long getHoldSeconds() {
    String val = getHold();
    if (val.isEmpty()) {
      return 0L;
    }
    return Math.round(Double.parseDouble(val) * getUnitFactor());
  }

  public double getTargetLevelAsDouble()
  {
    return getTargetLevelAsInt();
  }

  public double getTargetLevelFactored() {
    return getTargetLevelAsDouble() / getUnitFactor();
  }

  public long getIterationsLimitAsLong() {
    String val = getIterationsLimit();
    if (val.isEmpty()) {
      return 0L;
    }
    return Long.parseLong(val);
  }

  public String getIterationsLimit()
  {
    return getPropertyAsString("Iterations");
  }

  public void setIterationsLimit(String val) {
    setProperty("Iterations", val);
  }
}