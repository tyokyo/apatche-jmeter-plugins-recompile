package com.blazemeter.jmeter.threads;

import com.blazemeter.jmeter.gui.ArrangedLabelFieldPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import kg.apc.jmeter.JMeterVariableEvaluator;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class LoadParamsFieldsPanel extends ArrangedLabelFieldPanel
  implements ParamsPanel
{
  private static final Logger log = LoggingManager.getLoggerForClass();

  protected JTextField targetRate = new JTextField();
  protected JTextField rampUpTime = new JTextField();
  protected JTextField steps = new JTextField();
  protected JTextField holdFor = new JTextField();

  protected JLabel targetRateLabel = new JLabel();
  protected JLabel rampUpLabel = new JLabel();
  protected JLabel holdLabel = new JLabel();

  public LoadParamsFieldsPanel(String targetLbl, String rampUpLbl, String holdLbl) {
    this.targetRateLabel.setText(targetLbl);
    this.rampUpLabel.setText(rampUpLbl);
    this.holdLabel.setText(holdLbl);

    add(this.targetRateLabel, this.targetRate);
    add(this.rampUpLabel, this.rampUpTime);
    add("Ramp-Up Steps Count: ", this.steps);
    add(this.holdLabel, this.holdFor);
  }

  public void modelToUI(AbstractDynamicThreadGroup tg)
  {
    this.targetRate.setText(tg.getTargetLevel());
    this.rampUpTime.setText(tg.getRampUp());
    this.steps.setText(tg.getSteps());
    this.holdFor.setText(tg.getHold());
  }

  public void UItoModel(AbstractDynamicThreadGroup tg, JMeterVariableEvaluator evaluator)
  {
    tg.setTargetLevel(evaluator.evaluate(this.targetRate.getText()));
    tg.setRampUp(evaluator.evaluate(this.rampUpTime.getText()));
    tg.setSteps(evaluator.evaluate(this.steps.getText()));
    tg.setHold(evaluator.evaluate(this.holdFor.getText()));
  }

  public void clearUI()
  {
    this.targetRate.setText("12");
    this.rampUpTime.setText("60");
    this.steps.setText("3");
    this.holdFor.setText("180");
  }

  public void addUpdateListener(DocumentListener listener) {
    this.targetRate.getDocument().addDocumentListener(listener);
    this.rampUpTime.getDocument().addDocumentListener(listener);
    this.steps.getDocument().addDocumentListener(listener);
    this.holdFor.getDocument().addDocumentListener(listener);
  }

  public void changeUnitInLabels(String unit) {
    String oldUnit = unit.equals("M") ? "S" : "M";
    String oldUnitStr = AbstractDynamicThreadGroup.getUnitStr(oldUnit);
    String unitStr = AbstractDynamicThreadGroup.getUnitStr(unit);
    log.debug(oldUnit + " " + oldUnitStr + "=>" + unitStr);
    this.targetRateLabel.setText(this.targetRateLabel.getText().replace("/" + oldUnitStr + ")", "/" + unitStr + ")"));
    this.rampUpLabel.setText(this.rampUpLabel.getText().replace("(" + oldUnitStr + ")", "(" + unitStr + ")"));
    this.holdLabel.setText(this.holdLabel.getText().replace("(" + oldUnitStr + ")", "(" + unitStr + ")"));
  }
}