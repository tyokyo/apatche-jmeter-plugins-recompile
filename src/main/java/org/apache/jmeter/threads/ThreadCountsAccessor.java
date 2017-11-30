package org.apache.jmeter.threads;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.MainFrame;

public class ThreadCountsAccessor
{
  private static long lastUpdate = 0L;

  public static void decrNumberOfThreads() {
    JMeterContextService.decrNumberOfThreads();
    refreshUI();
  }

  public static void incrNumberOfThreads() {
    JMeterContextService.incrNumberOfThreads();
    refreshUI();
  }

  private static void refreshUI() {
    long ts = System.currentTimeMillis();
    if (ts - lastUpdate < 1000L) {
      return;
    }

    lastUpdate = ts;
    GuiPackage gp = GuiPackage.getInstance();
    if (gp != null)
      gp.getMainFrame().updateCounts();
  }
}