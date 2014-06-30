package de.draexlmaier.bpm.demo.mit.nonarquillian;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.impl.util.LogUtil;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineTestCase;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
public class InMemoryH2Test extends ProcessEngineTestCase
{
    private static final String PROCESS_DEFINITION_KEY = "processMit";

    private static final String VAR_ITEMS = "items";
    private static final String VAR_ITEM = "item";

    private static final int TASK_COUNT = 5;

    private static final List<String> ITEMS;

    static
    {
        LogUtil.readJavaUtilLoggingConfigFromClasspath();

        ITEMS = new ArrayList<String>(TASK_COUNT);
        for(int i = 1; i <= TASK_COUNT; ++i)
        {
            ITEMS.add(String.valueOf(i));
        }
    }

    @Deployment(resources = "process.bpmn")
    public void testWithProcessVariable() throws InterruptedException
    {
        // Start a process
        startProcess();

        // Search for a process variable: this returns ALL items
        assertEquals(1, queryTasks().processVariableValueEquals(InMemoryH2Test.VAR_ITEM, "1").count());
    }

    @Deployment(resources = "process.bpmn")
    public void testWithTaskVariable() throws InterruptedException
    {
        // Start a process
        startProcess();

        // Search for a process variable: this returns NO item
        assertEquals(1, queryTasks().taskVariableValueEquals(InMemoryH2Test.VAR_ITEM, "1").count());
    }

    private void startProcess()
    {
        this.processEngine
                .getRuntimeService()
                .startProcessInstanceByKey(InMemoryH2Test.PROCESS_DEFINITION_KEY,
                        Collections.<String, Object> singletonMap(InMemoryH2Test.VAR_ITEMS, ITEMS)).getId();
    }

    private TaskQuery queryTasks()
    {
        return this.processEngine.getTaskService().createTaskQuery()
                .processDefinitionKey(InMemoryH2Test.PROCESS_DEFINITION_KEY);
    }
}
