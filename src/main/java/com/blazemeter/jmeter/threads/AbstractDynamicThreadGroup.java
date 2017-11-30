package com.blazemeter.jmeter.threads;

import com.blazemeter.jmeter.control.VirtualUserController;
import java.util.Set;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public abstract class AbstractDynamicThreadGroup extends AbstractDynamicThreadGroupModel
{
  private static final Logger log = LoggingManager.getLoggerForClass();
  public static final String UNIT = "Unit";
  public static final String UNIT_MINUTES = "M";
  public static final String UNIT_SECONDS = "S";
  protected transient Thread threadStarter;

  public AbstractDynamicThreadGroup()
  {
    setProperty(new TestElementProperty("ThreadGroup.main_controller", new VirtualUserController()));
  }

  public void start(int groupIndex, ListenerNotifier listenerNotifier, ListedHashTree testTree, StandardJMeterEngine engine)
  {
    this.running = true;
    this.threadStarter = getThreadStarter(groupIndex, listenerNotifier, testTree, engine);
    this.threadStarter.setName(getName() + "-ThreadStarter");
    this.threadStarter.start();
  }

  protected abstract Thread getThreadStarter(int paramInt, ListenerNotifier paramListenerNotifier, ListedHashTree paramListedHashTree, StandardJMeterEngine paramStandardJMeterEngine);

  public void threadFinished(JMeterThread jMeterThread)
  {
    log.debug("threadFinished: " + jMeterThread.getThreadName());
    if ((jMeterThread instanceof DynamicThread))
      this.threads.remove(jMeterThread);
  }

  public void waitThreadsStopped()
  {
    while (this.running) {
      if (!this.threads.isEmpty()) {
        joinThreadFrom(this.threads);
      } else if (isLimitReached()) {
        log.debug("Don't need more load, running=false");
        this.running = false;
      } else if (!this.threadStarter.isAlive()) {
        log.debug("Thread Starter is done and we have no active threads, let's finish with this");
        this.running = false;
      } else {
        log.debug("Nothing to do, let's have some sleep");
        try {
          Thread.sleep(200L);
        } catch (InterruptedException e) {
          log.warn("Interrupted", e);
        }
      }
    }
    log.debug("Done waiting for threads stopped");
  }

  public abstract boolean isLimitReached();

  public boolean verifyThreadsStopped()
  {
    for (DynamicThread thread : this.threads) {
      if (thread.getOSThread() != null) {
        try {
          thread.getOSThread().join(WAIT_TO_DIE);
        } catch (InterruptedException e) {
          log.warn("Interrupted", e);
        }
      }
      stopThread(thread.getThreadName(), true);
    }
    return this.threads.isEmpty();
  }

  public void tellThreadsToStop()
  {
    this.running = false;
    this.threadStarter.interrupt();

    for (DynamicThread thread : this.threads)
      stopThread(thread.getThreadName(), false);
  }

  public void stop()
  {
    this.running = false;
    this.threadStarter.interrupt();
    for (DynamicThread thread : this.threads) {
      thread.interrupt();
      thread.interruptOSThread();
    }
  }

  public boolean stopThread(String threadName, boolean forced)
  {
    for (DynamicThread thrd : this.threads) {
      if (thrd.getThreadName().equals(threadName)) {
        thrd.stop();
        thrd.interrupt();
        if ((forced) && 
          (thrd.getOSThread() != null)) {
          thrd.getOSThread().interrupt();
        }

        return true;
      }
    }
    return false;
  }

  protected void joinThreadFrom(Set<DynamicThread> threadSet) {
    DynamicThread[] threads = (DynamicThread[])threadSet.toArray(new DynamicThread[threadSet.size()]);
    if ((threads.length > 0) && (threads[0] != null)) {
      DynamicThread thread = threads[0];
      log.debug("Joining thread " + thread.getThreadName());
      if (thread.getOSThread() != null) {
        try {
          thread.getOSThread().join(WAIT_TO_DIE);
        } catch (InterruptedException e) {
          log.warn("Interrupted", e);
        }
      }
      log.debug("Done joining thread " + thread.getThreadName());
    }
  }

  public boolean isRunning() {
    return this.running;
  }

  public static String getUnitStr(String unit) {
    if (unit.equals("M")) {
      return "min";
    }
    return "sec";
  }

  public void setUnit(String value)
  {
    setProperty("Unit", value);
  }

  public String getUnit() {
    return getPropertyAsString("Unit");
  }

  public double getUnitFactor() {
    if (getUnit().equals("M")) {
      return 60.0D;
    }
    return 1.0D;
  }

  public String getUnitStr()
  {
    String unit = getUnit();
    return getUnitStr(unit);
  }

  public void startNextLoop()
  {
    ((VirtualUserController)getSamplerController()).startNextLoop();
  }
}