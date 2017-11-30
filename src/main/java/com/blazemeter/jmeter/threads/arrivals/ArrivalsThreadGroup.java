package com.blazemeter.jmeter.threads.arrivals;

import com.blazemeter.jmeter.threads.AbstractDynamicThreadGroup;
import com.blazemeter.jmeter.threads.DynamicThread;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.threads.ThreadCountsAccessor;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class ArrivalsThreadGroup extends AbstractDynamicThreadGroup
{
  public static final String CONCURRENCY_LIMIT = "ConcurrencyLimit";
  public static final String ARRIVALS_LIMIT = "ArrivalsLimit";
  private static final Logger log = LoggingManager.getLoggerForClass();
  protected final AtomicLong arrivalsCount = new AtomicLong();
  protected final AtomicLong completionsCount = new AtomicLong();
  protected AtomicLong abandonsCount = new AtomicLong();
  protected final Set<DynamicThread> poolThreads = Collections.newSetFromMap(new ConcurrentHashMap());

  public void start(int groupIndex, ListenerNotifier listenerNotifier, ListedHashTree testTree, StandardJMeterEngine engine)
  {
    super.start(groupIndex, listenerNotifier, testTree, engine);
    synchronized (this) {
      try {
        wait();
        log.info("Got first arrival");
      } catch (InterruptedException e) {
        log.warn("Interrupted start", e);
      }
    }
  }

  public int getNumThreads() {
    return this.threads.size();
  }

  public void addThread(DynamicThread threadWorker)
  {
    super.addThread(threadWorker);
    JMeterContextService.addTotalThreads(1);
  }

  protected Thread getThreadStarter(int groupIndex, ListenerNotifier listenerNotifier, ListedHashTree testTree, StandardJMeterEngine engine)
  {
    return new ArrivalsThreadStarter(groupIndex, listenerNotifier, testTree, engine, this);
  }

  public void stop()
  {
    super.stop();

    for (DynamicThread thread : this.poolThreads)
      thread.interruptOSThread();
  }

  public void tellThreadsToStop()
  {
    super.tellThreadsToStop();
    for (DynamicThread thread : this.poolThreads)
      stopThread(thread.getThreadName(), true);
  }

  public boolean verifyThreadsStopped()
  {
    boolean parent = super.verifyThreadsStopped();
    log.info("Verify shutdown thread counts: " + this.threads.size() + "/" + this.poolThreads.size());
    return (parent) && (this.poolThreads.isEmpty());
  }

  public boolean movedToPool(DynamicThread thread) {
    this.threads.remove(thread);
    if (thread.isStopping()) {
      log.debug("Did not move into pool, because thread is stopping: " + thread);
      return false;
    }

    this.poolThreads.add(thread);
    log.debug("Moved thread to pool: " + thread + ", pool size: " + this.poolThreads.size());

    ThreadCountsAccessor.decrNumberOfThreads();

    synchronized (thread) {
      try {
        thread.wait();
      } catch (InterruptedException e) {
        log.debug("Interrupted", e);
      }
    }
    ThreadCountsAccessor.incrNumberOfThreads();
    return this.running;
  }

  public synchronized boolean releasedPoolThread() {
    if (this.poolThreads.isEmpty()) {
      return false;
    }

    DynamicThread thread = ((DynamicThread[])this.poolThreads.toArray(new DynamicThread[this.poolThreads.size()]))[0];
    this.poolThreads.remove(thread);
    this.threads.add(thread);
    log.debug("Releasing pool thread: " + thread + ", pool size: " + this.poolThreads.size());

    synchronized (thread) {
      thread.notify();
    }
    return true;
  }

  public boolean isLimitReached() {
    long limit;
    try {
      limit = Long.parseLong(getArrivalsLimit());
    }
    catch (NumberFormatException e)
    {
      log.error("Invalid arrivals limit, defaulting to 0");
      limit = 0L;
    }
    return (limit > 0L) && (this.arrivalsCount.longValue() >= limit);
  }

  public synchronized void arrivalFact(JMeterThread thread, long arrivalID) {
    this.arrivalsCount.incrementAndGet();
    notifyAll();
    saveLogRecord("ARRIVAL", thread.getThreadName(), thread.getThreadNum() + "." + arrivalID);
  }

  public void completionFact(JMeterThread thread, long arrivalID) {
    this.completionsCount.incrementAndGet();
    saveLogRecord("COMPLETION", thread.getThreadName(), thread.getThreadNum() + "." + arrivalID);
  }

  public void abandonFact(JMeterThread thread, long arrivalID) {
    this.abandonsCount.incrementAndGet();
    saveLogRecord("ABANDONMENT", thread.getThreadName(), thread.getThreadNum() + "." + arrivalID);
  }

  public boolean canCreateMoreThreads() {
    try {
      long limit = Long.parseLong(getConcurrencyLimit());
      return (limit <= 0L) || (this.threads.size() < limit);
    } catch (NumberFormatException e) {
      log.debug("Invalid concurrency limit, defaulting to 0");
    }return true;
  }

  public void setConcurrencyLimit(String value)
  {
    setProperty("ConcurrencyLimit", value);
  }

  public String getConcurrencyLimit() {
    return getPropertyAsString("ConcurrencyLimit", "");
  }

  public void setArrivalsLimit(String value) {
    setProperty("ArrivalsLimit", value);
  }

  public String getArrivalsLimit() {
    return getPropertyAsString("ArrivalsLimit", "0");
  }

  public void testEnded(String s)
  {
    releaseAllPoolThreads();
    super.testEnded(s);
    log.info("Done " + this.arrivalsCount.longValue() + " arrivals, " + this.completionsCount.longValue() + " completions, " + this.abandonsCount.longValue() + " abandonments");
    log.debug("Pool size: " + this.poolThreads.size());
  }

  public void releaseAllPoolThreads() {
    for (DynamicThread thread : this.poolThreads)
    {
      synchronized (thread) {
        thread.interrupt();
        thread.interruptOSThread();
        thread.notify();
      }
    }
  }
}