package com.blazemeter.jmeter.threads.arrivals;

import com.blazemeter.jmeter.threads.AbstractDynamicThreadGroup;
import com.blazemeter.jmeter.threads.AbstractDynamicThreadGroupGui;
import com.blazemeter.jmeter.threads.AdditionalFieldsPanel;
import com.blazemeter.jmeter.threads.LoadParamsFieldsPanel;
import com.blazemeter.jmeter.threads.ParamsPanel;
import java.awt.Color;
import kg.apc.charting.GraphPanelChart;
import kg.apc.jmeter.JMeterPluginsUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class ArrivalsThreadGroupGui extends AbstractDynamicThreadGroupGui
{
  private static final Logger log = LoggingManager.getLoggerForClass();

  public ArrivalsThreadGroupGui()
  {
    JMeterPluginsUtils.addHelpLinkToPanel(this, getClass().getSimpleName());
  }

  public String getLabelResource()
  {
    return getClass().getCanonicalName();
  }

  public String getStaticLabel()
  {
    return "bzm - Arrivals Thread Group";
  }

  protected ArrivalsThreadGroup createThreadGroupObject()
  {
    return new ArrivalsThreadGroup();
  }

  protected AdditionalFieldsPanel getAdditionalFieldsPanel()
  {
    return new AdditionalFieldsPanel(true);
  }

  protected void setChartPropertiesFromTG(AbstractDynamicThreadGroup tg)
  {
    if ((tg instanceof ArrivalsThreadGroup)) {
      ArrivalsThreadGroup atg = (ArrivalsThreadGroup)tg;
      this.previewChart.setYAxisLabel("Number of arrivals/" + atg.getUnitStr());
    }
  }

  protected Color getRowColor()
  {
    return Color.MAGENTA;
  }

  protected String getRowLabel(double totalArrivals)
  {
    log.debug("Total arr: " + totalArrivals);
    return "Arrival Rate (~" + Math.round(totalArrivals) + " total arrivals)";
  }

  protected ParamsPanel createLoadPanel()
  {
    LoadParamsFieldsPanel loadFields = new LoadParamsFieldsPanel("Target Rate (arrivals/sec): ", "Ramp Up Time (sec): ", "Hold Target Rate Time (sec): ");
    loadFields.addUpdateListener(this);
    return loadFields;
  }
}