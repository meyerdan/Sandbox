package de.draexlmaier.bpm.process.repro;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.cdi.BusinessProcess;
import org.camunda.bpm.engine.task.Task;

@Singleton
public class TaskPollingEJB implements TaskPollingEJBLocal
{
    @Inject
    private BusinessProcess businessProcess;

    @Inject
    private TaskService taskService;

    @Resource(lookup = "java:/TransactionManager")
    private TransactionManager transactionManager;

    @Override
    public void pollNow()
    {
        for(final Task task : this.taskService.createTaskQuery().list())
        {
            try
            {
                handleTaskInternal(task);
            }
            catch(final Exception ex)
            {
                logIncident(task, ex);
                continue;
            }

            this.taskService.complete(task.getId());
        }
    }

    protected void handleTask(final Task task)
    {
    }

    private void handleTaskInternal(final Task task) throws Exception
    {
        // Suspend current TX and start a new one to process this one task
        final Transaction oldTx = this.transactionManager.suspend();
        this.transactionManager.begin();

        // Associate task (needed in internal code later)
        this.businessProcess.setTask(task);

        try
        {
            // Do the real work
            handleTask(task);

            // Flush data
            this.businessProcess.flushVariableCache();
            this.businessProcess.saveTask();

            this.transactionManager.commit();
        }
        catch(final Exception ex)
        {
            this.transactionManager.rollback();

            throw ex;
        }
        finally
        {
            // Disassociate task
            this.businessProcess.stopTask();

            // Restore old TX (if there)
            if(oldTx != null)
            {
                this.transactionManager.resume(oldTx);
            }
        }
    }

    private void logIncident(final Task task, final Exception ex)
    {
        IncidentLogger.logIncident(task.getExecutionId(), "polling.exception", ex.getMessage());
    }
}
