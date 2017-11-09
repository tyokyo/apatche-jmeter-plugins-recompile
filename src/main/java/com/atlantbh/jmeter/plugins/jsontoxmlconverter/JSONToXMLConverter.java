package com.atlantbh.jmeter.plugins.jsontoxmlconverter;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;

public class JSONToXMLConverter extends AbstractSampler
{
	private static final long serialVersionUID = 1L;
	private static final String JSONINPUT = "JSONINPUT";
	private static final String XMLOUTPUT = "XMLOUTPUT";

	@Deprecated
	private String ConvertToXML(String jsonData)
	{
		XMLSerializer serializer = new XMLSerializer();
		JSON json = JSONSerializer.toJSON(jsonData);
		serializer.setRootName("xmlOutput");
		serializer.setTypeHintsEnabled(false);
		return serializer.write(json);
	}

	private void convertToXML() {
		XMLSerializer serializer = new XMLSerializer();
		JSON json = JSONSerializer.toJSON(getJsonInput());
		serializer.setRootName("xmlOutput");
		serializer.setTypeHintsEnabled(false);
		setXmlOutput(serializer.write(json));
	}

	public void setJsonInput(String jsonInput) {
		setProperty("JSONINPUT", jsonInput);
	}

	public String getJsonInput() {
		return getPropertyAsString("JSONINPUT");
	}

	public void setXmlOutput(String xmlOutput) {
		setProperty("XMLOUTPUT", xmlOutput);
	}

	public String getXmlOutput() {
		return getPropertyAsString("XMLOUTPUT");
	}

	public SampleResult sample(Entry e)
	{
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());
		result.setSamplerData(getJsonInput());
		result.setDataType("text");
		result.sampleStart();
		if (!getJsonInput().equalsIgnoreCase("")) {
			try {
				convertToXML();
				result.setResponseData(getXmlOutput().getBytes());
				result.setSuccessful(true);
			} catch (Exception e1) {
				result.setResponseData(e1.getMessage().getBytes());
				result.setSuccessful(false);
			}
		}

		result.sampleEnd();
		return result;
	}
}