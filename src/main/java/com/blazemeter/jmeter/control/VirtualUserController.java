package com.blazemeter.jmeter.control;

import com.blazemeter.jmeter.threads.AbstractDynamicThreadGroup;
import com.blazemeter.jmeter.threads.DynamicThread;
import com.blazemeter.jmeter.threads.arrivals.ArrivalsThreadGroup;
import com.blazemeter.jmeter.threads.concurrency.ConcurrencyThreadGroup;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.NextIsNullException;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class VirtualUserController extends GenericController
{
  private static final Logger log = LoggingManager.getLoggerForClass();
  private boolean hasArrived = false;
  protected AbstractDynamicThreadGroup owner;
  private long iterationNo = 0L;

  public Sampler next()
  {
    if (!this.owner.isRunning()) {
      setDone(true);
    } else if (!this.hasArrived) {
      if (this.owner.isLimitReached()) {
        throw new IllegalStateException("Should not have more iterations");
      }
      this.hasArrived = true;
      this.iterationNo += 1L;
      if ((this.owner instanceof ArrivalsThreadGroup)) {
        getOwnerAsArrivals().arrivalFact(JMeterContextService.getContext().getThread(), this.iterationNo);
      }
    }

    return super.next();
  }

  private boolean moveToPool(JMeterThread thread) {
    if (((thread instanceof DynamicThread)) && 
      (!this.owner.isLimitReached()) && (getOwnerAsArrivals().movedToPool((DynamicThread)thread))) {
      reInitialize();
      return true;
    }

    return false;
  }

  protected void reInitialize()
  {
    super.reInitialize();
    this.hasArrived = false;
  }

  protected Sampler nextIsNull() throws NextIsNullException
  {
    JMeterThread thread = JMeterContextService.getContext().getThread();
    if ((this.owner instanceof ArrivalsThreadGroup)) {
      getOwnerAsArrivals().completionFact(thread, this.iterationNo);
    }

    long iLimit = this.owner.getIterationsLimitAsLong();

    if (this.owner.isLimitReached()) {
      log.info("Test limit reached, thread is done: " + thread.getThreadName());
      setDone(true);
      return null;
    }if ((iLimit > 0L) && (this.iterationNo >= iLimit)) {
      log.info("Iteration limit reached, thread is done: " + thread.getThreadName());
      setDone(true);
      return null;
    }if (((this.owner instanceof ConcurrencyThreadGroup)) && (((ConcurrencyThreadGroup)this.owner).tooMuchConcurrency())) {
      log.info("Need to decrease concurrency, thread is done: " + thread.getThreadName());
      setDone(true);
      return null;
    }if ((this.owner instanceof ArrivalsThreadGroup)) {
      moveToPool(thread);
      return super.nextIsNull();
    }
    reInitialize();
    return next();
  }

  public void setOwner(AbstractDynamicThreadGroup owner)
  {
    this.owner = owner;
  }

  public void startNextLoop() {
    JMeterThread thread = JMeterContextService.getContext().getThread();
    if ((this.owner instanceof ArrivalsThreadGroup)) {
      getOwnerAsArrivals().abandonFact(thread, this.iterationNo);

      if (!moveToPool(thread))
        setDone(true);
    }
  }

  private ArrivalsThreadGroup getOwnerAsArrivals()
  {
    return (ArrivalsThreadGroup)this.owner;
  }
}