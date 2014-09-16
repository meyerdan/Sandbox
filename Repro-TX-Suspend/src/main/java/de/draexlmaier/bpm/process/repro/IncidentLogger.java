package de.draexlmaier.bpm.process.repro;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;

public class IncidentLogger
{
    public static void logIncident(final String executionId, final String incidentType, final String message)
    {
        executeCommand(new Command<Void>()
        {
            @Override
            public Void execute(final CommandContext commandContext)
            {
                final ExecutionEntity execution = commandContext.getExecutionManager().findExecutionById(executionId);

                final IncidentEntity incident = IncidentEntity.createAndInsertIncident(incidentType, null, message);
                incident.setExecution(execution);

                return null;
            }
        });
    }

    public static <T> T executeCommand(final Command<T> command)
    {
        final CommandExecutor executor =
                ((ProcessEngineImpl) BpmPlatform.getDefaultProcessEngine()).getProcessEngineConfiguration()
                        .getCommandExecutorTxRequired();

        return executor.execute(command);
    }

}
