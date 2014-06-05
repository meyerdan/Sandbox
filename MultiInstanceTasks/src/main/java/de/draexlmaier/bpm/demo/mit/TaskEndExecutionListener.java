package de.draexlmaier.bpm.demo.mit;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

public class TaskEndExecutionListener implements ExecutionListener
{
    @Override
    public void notify(final DelegateExecution execution) throws Exception
    {
        System.out.println("ExecutionListener: " + execution.getVariableLocal("stuff"));
    }
}
