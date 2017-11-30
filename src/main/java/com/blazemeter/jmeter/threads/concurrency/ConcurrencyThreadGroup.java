package com.blazemeter.jmeter.threads.concurrency;

import com.blazemeter.jmeter.threads.AbstractDynamicThreadGroup;
import java.util.Set;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class ConcurrencyThreadGroup extends AbstractDynamicThreadGroup
{
  private static final Logger log = LoggingManager.getLoggerForClass();
  public static final int MIN_CHECK_TIME = 1000;

  protected Thread getThreadStarter(int groupIndex, ListenerNotifier listenerNotifier, ListedHashTree testTree, StandardJMeterEngine engine)
  {
    return new ConcurrencyThreadStarter(groupIndex, listenerNotifier, testTree, engine, this);
  }

  public synchronized void waitThreadStopped() {
    if (!this.threads.isEmpty())
      try {
        wait(1000L);
      } catch (InterruptedException e) {
        log.debug("Interrupted", e);
      }
  }

  public int getNumThreads()
  {
    return (int)Math.round(getTargetLevelAsDouble());
  }

  public boolean isLimitReached()
  {
    return (!this.running) || (!this.threadStarter.isAlive());
  }

  public void threadStarted(JMeterThread thread) {
    saveLogRecord("START", thread.getThreadName(), "");
  }

  public void threadFinished(JMeterThread thread)
  {
    super.threadFinished(thread);
    saveLogRecord("FINISH", thread.getThreadName(), "");
    synchronized (this) {
      notifyAll();
    }
  }

  public long getConcurrency() {
    return this.threads.size();
  }

  public boolean tooMuchConcurrency() {
    return this.threads.size() > getTargetLevelAsDouble();
  }
}