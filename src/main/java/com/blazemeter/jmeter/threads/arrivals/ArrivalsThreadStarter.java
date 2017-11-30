package com.blazemeter.jmeter.threads.arrivals;

import com.blazemeter.jmeter.threads.AbstractDynamicThreadGroup;
import com.blazemeter.jmeter.threads.AbstractThreadStarter;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class ArrivalsThreadStarter extends AbstractThreadStarter
{
  private static final Logger log = LoggingManager.getLoggerForClass();
  private ArrivalsThreadGroup arrivalsTG;
  protected long scheduledCount = 0L;
  protected double rollingTime = 0.0D;

  public ArrivalsThreadStarter(int groupIndex, ListenerNotifier listenerNotifier, ListedHashTree listedHashTree, StandardJMeterEngine standardJMeterEngine, ArrivalsThreadGroup owner) {
    super(groupIndex, owner, listedHashTree, listenerNotifier, standardJMeterEngine);
    this.arrivalsTG = owner;
  }

  protected void supplyActiveThreads() throws InterruptedException {
    while (needMoreArrivals())
      if (!this.arrivalsTG.releasedPoolThread())
        if (this.arrivalsTG.canCreateMoreThreads())
          addActiveThread();
        else
          log.debug("Not creating thread because of concurrency limit");
  }

  public synchronized boolean needMoreArrivals()
    throws InterruptedException
  {
    if (this.rollingTime > 0.0D) {
      while (this.rollingTime >= System.currentTimeMillis()) {
        long maxWait = (long) (this.rollingTime - System.currentTimeMillis());
        if (maxWait > 0L) {
          log.debug("Waiting " + maxWait);
          wait(maxWait);
        }
      }
    }
    this.rollingTime = System.currentTimeMillis();
    this.startTime = (this.rollingTime / 1000.0D);
    double currentRate;
    do {
      currentRate = getCurrentRate();
      if (currentRate == 0.0D) {
        log.debug("Zero arrivals rate, waiting a bit");
        this.rollingTime += 200.0D;
        Thread.sleep(200L);
      }
    }
    while (currentRate == 0.0D);

    if (currentRate < 0.0D) {
      log.info("Duration limit reached, no more arrivals needed, had arrivals: " + this.scheduledCount);
      ((ArrivalsThreadGroup)this.owner).setArrivalsLimit(String.valueOf(this.scheduledCount));
      return false;
    }
    tickRollingTime(currentRate);
    return !this.owner.isLimitReached();
  }

  protected double getCurrentRate()
  {
    long rampUp = this.owner.getRampUpSeconds();
    long hold = this.owner.getHoldSeconds();
    long steps = this.owner.getStepsAsLong();
    double throughput = this.owner.getTargetLevelFactored();
    double timeOffset = this.rollingTime / 1000.0D - this.startTime;

    if (timeOffset >= rampUp + hold) {
      return -1.0D;
    }

    if ((rampUp == 0L) || (timeOffset > rampUp))
      return throughput;
    if (steps > 0L) {
      double stepSize = throughput / steps;
      double stepLen = rampUp / steps;
      return stepSize * (Math.floor(timeOffset / stepLen) + 1.0D);
    }
    double slope = throughput / rampUp;
    return slope * Math.sqrt(2L * this.scheduledCount / slope);
  }

  protected void tickRollingTime(double currentRate)
  {
    if (currentRate > 0.0D) {
      double delay = currentRate > 0.0D ? 1000.0D / currentRate : 0.0D;
      this.rollingTime += delay;
      this.scheduledCount += 1L;
    } else {
      log.debug("Negative arrivals rate, ignoring");
    }
  }
}