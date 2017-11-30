package com.blazemeter.jmeter.threads;

import kg.apc.jmeter.JMeterVariableEvaluator;

public abstract interface ParamsPanel
{
  public abstract void modelToUI(AbstractDynamicThreadGroup paramAbstractDynamicThreadGroup);

  public abstract void UItoModel(AbstractDynamicThreadGroup paramAbstractDynamicThreadGroup, JMeterVariableEvaluator paramJMeterVariableEvaluator);

  public abstract void clearUI();
}