package com.atlantbh.jmeter.plugins.jsonutils.jsonpathextractor.gui;

import com.atlantbh.jmeter.plugins.jsonutils.jsonpathextractor.JSONPathExtractor;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import kg.apc.jmeter.JMeterPluginsUtils;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.testelement.TestElement;

public class JSONPathExtractorGui extends AbstractPostProcessorGui
{
	private static final long serialVersionUID = 1L;
	private JTextField variableNameTextField = null;
	private JTextField jsonPathTextField = null;
	private JTextField defaultValTextField = null;
	private static final String WIKIPAGE = "JSONPathExtractor";
	private JRadioButton useBody;
	private JRadioButton useVariable;
	private ButtonGroup group;
	private JTextField srcVariableName;

	public JSONPathExtractorGui()
	{
		init();
	}

	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());
		add(JMeterPluginsUtils.addHelpLinkToPanel(makeTitlePanel(), "JSONPathExtractor"), "North");

		JPanel mainPanel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0D;
		c.anchor = 23;
		c.fill = 2;
		mainPanel.add(makeSourcePanel(), c);

		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.anchor = 24;

		GridBagConstraints editConstraints = new GridBagConstraints();
		editConstraints.anchor = 23;
		editConstraints.weightx = 1.0D;
		editConstraints.fill = 2;

		addToPanel(mainPanel, labelConstraints, 0, 1, new JLabel("Destination Variable Name: ", 4));
		addToPanel(mainPanel, editConstraints, 1, 1, this.variableNameTextField = new JTextField(20));

		addToPanel(mainPanel, labelConstraints, 0, 2, new JLabel("JSONPath Expression: ", 4));
		addToPanel(mainPanel, editConstraints, 1, 2, this.jsonPathTextField = new JTextField(20));

		addToPanel(mainPanel, labelConstraints, 0, 3, new JLabel("Default Value: ", 4));
		addToPanel(mainPanel, editConstraints, 1, 3, this.defaultValTextField = new JTextField(20));

		JPanel container = new JPanel(new BorderLayout());
		container.add(mainPanel, "North");
		add(container, "Center");
	}

	private JPanel makeSourcePanel()
	{
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));

		this.useBody = new JRadioButton("Response Text");
		this.useVariable = new JRadioButton("JMeter Variable:");
		this.srcVariableName = new JTextField(20);

		this.group = new ButtonGroup();
		this.group.add(this.useBody);
		this.group.add(this.useVariable);

		panel.add(this.useBody);
		panel.add(this.useVariable);
		panel.add(this.srcVariableName);

		this.useBody.setSelected(true);

		this.useBody.setActionCommand("BODY");
		this.useVariable.setActionCommand("VAR");

		return panel;
	}

	private void addToPanel(JPanel panel, GridBagConstraints constraints, int col, int row, JComponent component) {
		constraints.gridx = col;
		constraints.gridy = row;
		panel.add(component, constraints);
	}

	public void clearGui()
	{
		super.clearGui();
		this.variableNameTextField.setText("");
		this.jsonPathTextField.setText("");
		this.defaultValTextField.setText("");
		this.srcVariableName.setText("");
		this.useBody.setSelected(true);
	}

	public TestElement createTestElement()
	{
		JSONPathExtractor extractor = new JSONPathExtractor();
		modifyTestElement(extractor);
		extractor.setComment(JMeterPluginsUtils.getWikiLinkText("JSONPathExtractor"));
		return extractor;
	}

	public String getLabelResource()
	{
		return getClass().getSimpleName();
	}

	public String getStaticLabel()
	{
		return JMeterPluginsUtils.prefixLabel("JSON Path Extractor");
	}

	public void modifyTestElement(TestElement element)
	{
		super.configureTestElement(element);
		if ((element instanceof JSONPathExtractor)) {
			JSONPathExtractor extractor = (JSONPathExtractor)element;
			extractor.setVar(this.variableNameTextField.getText());
			extractor.setJsonPath(this.jsonPathTextField.getText());
			extractor.setDefaultValue(this.defaultValTextField.getText());
			extractor.setSrcVariableName(this.srcVariableName.getText());
			extractor.setSubject(this.group.getSelection().getActionCommand());
		}
	}

	public void configure(TestElement element)
	{
		super.configure(element);
		if ((element instanceof JSONPathExtractor)) {
			JSONPathExtractor extractor = (JSONPathExtractor)element;
			this.variableNameTextField.setText(extractor.getVar());
			this.jsonPathTextField.setText(extractor.getJsonPath());
			this.defaultValTextField.setText(extractor.getDefaultValue());
			this.srcVariableName.setText(extractor.getSrcVariableName());
			if (extractor.getSubject().equals("VAR"))
				this.useVariable.setSelected(true);
			else
				this.useBody.setSelected(true);
		}
	}
}