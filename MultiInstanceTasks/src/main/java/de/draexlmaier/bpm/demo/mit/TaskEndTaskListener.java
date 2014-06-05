package de.draexlmaier.bpm.demo.mit;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class TaskEndTaskListener implements TaskListener, ProcessConstants
{
    @SuppressWarnings("unchecked")
    @Override
    public void notify(final DelegateTask task)
    {
        final List<String> stuff = (List<String>) task.getVariableLocal(VAR_STUFF_LOCAL);
        System.out.println("TaskListener: " + stuff);

        // Merge into process pipeline
        synchronized(TaskEndTaskListener.class) // TODO: Sync scope too big - should sync over process instance - but how?!?
        {
            List<String> stuffGlobal = (List<String>) task.getVariable(VAR_STUFF_GLOBAL);

            if(stuffGlobal == null)
            {
                stuffGlobal = new ArrayList<String>();
            }

            for(final String stuffItem : stuff)
            {
                if(!stuffGlobal.contains(stuffItem))
                {
                    stuffGlobal.add(stuffItem);
                }
            }

            task.setVariable(VAR_STUFF_GLOBAL, stuffGlobal);
        }
    }
}
