package com.blazemeter.jmeter.threads.concurrency;

import com.blazemeter.jmeter.threads.AbstractDynamicThreadGroup;
import com.blazemeter.jmeter.threads.AbstractThreadStarter;
import com.blazemeter.jmeter.threads.DynamicThread;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class ConcurrencyThreadStarter extends AbstractThreadStarter
{
  private static final Logger log = LoggingManager.getLoggerForClass();
  private final ConcurrencyThreadGroup concurrTG;

  public ConcurrencyThreadStarter(int groupIndex, ListenerNotifier listenerNotifier, ListedHashTree testTree, StandardJMeterEngine engine, ConcurrencyThreadGroup concurrencyThreadGroup)
  {
    super(groupIndex, concurrencyThreadGroup, testTree, listenerNotifier, engine);
    this.concurrTG = concurrencyThreadGroup;
  }

  protected void supplyActiveThreads() throws InterruptedException
  {
    log.info("Start supplying threads");
    this.startTime = System.currentTimeMillis();
    while ((!this.owner.isLimitReached()) && (getPlannedConcurrency() >= 0L)) {
      log.debug("Concurrency factual/expected: " + this.concurrTG.getConcurrency() + "/" + getPlannedConcurrency());
      while (this.concurrTG.getConcurrency() < getPlannedConcurrency()) {
        DynamicThread thread = addActiveThread();
        this.concurrTG.threadStarted(thread);
      }
      this.concurrTG.waitThreadStopped();
    }
    log.info("Done supplying threads");
  }

  private long getPlannedConcurrency() {
    long rampUp = this.owner.getRampUpSeconds();
    long hold = this.owner.getHoldSeconds();
    long steps = this.owner.getStepsAsLong();
    double maxConcurr = this.owner.getTargetLevelAsDouble();
    double timeOffset = (System.currentTimeMillis() - this.startTime) / 1000.0D;
    log.debug("Time progress: " + timeOffset + "/" + (rampUp + hold));

    long shift = JMeterUtils.getPropDefault("dynamic_tg.shift_rampup_start", 0L);
    timeOffset -= shift;
    if (timeOffset < 0.0D) {
      timeOffset = 0.0D;
    }

    if (timeOffset >= rampUp + hold) {
      return -1L;
    }

    if ((rampUp == 0L) || (timeOffset > rampUp))
      return Math.round(maxConcurr);
    if (steps > 0L) {
      double stepSize = maxConcurr / steps;
      double stepLen = rampUp / steps;
      return Math.round(stepSize * (Math.floor(timeOffset / stepLen) + 1.0D));
    }
    double slope = maxConcurr / rampUp;
    return Math.round(slope * timeOffset);
  }
}