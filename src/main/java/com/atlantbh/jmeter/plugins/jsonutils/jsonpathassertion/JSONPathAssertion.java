package com.atlantbh.jmeter.plugins.jsonutils.jsonpathassertion;

import com.atlantbh.jmeter.plugins.jsonutils.jsonpathextractor.JSONPathExtractor;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;
import java.io.Serializable;
import net.minidev.json.JSONArray;
import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.oro.text.PatternCacheLRU;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Matcher;

public class JSONPathAssertion extends AbstractTestElement
implements Serializable, Assertion
{
	private static final Logger log = LoggingManager.getLoggerForClass();
	private static final long serialVersionUID = 1L;
	public static final String JSONPATH = "JSON_PATH";
	public static final String EXPECTEDVALUE = "EXPECTED_VALUE";
	public static final String JSONVALIDATION = "JSONVALIDATION";
	public static final String EXPECT_NULL = "EXPECT_NULL";
	public static final String INVERT = "INVERT";
	public static final String ISREGEX = "ISREGEX";

	public String getJsonPath()
	{
		return getPropertyAsString("JSON_PATH");
	}

	public void setJsonPath(String jsonPath) {
		setProperty("JSON_PATH", jsonPath);
	}

	public String getExpectedValue() {
		return getPropertyAsString("EXPECTED_VALUE");
	}

	public void setExpectedValue(String expectedValue) {
		setProperty("EXPECTED_VALUE", expectedValue);
	}

	public void setJsonValidationBool(boolean jsonValidation) {
		setProperty("JSONVALIDATION", jsonValidation);
	}

	public void setExpectNull(boolean val) {
		setProperty("EXPECT_NULL", val);
	}

	public boolean isExpectNull() {
		return getPropertyAsBoolean("EXPECT_NULL");
	}

	public boolean isJsonValidationBool() {
		return getPropertyAsBoolean("JSONVALIDATION");
	}

	public void setInvert(boolean invert) {
		setProperty("INVERT", invert);
	}

	public boolean isInvert() {
		return getPropertyAsBoolean("INVERT");
	}

	public void setIsRegex(boolean flag) {
		setProperty("ISREGEX", flag);
	}

	public boolean isUseRegex() {
		return getPropertyAsBoolean("ISREGEX", true);
	}

	private void doAssert(String jsonString) {
		Object value = JsonPath.read(jsonString, getJsonPath(), new Predicate[0]);

		if (isJsonValidationBool()) {
			if ((value instanceof JSONArray)) {
				if (!arrayMatched((JSONArray)value));
			}
			else
			{
				if ((isExpectNull()) && (value == null))
					return;
				if (isEquals(value)) {
					return;
				}
			}

			if (isExpectNull()) {
				throw new RuntimeException(String.format("Value expected to be null, but found '%s'", new Object[] { value }));
			}
			throw new RuntimeException(String.format("Value expected to be '%s', but found '%s'", new Object[] { getExpectedValue(), JSONPathExtractor.objectToString(value) }));
		}
	}

	private boolean arrayMatched(JSONArray value)
	{
		if ((value.isEmpty()) && (getExpectedValue().equals("[]"))) {
			return true;
		}

		for (Object subj : value.toArray()) {
			if ((isExpectNull()) && (subj == null))
				return true;
			if (isEquals(subj)) {
				return true;
			}
		}

		return isEquals(value);
	}

	private boolean isEquals(Object subj) {
		String str = JSONPathExtractor.objectToString(subj);
		if (isUseRegex()) {
			Pattern pattern = JMeterUtils.getPatternCache().getPattern(getExpectedValue());
			return JMeterUtils.getMatcher().matches(str, pattern);
		}
		return str.equals(getExpectedValue());
	}

	public AssertionResult getResult(SampleResult samplerResult)
	{
		AssertionResult result = new AssertionResult(getName());
		String responseData = samplerResult.getResponseDataAsString();
		if (responseData.isEmpty()) {
			return result.setResultForNull();
		}

		result.setFailure(false);
		result.setFailureMessage("");

		if (!isInvert())
			try {
				doAssert(responseData);
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Assertion failed", e);
				}
				result.setFailure(true);
				result.setFailureMessage(e.getMessage());
			}
		else {
			try {
				doAssert(responseData);
				result.setFailure(true);
				if (isJsonValidationBool()) {
					if (isExpectNull())
						result.setFailureMessage("Failed that JSONPath " + getJsonPath() + " not matches null");
					else
						result.setFailureMessage("Failed that JSONPath " + getJsonPath() + " not matches " + getExpectedValue());
				}
				else result.setFailureMessage("Failed that JSONPath not exists: " + getJsonPath()); 
			}
			catch (Exception e)
			{
				if (log.isDebugEnabled()) {
					log.debug("Assertion failed", e);
				}
			}
		}
		return result;
	}
}