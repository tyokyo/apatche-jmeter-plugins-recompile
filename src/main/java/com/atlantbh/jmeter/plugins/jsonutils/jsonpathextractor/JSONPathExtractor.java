package com.atlantbh.jmeter.plugins.jsonutils.jsonpathextractor;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.Predicate;
import java.util.Map;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class JSONPathExtractor extends AbstractTestElement
implements PostProcessor
{
	private static final Logger log = LoggingManager.getLoggerForClass();
	private static final long serialVersionUID = 1L;
	public static final String JSONPATH = "JSONPATH";
	public static final String VAR = "VAR";
	public static final String DEFAULT = "DEFAULT";
	public static final String SUBJECT = "SUBJECT";
	public static final String SRC_VARNAME = "VARIABLE";
	public static final String SUBJECT_BODY = "BODY";
	public static final String SUBJECT_VARIABLE = "VAR";

	public String getJsonPath()
	{
		return getPropertyAsString("JSONPATH");
	}

	public void setJsonPath(String jsonPath) {
		setProperty("JSONPATH", jsonPath);
	}

	public String getVar() {
		return getPropertyAsString("VAR");
	}

	public void setVar(String var) {
		setProperty("VAR", var);
	}

	public void setDefaultValue(String defaultValue) {
		setProperty("DEFAULT", defaultValue);
	}

	public String getDefaultValue() {
		return getPropertyAsString("DEFAULT");
	}

	public void setSrcVariableName(String defaultValue) {
		setProperty("VARIABLE", defaultValue);
	}

	public String getSrcVariableName() {
		return getPropertyAsString("VARIABLE");
	}

	public void setSubject(String defaultValue) {
		setProperty("SUBJECT", defaultValue);
	}

	public String getSubject() {
		return getPropertyAsString("SUBJECT");
	}

	public void process()
	{
		JMeterContext context = getThreadContext();
		JMeterVariables vars = context.getVariables();
		SampleResult previousResult = context.getPreviousResult();
		String responseData;
		if (getSubject().equals("VAR"))
			responseData = vars.get(getSrcVariableName());
		else {
			responseData = previousResult.getResponseDataAsString();
		}

		try
		{
			Object jsonPathResult = JsonPath.read(responseData, getJsonPath(), new Predicate[0]);
			if ((jsonPathResult instanceof JSONArray)) {
				Object[] arr = ((JSONArray)jsonPathResult).toArray();

				if (arr.length == 0) {
					throw new PathNotFoundException("Extracted array is empty");
				}

				vars.put(getVar(), objectToString(jsonPathResult));
				vars.put(getVar() + "_matchNr", objectToString(Integer.valueOf(arr.length)));

				int k = 1;
				while (vars.get(getVar() + "_" + k) != null) {
					vars.remove(getVar() + "_" + k);
					k++;
				}

				for (int n = 0; n < arr.length; n++)
					vars.put(getVar() + "_" + (n + 1), objectToString(arr[n]));
			}
			else {
				vars.put(getVar(), objectToString(jsonPathResult));
			}
		} catch (Exception e) {
			log.warn("Extract failed", e);
			vars.put(getVar(), getDefaultValue());
			vars.put(getVar() + "_matchNr", "0");
			int k = 1;
			while (vars.get(getVar() + "_" + k) != null) {
				vars.remove(getVar() + "_" + k);
				k++;
			}
		}
	}

	public static String objectToString(Object subj)
	{
		String str;
		if (subj == null) {
			str = "null";
		}
		else
		{
			if ((subj instanceof Map))
			{
				str = new JSONObject((Map)subj).toJSONString();
			}
			else str = subj.toString();
		}
		return str;
	}
}