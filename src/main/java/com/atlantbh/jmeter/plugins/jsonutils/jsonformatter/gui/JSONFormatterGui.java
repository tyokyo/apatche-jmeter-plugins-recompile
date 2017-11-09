package com.atlantbh.jmeter.plugins.jsonutils.jsonformatter.gui;

import com.atlantbh.jmeter.plugins.jsonutils.jsonformatter.JSONFormatter;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import kg.apc.jmeter.JMeterPluginsUtils;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.testelement.TestElement;

public class JSONFormatterGui extends AbstractPostProcessorGui
{
	private static final long serialVersionUID = 1L;
	private static final String WIKIPAGE = "JSONFormatter";

	public JSONFormatterGui()
	{
		init();
	}

	public void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());
		add(JMeterPluginsUtils.addHelpLinkToPanel(makeTitlePanel(), "JSONFormatter"), "North");

		VerticalPanel panel = new VerticalPanel();
		panel.setBorder(BorderFactory.createEtchedBorder());

		add(panel, "Center");
	}

	public void clearGui()
	{
		super.clearGui();
	}

	public TestElement createTestElement()
	{
		JSONFormatter formatter = new JSONFormatter();
		modifyTestElement(formatter);
		formatter.setComment(JMeterPluginsUtils.getWikiLinkText("JSONFormatter"));
		return formatter;
	}

	public String getLabelResource()
	{
		return getClass().getSimpleName();
	}

	public String getStaticLabel()
	{
		return JMeterPluginsUtils.prefixLabel("JSON Format Post Processor");
	}

	public void modifyTestElement(TestElement element)
	{
		super.configureTestElement(element);
	}
}