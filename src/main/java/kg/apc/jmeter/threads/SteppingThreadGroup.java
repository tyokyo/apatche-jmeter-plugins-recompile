package kg.apc.jmeter.threads;

import org.apache.jmeter.threads.JMeterThread;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

@Deprecated
public class SteppingThreadGroup extends AbstractSimpleThreadGroup
{
  private static final Logger log = LoggingManager.getLoggerForClass();
  private static final String THREAD_GROUP_DELAY = "Threads initial delay";
  private static final String INC_USER_PERIOD = "Start users period";
  private static final String INC_USER_COUNT = "Start users count";
  private static final String INC_USER_COUNT_BURST = "Start users count burst";
  private static final String DEC_USER_PERIOD = "Stop users period";
  private static final String DEC_USER_COUNT = "Stop users count";
  private static final String FLIGHT_TIME = "flighttime";
  private static final String RAMPUP = "rampUp";

  protected void scheduleThread(JMeterThread thread, long tgStartTime)
  {
    int inUserCount = getInUserCountAsInt();
    int outUserCount = getOutUserCountAsInt();

    if (inUserCount == 0) {
      inUserCount = getNumThreads();
    }
    if (outUserCount == 0) {
      outUserCount = getNumThreads();
    }

    int inUserCountBurst = Math.min(getInUserCountBurstAsInt(), getNumThreads());
    if (inUserCountBurst <= 0) {
      inUserCountBurst = inUserCount;
    }

    int rampUpBucket = thread.getThreadNum() < inUserCountBurst ? 0 : 1 + 
      (thread
      .getThreadNum() - inUserCountBurst) / inUserCount;
    int rampUpBucketThreadCount = thread.getThreadNum() < inUserCountBurst ? inUserCountBurst : inUserCount;

    long threadGroupDelay = 1000L * getThreadGroupDelayAsInt();
    long ascentPoint = tgStartTime + threadGroupDelay;
    long inUserPeriod = 1000L * getInUserPeriodAsInt();
    long additionalRampUp = 1000L * getRampUpAsInt() / rampUpBucketThreadCount;
    long flightTime = 1000L * getFlightTimeAsInt();
    long outUserPeriod = 1000L * getOutUserPeriodAsInt();

    long rampUpDuration = 1000L * getRampUpAsInt();
    long iterationDuration = inUserPeriod + rampUpDuration;

    int iterationCountTotal = getNumThreads() < inUserCountBurst ? 1 : 
      (int)Math.ceil((getNumThreads() - inUserCountBurst) / inUserCount);

    int lastIterationUserCount = (getNumThreads() - inUserCountBurst) % inUserCount;
    if (lastIterationUserCount == 0) {
      lastIterationUserCount = inUserCount;
    }
    long descentPoint = ascentPoint + iterationCountTotal * iterationDuration + 1000L * getRampUpAsInt() / inUserCount * lastIterationUserCount + flightTime;

    long rampUpBucketStartTime = ascentPoint + rampUpBucket * iterationDuration;

    int rampUpBucketThreadPosition = thread.getThreadNum() < inUserCountBurst ? thread.getThreadNum() : 
      (thread
      .getThreadNum() - inUserCountBurst) % inUserCount;

    long startTime = rampUpBucketStartTime + rampUpBucketThreadPosition * additionalRampUp;
    long endTime = descentPoint + outUserPeriod * (int)Math.floor(thread.getThreadNum() / outUserCount);

    log.debug(String.format("threadNum=%d, rampUpBucket=%d, rampUpBucketThreadCount=%d, rampUpBucketStartTime=%d, rampUpBucketThreadPosition=%d, rampUpDuration=%d, iterationDuration=%d, iterationCountTotal=%d, ascentPoint=%d, descentPoint=%d, startTime=%d, endTime=%d", new Object[] { 
      Integer.valueOf(thread
      .getThreadNum()), Integer.valueOf(rampUpBucket), Integer.valueOf(rampUpBucketThreadCount), Long.valueOf(rampUpBucketStartTime), Integer.valueOf(rampUpBucketThreadPosition), Long.valueOf(rampUpDuration), Long.valueOf(iterationDuration), Integer.valueOf(iterationCountTotal), Long.valueOf(ascentPoint), Long.valueOf(descentPoint), Long.valueOf(startTime), Long.valueOf(endTime) }));

    thread.setStartTime(startTime);
    thread.setEndTime(endTime);
    thread.setScheduled(true);
  }

  public String getThreadGroupDelay() {
    return getPropertyAsString("Threads initial delay");
  }

  public void setThreadGroupDelay(String delay) {
    setProperty("Threads initial delay", delay);
  }

  public String getInUserPeriod() {
    return getPropertyAsString("Start users period");
  }

  public void setInUserPeriod(String value) {
    setProperty("Start users period", value);
  }

  public String getInUserCount() {
    return getPropertyAsString("Start users count");
  }

  public void setInUserCount(String delay) {
    setProperty("Start users count", delay);
  }

  public String getInUserCountBurst() {
    return getPropertyAsString("Start users count burst");
  }

  public void setInUserCountBurst(String text) {
    setProperty("Start users count burst", text);
  }

  public String getFlightTime() {
    return getPropertyAsString("flighttime");
  }

  public void setFlightTime(String delay) {
    setProperty("flighttime", delay);
  }

  public String getOutUserPeriod() {
    return getPropertyAsString("Stop users period");
  }

  public void setOutUserPeriod(String delay) {
    setProperty("Stop users period", delay);
  }

  public String getOutUserCount() {
    return getPropertyAsString("Stop users count");
  }

  public void setOutUserCount(String delay) {
    setProperty("Stop users count", delay);
  }

  public String getRampUp() {
    return getPropertyAsString("rampUp");
  }

  public void setRampUp(String delay) {
    setProperty("rampUp", delay);
  }

  public int getThreadGroupDelayAsInt() {
    return getPropertyAsInt("Threads initial delay");
  }

  public int getInUserPeriodAsInt() {
    return getPropertyAsInt("Start users period");
  }

  public int getInUserCountAsInt() {
    return getPropertyAsInt("Start users count");
  }

  public int getInUserCountBurstAsInt() {
    return getPropertyAsInt("Start users count burst");
  }

  public int getRampUpAsInt() {
    return getPropertyAsInt("rampUp");
  }

  public int getFlightTimeAsInt() {
    return getPropertyAsInt("flighttime");
  }

  public int getOutUserPeriodAsInt() {
    return getPropertyAsInt("Stop users period");
  }

  public int getOutUserCountAsInt() {
    return getPropertyAsInt("Stop users count");
  }

  public void setNumThreads(String execute) {
    setProperty("ThreadGroup.num_threads", execute);
  }

  public String getNumThreadsAsString() {
    return getPropertyAsString("ThreadGroup.num_threads");
  }
}