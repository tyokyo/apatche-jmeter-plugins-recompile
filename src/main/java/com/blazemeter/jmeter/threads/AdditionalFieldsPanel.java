package com.blazemeter.jmeter.threads;

import com.blazemeter.jmeter.gui.ArrangedLabelFieldPanel;
import com.blazemeter.jmeter.threads.arrivals.ArrivalsThreadGroup;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import kg.apc.jmeter.JMeterVariableEvaluator;
import org.apache.jmeter.gui.util.HorizontalPanel;

public class AdditionalFieldsPanel extends ArrangedLabelFieldPanel
  implements ParamsPanel
{
  protected JTextField logFile = new JTextField();
  protected JTextField iterations = new JTextField();
  protected JTextField concurrLimit = new JTextField();
  protected ButtonGroup unitGroup = new ButtonGroup();
  protected JRadioButton unitSeconds = new JRadioButton("seconds");
  protected JRadioButton unitMinutes = new JRadioButton("minutes");

  public AdditionalFieldsPanel(boolean showConcurrencyLimit) {
    JPanel groupPanel = new HorizontalPanel();
    this.unitMinutes.setActionCommand("M");
    this.unitSeconds.setActionCommand("S");
    this.unitGroup.add(this.unitMinutes);
    this.unitGroup.add(this.unitSeconds);
    groupPanel.add(this.unitMinutes);
    groupPanel.add(this.unitSeconds);
    add("Time Unit: ", groupPanel);

    add("Thread Iterations Limit: ", this.iterations);
    add("Log Threads Status into File: ", this.logFile);

    if (showConcurrencyLimit)
      add("Concurrency Limit: ", this.concurrLimit);
  }

  public void modelToUI(AbstractDynamicThreadGroup tg)
  {
    this.logFile.setText(tg.getLogFilename());
    this.iterations.setText(tg.getIterationsLimit());
    this.concurrLimit.setText("1000");
    this.unitMinutes.setSelected(true);
    if ((tg instanceof ArrivalsThreadGroup)) {
      ArrivalsThreadGroup atg = (ArrivalsThreadGroup)tg;
      this.concurrLimit.setText(atg.getConcurrencyLimit());
    }

    Enumeration it = this.unitGroup.getElements();
    while (it.hasMoreElements()) {
      AbstractButton btn = (AbstractButton)it.nextElement();
      if (btn.getActionCommand().equals(tg.getUnit()))
        btn.setSelected(true);
    }
  }

  public void UItoModel(AbstractDynamicThreadGroup tg, JMeterVariableEvaluator evaluator)
  {
    tg.setLogFilename(evaluator.evaluate(this.logFile.getText()));
    tg.setIterationsLimit(evaluator.evaluate(this.iterations.getText()));
    if (this.unitGroup.getSelection() != null) {
      tg.setUnit(this.unitGroup.getSelection().getActionCommand());
    }

    if ((tg instanceof ArrivalsThreadGroup)) {
      ArrivalsThreadGroup atg = (ArrivalsThreadGroup)tg;
      atg.setConcurrencyLimit(evaluator.evaluate(this.concurrLimit.getText()));
    }
  }

  public void clearUI() {
    this.logFile.setText("");
    this.iterations.setText("");
    this.concurrLimit.setText("1000");
    this.unitMinutes.setSelected(true);
  }

  public void addActionListener(ActionListener listener) {
    this.unitMinutes.addActionListener(listener);
    this.unitSeconds.addActionListener(listener);
  }
}