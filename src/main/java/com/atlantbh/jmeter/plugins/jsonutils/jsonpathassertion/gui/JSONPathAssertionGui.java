package com.atlantbh.jmeter.plugins.jsonutils.jsonpathassertion.gui;

import com.atlantbh.jmeter.plugins.jsonutils.jsonpathassertion.JSONPathAssertion;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import kg.apc.jmeter.JMeterPluginsUtils;
import org.apache.jmeter.assertions.gui.AbstractAssertionGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextArea;
import org.apache.jorphan.gui.JLabeledTextField;

public class JSONPathAssertionGui extends AbstractAssertionGui
  implements ChangeListener
{
  private static final long serialVersionUID = 1L;
  private JLabeledTextField jsonPath = null;
  private JLabeledTextArea jsonValue = null;
  private JCheckBox jsonValidation = null;
  private JCheckBox expectNull = null;
  private JCheckBox invert = null;
  private static final String WIKIPAGE = "JSONPathAssertion";
  private JCheckBox isRegex;

  public JSONPathAssertionGui()
  {
    init();
  }

  public void init() {
    setLayout(new BorderLayout());
    setBorder(makeBorder());
    add(JMeterPluginsUtils.addHelpLinkToPanel(makeTitlePanel(), "JSONPathAssertion"), "North");

    VerticalPanel panel = new VerticalPanel();
    panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

    this.jsonPath = new JLabeledTextField("JSON Path: ");
    this.jsonValidation = new JCheckBox("Validate against expected value");
    this.isRegex = new JCheckBox("Match as regular expression");
    this.jsonValue = new JLabeledTextArea("Expected Value: ");
    this.expectNull = new JCheckBox("Expect null");
    this.invert = new JCheckBox("Invert assertion (will fail if above conditions met)");

    this.jsonValidation.addChangeListener(this);
    this.expectNull.addChangeListener(this);

    panel.add(this.jsonPath);
    panel.add(this.jsonValidation);
    panel.add(this.isRegex);
    panel.add(this.jsonValue);
    panel.add(this.expectNull);
    panel.add(this.invert);

    add(panel, "Center");
  }

  public void clearGui()
  {
    super.clearGui();
    this.jsonPath.setText("$.");
    this.jsonValue.setText("");
    this.jsonValidation.setSelected(false);
    this.expectNull.setSelected(false);
    this.invert.setSelected(false);
    this.isRegex.setSelected(true);
  }

  public TestElement createTestElement()
  {
    JSONPathAssertion jpAssertion = new JSONPathAssertion();
    modifyTestElement(jpAssertion);
    jpAssertion.setComment(JMeterPluginsUtils.getWikiLinkText("JSONPathAssertion"));
    return jpAssertion;
  }

  public String getLabelResource()
  {
    return getClass().getSimpleName();
  }

  public String getStaticLabel()
  {
    return JMeterPluginsUtils.prefixLabel("JSON Path Assertion");
  }

  public void modifyTestElement(TestElement element)
  {
    super.configureTestElement(element);
    if ((element instanceof JSONPathAssertion)) {
      JSONPathAssertion jpAssertion = (JSONPathAssertion)element;
      jpAssertion.setJsonPath(this.jsonPath.getText());
      jpAssertion.setExpectedValue(this.jsonValue.getText());
      jpAssertion.setJsonValidationBool(this.jsonValidation.isSelected());
      jpAssertion.setExpectNull(this.expectNull.isSelected());
      jpAssertion.setInvert(this.invert.isSelected());
      jpAssertion.setIsRegex(this.isRegex.isSelected());
    }
  }

  public void configure(TestElement element)
  {
    super.configure(element);
    JSONPathAssertion jpAssertion = (JSONPathAssertion)element;
    this.jsonPath.setText(jpAssertion.getJsonPath());
    this.jsonValue.setText(jpAssertion.getExpectedValue());
    this.jsonValidation.setSelected(jpAssertion.isJsonValidationBool());
    this.expectNull.setSelected(jpAssertion.isExpectNull());
    this.invert.setSelected(jpAssertion.isInvert());
    this.isRegex.setSelected(jpAssertion.isUseRegex());
  }

  public void stateChanged(ChangeEvent e)
  {
    this.jsonValue.setEnabled((this.jsonValidation.isSelected()) && (!this.expectNull.isSelected()));
    this.isRegex.setEnabled((this.jsonValidation.isSelected()) && (!this.expectNull.isSelected()));
  }
}