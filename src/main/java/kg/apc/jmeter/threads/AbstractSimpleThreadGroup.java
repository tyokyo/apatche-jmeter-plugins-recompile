package kg.apc.jmeter.threads;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public abstract class AbstractSimpleThreadGroup extends AbstractThreadGroup
{
  private static final Logger log = LoggingManager.getLoggerForClass();

  private static final long WAIT_TO_DIE = JMeterUtils.getPropDefault("jmeterengine.threadstop.wait", 5000);

  private final Map<JMeterThread, Thread> allThreads = new ConcurrentHashMap();

  private volatile boolean running = false;

  private long tgStartTime = -1L;
  private static final long TOLERANCE = 1000L;

  protected abstract void scheduleThread(JMeterThread paramJMeterThread, long paramLong);

  public void scheduleThread(JMeterThread thread)
  {
    if (System.currentTimeMillis() - this.tgStartTime > 1000L) {
      this.tgStartTime = System.currentTimeMillis();
    }
    scheduleThread(thread, this.tgStartTime);
  }

  public void start(int groupCount, ListenerNotifier notifier, ListedHashTree threadGroupTree, StandardJMeterEngine engine)
  {
    this.running = true;

    int numThreads = getNumThreads();

    log.info("Starting thread group number " + groupCount + " threads " + numThreads);

    long now = System.currentTimeMillis();
    JMeterContext context = JMeterContextService.getContext();
    for (int i = 0; (this.running) && (i < numThreads); i++) {
      JMeterThread jmThread = makeThread(groupCount, notifier, threadGroupTree, engine, i, context);
      scheduleThread(jmThread, now);
      Thread newThread = new Thread(jmThread, jmThread.getThreadName());
      registerStartedThread(jmThread, newThread);
      newThread.start();
    }

    log.info("Started thread group number " + groupCount);
  }

  private void registerStartedThread(JMeterThread jMeterThread, Thread newThread) {
    this.allThreads.put(jMeterThread, newThread);
  }

  private JMeterThread makeThread(int groupCount, ListenerNotifier notifier, ListedHashTree threadGroupTree, StandardJMeterEngine engine, int i, JMeterContext context)
  {
    boolean onErrorStopTest = getOnErrorStopTest();
    boolean onErrorStopTestNow = getOnErrorStopTestNow();
    boolean onErrorStopThread = getOnErrorStopThread();
    boolean onErrorStartNextLoop = getOnErrorStartNextLoop();
    String groupName = getName();
    JMeterThread jmeterThread = new JMeterThread(cloneTree(threadGroupTree), this, notifier);
    jmeterThread.setThreadNum(i);
    jmeterThread.setThreadGroup(this);
    jmeterThread.setInitialContext(context);
    String threadName = groupName + " " + groupCount + "-" + (i + 1);
    jmeterThread.setThreadName(threadName);
    jmeterThread.setEngine(engine);
    jmeterThread.setOnErrorStopTest(onErrorStopTest);
    jmeterThread.setOnErrorStopTestNow(onErrorStopTestNow);
    jmeterThread.setOnErrorStopThread(onErrorStopThread);
    jmeterThread.setOnErrorStartNextLoop(onErrorStartNextLoop);
    return jmeterThread;
  }

  public boolean stopThread(String threadName, boolean now)
  {
    for (Map.Entry entry : this.allThreads.entrySet()) {
      JMeterThread thrd = (JMeterThread)entry.getKey();
      if (thrd.getThreadName().equals(threadName)) {
        thrd.stop();
        thrd.interrupt();
        if (now) {
          Thread t = (Thread)entry.getValue();
          if (t != null) {
            t.interrupt();
          }
        }
        return true;
      }
    }
    return false;
  }

  public void threadFinished(JMeterThread thread)
  {
    log.debug("Ending thread " + thread.getThreadName());
    this.allThreads.remove(thread);
  }

  public void tellThreadsToStop()
  {
    this.running = false;
    for (Map.Entry entry : this.allThreads.entrySet()) {
      JMeterThread item = (JMeterThread)entry.getKey();
      item.stop();
      item.interrupt();
      Thread t = (Thread)entry.getValue();
      if (t != null)
        t.interrupt();
    }
  }

  public void stop()
  {
    this.running = false;
    for (JMeterThread item : this.allThreads.keySet())
      item.stop();
  }

  public int numberOfActiveThreads()
  {
    return this.allThreads.size();
  }

  public boolean verifyThreadsStopped()
  {
    boolean stoppedAll = true;
    for (Thread t : this.allThreads.values()) {
      stoppedAll = (stoppedAll) && (verifyThreadStopped(t));
    }
    return stoppedAll;
  }

  private boolean verifyThreadStopped(Thread thread) {
    boolean stopped = true;
    if ((thread != null) && 
      (thread.isAlive())) {
      try {
        thread.join(WAIT_TO_DIE);
      } catch (InterruptedException localInterruptedException) {
      }
      if (thread.isAlive()) {
        stopped = false;
        log.warn("Thread won't exit: " + thread.getName());
      }
    }

    return stopped;
  }

  public void waitThreadsStopped()
  {
    for (Thread t : this.allThreads.values())
      waitThreadStopped(t);
  }

  private void waitThreadStopped(Thread thread)
  {
    if (thread != null)
      while (thread.isAlive())
        try {
          thread.join(WAIT_TO_DIE);
        }
        catch (InterruptedException localInterruptedException)
        {
        }
  }

  private ListedHashTree cloneTree(ListedHashTree tree) {
    TreeCloner cloner = new TreeCloner(true);
    tree.traverse(cloner);
    return cloner.getClonedTree();
  }
}