package com.atlantbh.jmeter.plugins.jsontoxmlconverter.gui;

import com.atlantbh.jmeter.plugins.jsontoxmlconverter.JSONToXMLConverter;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import kg.apc.jmeter.JMeterPluginsUtils;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

public class JSONToXMLConverterGui extends AbstractSamplerGui
{
  private static final long serialVersionUID = 1L;
  private JLabeledTextField jsonInputTextField = null;
  private static final String WIKIPAGE = "JSONToXMLConverter";

  public JSONToXMLConverterGui()
  {
    init();
  }

  private void init() {
    setLayout(new BorderLayout());
    setBorder(makeBorder());
    add(JMeterPluginsUtils.addHelpLinkToPanel(makeTitlePanel(), "JSONToXMLConverter"), "North");

    VerticalPanel panel = new VerticalPanel();
    panel.setBorder(BorderFactory.createEtchedBorder());
    this.jsonInputTextField = new JLabeledTextField("JSON input");
    panel.add(this.jsonInputTextField);
    add(panel, "Center");
  }

  public void clearGui()
  {
    super.clearGui();
    this.jsonInputTextField.setText("");
  }

  public TestElement createTestElement()
  {
    JSONToXMLConverter converter = new JSONToXMLConverter();
    modifyTestElement(converter);
    converter.setComment(JMeterPluginsUtils.getWikiLinkText("JSONToXMLConverter"));
    return converter;
  }

  public void modifyTestElement(TestElement element)
  {
    super.configureTestElement(element);
    if ((element instanceof JSONToXMLConverter)) {
      JSONToXMLConverter conv = (JSONToXMLConverter)element;
      conv.setJsonInput(this.jsonInputTextField.getText());
    }
  }

  public void configure(TestElement element)
  {
    super.configure(element);
    JSONToXMLConverter conv = null;
    if ((element instanceof JSONToXMLConverter)) {
      conv = (JSONToXMLConverter)element;
      this.jsonInputTextField.setText(conv.getJsonInput());
    }
  }

  public String getLabelResource()
  {
    return getClass().getSimpleName();
  }

  public String getStaticLabel()
  {
    return JMeterPluginsUtils.prefixLabel("JSON to XML Converter");
  }
}