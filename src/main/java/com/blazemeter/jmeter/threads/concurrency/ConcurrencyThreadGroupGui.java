package com.blazemeter.jmeter.threads.concurrency;

import com.blazemeter.jmeter.threads.AbstractDynamicThreadGroup;
import com.blazemeter.jmeter.threads.AbstractDynamicThreadGroupGui;
import com.blazemeter.jmeter.threads.AdditionalFieldsPanel;
import com.blazemeter.jmeter.threads.LoadParamsFieldsPanel;
import com.blazemeter.jmeter.threads.ParamsPanel;
import java.awt.Color;
import kg.apc.charting.GraphPanelChart;
import kg.apc.jmeter.JMeterPluginsUtils;

public class ConcurrencyThreadGroupGui extends AbstractDynamicThreadGroupGui
{
  public String getLabelResource()
  {
    return getClass().getCanonicalName();
  }

  public String getStaticLabel()
  {
    return "bzm - Concurrency Thread Group";
  }

  public ConcurrencyThreadGroupGui()
  {
    JMeterPluginsUtils.addHelpLinkToPanel(this, getClass().getSimpleName());
  }

  protected ParamsPanel createLoadPanel()
  {
    LoadParamsFieldsPanel loadParamsFieldsPanel = new LoadParamsFieldsPanel("Target Concurrency: ", "Ramp Up Time (sec): ", "Hold Target Rate Time (sec): ");
    loadParamsFieldsPanel.addUpdateListener(this);
    return loadParamsFieldsPanel;
  }

  protected AdditionalFieldsPanel getAdditionalFieldsPanel()
  {
    return new AdditionalFieldsPanel(false);
  }

  protected AbstractDynamicThreadGroup createThreadGroupObject()
  {
    return new ConcurrencyThreadGroup();
  }

  protected void setChartPropertiesFromTG(AbstractDynamicThreadGroup atg)
  {
    this.previewChart.setYAxisLabel("Number of concurrent threads");
  }

  protected Color getRowColor()
  {
    return Color.RED;
  }

  protected String getRowLabel(double totalArrivals)
  {
    return "Concurrent Threads";
  }
}