package de.draexlmaier.bpm.demo.mit;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

public class TaskEndExecutionListener implements ExecutionListener, ProcessConstants
{
    @Override
    public void notify(final DelegateExecution execution) throws Exception
    {
        System.out.println("ExecutionListener: " + execution.getVariableLocal(VAR_STUFF_LOCAL));
    }
}
