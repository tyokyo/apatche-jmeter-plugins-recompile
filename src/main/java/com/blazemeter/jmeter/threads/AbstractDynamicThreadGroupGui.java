package com.blazemeter.jmeter.threads;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import kg.apc.charting.AbstractGraphRow;
import kg.apc.charting.DateTimeRenderer;
import kg.apc.charting.GraphPanelChart;
import kg.apc.charting.rows.GraphRowExactValues;
import kg.apc.jmeter.DummyEvaluator;
import kg.apc.jmeter.JMeterVariableEvaluator;
import kg.apc.jmeter.gui.GuiBuilderHelper;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.gui.AbstractThreadGroupGui;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public abstract class AbstractDynamicThreadGroupGui extends AbstractThreadGroupGui
  implements DocumentListener, Runnable, ActionListener
{
  private static final Logger log = LoggingManager.getLoggerForClass();
  protected GraphPanelChart previewChart;
  protected ConcurrentHashMap<String, AbstractGraphRow> chartModel;
  protected boolean uiCreated = false;
  private ParamsPanel loadFields = null;
  private AdditionalFieldsPanel additionalFields = null;
  private JMeterVariableEvaluator evaluator = new DummyEvaluator();

  public void configure(TestElement element)
  {
    super.configure(element);
    if (!this.uiCreated) {
      initUI();
    }
    if ((element instanceof AbstractDynamicThreadGroup)) {
      AbstractDynamicThreadGroup tg = (AbstractDynamicThreadGroup)element;
      this.loadFields.modelToUI(tg);
      this.additionalFields.modelToUI(tg);
      updateUI();
    }
  }

  public void modifyTestElement(TestElement element)
  {
    super.configureTestElement(element);
    if (!this.uiCreated) {
      initUI();
    }

    if ((element instanceof AbstractDynamicThreadGroup)) {
      AbstractDynamicThreadGroup tg = (AbstractDynamicThreadGroup)element;
      this.loadFields.UItoModel(tg, this.evaluator);
      this.additionalFields.UItoModel(tg, this.evaluator);
    }
  }

  protected void initUI() {
    JPanel container = new VerticalPanel();
    this.loadFields = createLoadPanel();
    container.add((Component)this.loadFields, "North");
    container.add(GuiBuilderHelper.getComponentWithMargin(getPreviewChart(), 2, 2, 0, 2), "Center");
    this.additionalFields = getAdditionalFieldsPanel();
    this.additionalFields.addActionListener(this);
    container.add(this.additionalFields, "South");
    add(container, "Center");
    this.uiCreated = true;
  }

  protected abstract AdditionalFieldsPanel getAdditionalFieldsPanel();

  public void clearGui()
  {
    super.clearGui();
    if (this.uiCreated) {
      this.loadFields.clearUI();
      this.additionalFields.clearUI();
    }
  }

  protected abstract ParamsPanel createLoadPanel();

  protected abstract AbstractDynamicThreadGroup createThreadGroupObject();

  protected abstract void setChartPropertiesFromTG(AbstractDynamicThreadGroup paramAbstractDynamicThreadGroup);

  protected abstract String getRowLabel(double paramDouble);

  protected abstract Color getRowColor();

  public void insertUpdate(DocumentEvent documentEvent)
  {
    SwingUtilities.invokeLater(this);
  }

  public void removeUpdate(DocumentEvent documentEvent)
  {
    SwingUtilities.invokeLater(this);
  }

  public void changedUpdate(DocumentEvent documentEvent)
  {
    SwingUtilities.invokeLater(this);
  }

  public void run()
  {
    updateUI();
  }

  public void actionPerformed(ActionEvent actionEvent)
  {
    SwingUtilities.invokeLater(this);
  }

  public TestElement createTestElement()
  {
    AbstractDynamicThreadGroup te = createThreadGroupObject();
    modifyTestElement(te);
    return te;
  }

  public void updateUI() {
    super.updateUI();
    if (!this.uiCreated) {
      log.debug("Won't update UI");
      return;
    }
    log.debug("Updating UI");

    AbstractDynamicThreadGroup atg = createThreadGroupObject();

    JMeterVariableEvaluator evaluator = new JMeterVariableEvaluator();
    this.loadFields.UItoModel(atg, evaluator);
    this.additionalFields.UItoModel(atg, evaluator);
    try
    {
      updateChart(atg);
    } catch (NumberFormatException e) {
      this.previewChart.setErrorMessage("The values entered cannot be rendered in preview...");
    } finally {
      setChartPropertiesFromTG(atg);
      this.previewChart.invalidateCache();
      this.previewChart.repaint();
    }

    if ((this.loadFields instanceof LoadParamsFieldsPanel)) {
      LoadParamsFieldsPanel panel = (LoadParamsFieldsPanel)this.loadFields;
      panel.changeUnitInLabels(atg.getUnit());
    }
  }

  protected void updateChart(AbstractDynamicThreadGroup atg) {
    double targetRate = atg.getTargetLevelAsDouble();
    long rampUp = atg.getRampUpSeconds();
    long holdFor = atg.getHoldSeconds();
    long stepsCount = atg.getStepsAsLong();
    double unitFactor = atg.getUnitFactor();

    this.chartModel.clear();
    this.previewChart.clearErrorMessage();
    AbstractGraphRow row = new GraphRowExactValues();
    row.setColor(getRowColor());
    row.setDrawLine(true);
    row.setMarkerSize(0);
    row.setDrawThickLines(true);

    row.add(0L, 0.0D);

    double totalArrivals = 0.0D;

    if (stepsCount > 0L) {
      double stepSize = targetRate / stepsCount;
      double stepLen = rampUp / stepsCount;

      for (int n = 1; n <= stepsCount; n++) {
        double stepRate = stepSize * n;
        row.add(Math.round((n - 1) * stepLen * 1000.0D), stepRate);
        row.add(Math.round(n * stepLen * 1000.0D), stepRate);
        totalArrivals += stepLen * stepRate;
      }
    } else {
      row.add(rampUp * 1000L, targetRate);
      totalArrivals += rampUp * targetRate / 2.0D;
    }
    row.add((rampUp + holdFor) * 1000L, targetRate);
    totalArrivals += holdFor * targetRate;
    totalArrivals /= unitFactor;

    this.previewChart.setxAxisLabelRenderer(new DateTimeRenderer("HH:mm:ss", 0L));
    this.chartModel.put(getRowLabel(totalArrivals), row);
  }

  public Component getPreviewChart() {
    this.previewChart = new GraphPanelChart(false, true);
    this.chartModel = new ConcurrentHashMap();
    this.previewChart.setRows(this.chartModel);
    this.previewChart.setxAxisLabel("Elapsed Time");
    this.previewChart.setBorder(BorderFactory.createBevelBorder(1));
    return this.previewChart;
  }
}