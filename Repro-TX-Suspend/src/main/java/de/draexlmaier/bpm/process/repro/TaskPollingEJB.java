package de.draexlmaier.bpm.process.repro;

import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;

@Singleton
public class TaskPollingEJB implements TaskPollingEJBLocal
{
    @Inject
    private TaskService taskService;

    @Inject
    private TaskHandlerEJBLocal taskHanler;

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void pollNow()
    {
        for(final Task task : this.taskService.createTaskQuery().list()) {

          taskHanler.handleTask(task);

        }
    }

}
