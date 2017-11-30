package com.blazemeter.jmeter.threads;

import com.blazemeter.jmeter.control.VirtualUserController;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public abstract class AbstractThreadStarter extends Thread
{
  private static final Logger log = LoggingManager.getLoggerForClass();
  protected final ListenerNotifier notifier;
  protected final ListedHashTree threadGroupTree;
  protected final StandardJMeterEngine engine;
  protected final JMeterContext context;
  protected final AbstractDynamicThreadGroup owner;
  protected final int groupIndex;
  protected long threadIndex = 0L;
  protected HashTree treeClone;
  protected double startTime = 0.0D;

  public AbstractThreadStarter(int groupIndex, AbstractDynamicThreadGroup owner, ListedHashTree listedHashTree, ListenerNotifier listenerNotifier, StandardJMeterEngine standardJMeterEngine)
  {
    this.owner = owner;
    this.treeClone = cloneTree(listedHashTree);
    this.engine = standardJMeterEngine;
    this.groupIndex = groupIndex;
    this.threadGroupTree = listedHashTree;
    this.notifier = listenerNotifier;
    this.context = JMeterContextService.getContext();
    setDaemon(true);
  }

  public void run()
  {
    try
    {
      JMeterContextService.getContext().setVariables(this.context.getVariables());
      supplyActiveThreads();
    } catch (InterruptedException e) {
      log.debug("Interrupted", e);
    }
    log.debug("Thread starter has done its job");
  }

  protected abstract void supplyActiveThreads() throws InterruptedException;

  protected DynamicThread makeThread(long threadIndex) {
    boolean onErrorStopTest = this.owner.getOnErrorStopTest();
    boolean onErrorStopTestNow = this.owner.getOnErrorStopTestNow();
    boolean onErrorStopThread = this.owner.getOnErrorStopThread();
    boolean onErrorStartNextLoop = this.owner.getOnErrorStartNextLoop();
    DynamicThread jmeterThread = new DynamicThread(this.treeClone, this.owner, this.notifier);
    jmeterThread.setThreadNum((int)threadIndex);
    jmeterThread.setThreadGroup(this.owner);
    jmeterThread.setInitialContext(this.context);
    String threadName = this.owner.getName() + " " + this.groupIndex + "-" + (threadIndex + 1L);
    jmeterThread.setThreadName(threadName);
    jmeterThread.setEngine(this.engine);
    jmeterThread.setOnErrorStopTest(onErrorStopTest);
    jmeterThread.setOnErrorStopTestNow(onErrorStopTestNow);
    jmeterThread.setOnErrorStopThread(onErrorStopThread);
    jmeterThread.setOnErrorStartNextLoop(onErrorStartNextLoop);
    return jmeterThread;
  }

  protected ListedHashTree cloneTree(ListedHashTree tree)
  {
    TreeCloner cloner = new TreeCloner(true);
    tree.traverse(cloner);
    ListedHashTree clonedTree = cloner.getClonedTree();
    if (!clonedTree.isEmpty()) {
      Object firstElement = clonedTree.getArray()[0];
      Controller samplerController = ((AbstractDynamicThreadGroup)firstElement).getSamplerController();
      if ((samplerController instanceof VirtualUserController)) {
        assert (this.owner != null);
        ((VirtualUserController)samplerController).setOwner(this.owner);
      }
    }
    return clonedTree;
  }

  protected DynamicThread addActiveThread() {
    DynamicThread threadWorker = makeThread(this.threadIndex++);
    this.owner.addThread(threadWorker);
    Thread thread = new Thread(threadWorker, threadWorker.getThreadName());
    threadWorker.setOSThread(thread);
    thread.setDaemon(false);
    thread.start();
    this.treeClone = cloneTree(this.threadGroupTree);
    return threadWorker;
  }
}