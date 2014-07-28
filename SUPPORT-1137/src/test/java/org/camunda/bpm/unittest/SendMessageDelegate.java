package org.camunda.bpm.unittest;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class SendMessageDelegate implements JavaDelegate
{
    public void execute(final DelegateExecution execution) throws Exception
    {
        execution.getProcessEngineServices().getRuntimeService().correlateMessage("startMessage");
    }
}
