package com.atlantbh.jmeter.plugins.jsonutils.jsonformatter;

import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class JSONFormatter extends AbstractTestElement
implements PostProcessor
{
	private static final Logger log = LoggingManager.getLoggerForClass();
	private static final long serialVersionUID = 1L;
	private static final JsonConfig config = new JsonConfig();

	private String formatJSON(String json)
	{
		JSON object = JSONSerializer.toJSON(json, config);
		return object.toString(4);
	}

	public void process()
	{
		JMeterContext context = getThreadContext();
		String responseData = context.getPreviousResult().getResponseDataAsString();
		try {
			String str = formatJSON(responseData);
			context.getPreviousResult().setResponseData(str.getBytes());
		} catch (JSONException e) {
			log.warn("Failed to format JSON: " + e.getMessage());
			log.debug("Failed to format JSON", e);
		}
	}
}