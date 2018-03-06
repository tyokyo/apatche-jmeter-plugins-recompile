/*!
 * AtlantBH Custom Jmeter Components v1.0.0
 * http://www.atlantbh.com/jmeter-components/
 *
 * Copyright 2011, AtlantBH
 *
 * Licensed under the under the Apache License, Version 2.0.
 */
package com.atlantbh.jmeter.plugins.jsonutils.jsonpathassertion;

import com.atlantbh.jmeter.plugins.jsonutils.jsonpathextractor.JSONPathExtractor;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.oro.text.regex.Pattern;

import java.io.Serializable;

/**
 * This is main class for JSONPath Assertion which verifies assertion on
 * previous sample result using JSON path expression
 */
public class JSONPathAssertion extends AbstractTestElement implements Serializable, Assertion {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private static final long serialVersionUID = 1L;
    public static final String JSONPATH = "JSON_PATH";
    public static final String EXPECTEDVALUE = "EXPECTED_VALUE";
    public static final String JSONVALIDATION = "JSONVALIDATION";
    public static final String EXPECT_NULL = "EXPECT_NULL";
    public static final String INVERT = "INVERT";
    public static final String ISREGEX = "ISREGEX";

    public String getJsonPath() {
        return getPropertyAsString(JSONPATH);
    }

    public void setJsonPath(String jsonPath) {
        setProperty(JSONPATH, jsonPath);
    }

    public String getExpectedValue() {
        return getPropertyAsString(EXPECTEDVALUE);
    }

    public void setExpectedValue(String expectedValue) {
        setProperty(EXPECTEDVALUE, expectedValue);
    }

    public void setJsonValidationBool(boolean jsonValidation) {
        setProperty(JSONVALIDATION, jsonValidation);
    }

    public void setExpectNull(boolean val) {
        setProperty(EXPECT_NULL, val);
    }

    public boolean isExpectNull() {
        return getPropertyAsBoolean(EXPECT_NULL);
    }

    public boolean isJsonValidationBool() {
        return getPropertyAsBoolean(JSONVALIDATION);
    }

    public void setInvert(boolean invert) {
        setProperty(INVERT, invert);
    }

    public boolean isInvert() {
        return getPropertyAsBoolean(INVERT);
    }

    public void setIsRegex(boolean flag) {
        setProperty(ISREGEX, flag);
    }

    public boolean isUseRegex() {
        return getPropertyAsBoolean(ISREGEX, true);
    }

    private void doAssert(String jsonString) {
        Object value = JsonPath.read(jsonString, getJsonPath());

        if (isJsonValidationBool()) {
            if (value instanceof JSONArray) {
                if (arrayMatched((JSONArray) value)) {
                    return;
                }
            } else {
                if (isExpectNull() && value == null) {
                    return;
                } else if (isEquals(value)) {
                    return;
                }
            }

            if (isExpectNull()) {
                throw new RuntimeException(String.format("Value expected to be null, but found '%s'", value));
            } else {
                String msg;
                if (isUseRegex()) {
                    msg="Value expected to match regexp '%s', but it did not match: '%s'";
                } else {
                    msg="Value expected to be '%s', but found '%s'";
                }
                throw new RuntimeException(String.format(msg, getExpectedValue(), JSONPathExtractor.objectToString(value)));
            }
        }
    }

    private boolean arrayMatched(JSONArray value) {
        if (value.isEmpty() && getExpectedValue().equals("[]")) {
            return true;
        }

        for (Object subj : value.toArray()) {
            if (isExpectNull() && subj == null) {
                return true;
            } else if (isEquals(subj)) {
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
        } else {
            return str.equals(getExpectedValue());
        }
    }

    @Override
    public AssertionResult getResult(SampleResult samplerResult) {
        AssertionResult result = new AssertionResult(getName());
        String responseData = samplerResult.getResponseDataAsString();
        if (responseData.isEmpty()) {
            return result.setResultForNull();
        }

        result.setFailure(false);
        result.setFailureMessage("");

        if (!isInvert()) {
            try {
                doAssert(responseData);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Assertion failed", e);
                }
                result.setFailure(true);
                result.setFailureMessage(e.getMessage());
            }
        } else {
            try {
                doAssert(responseData);
                result.setFailure(true);
                if (isJsonValidationBool()) {
                    if (isExpectNull())
                        result.setFailureMessage("Failed that JSONPath " + getJsonPath() + " not matches null");
                    else
                        result.setFailureMessage("Failed that JSONPath " + getJsonPath() + " not matches " + getExpectedValue());
                } else {
                    result.setFailureMessage("Failed that JSONPath not exists: " + getJsonPath());
                }
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Assertion failed", e);
                }
            }
        }
        return result;
    }
}
