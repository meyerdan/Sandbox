package de.draexlmaier.bpm.demo.mit.nonarquillian;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import net.sf.javagimmicks.collections.ArrayRing;
import net.sf.javagimmicks.collections.Ring;
import net.sf.javagimmicks.collections.RingCursor;

import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineTestCase;

import de.draexlmaier.bpm.demo.mit.ProcessConstants;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
public class InMemoryH2Test extends ProcessEngineTestCase implements ProcessConstants
{
    private static final Ring<String> values = new ArrayRing<String>();

    private static final int TASK_COUNT = 1000;

    static
    {
//        LogUtil.readJavaUtilLoggingConfigFromClasspath();
        values.add("a");
        values.add("b");
        values.add("c");
        values.add("d");
        values.add("e");
    }

    @SuppressWarnings("unchecked")
    @Deployment(resources = "process.bpmn")
    public void test() throws InterruptedException
    {
        // Start a process
        final String processInstanceId =
                this.runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY,
                        Collections.<String, Object> singletonMap(VAR_ITEMS, getMultiInstanceItems())).getId();

        // Query tasks
        final List<Task> tasks = this.taskService.createTaskQuery().list();

        assertNotNull(tasks);
        assertEquals(TASK_COUNT, tasks.size());

        // Set local variables
        final RingCursor<String> ringCursor = values.cursor();
        for(final Task task : tasks)
        {
            this.taskService.setVariableLocal(task.getId(), VAR_STUFF_LOCAL, take(ringCursor, 3));
        }

        final CountDownLatch latch = new CountDownLatch(TASK_COUNT);

        // Complete tasks multi-threaded
        int theadCounter = 0;
        for(final Task task : tasks)
        {
            new Thread("TaskCompleter" + (++theadCounter))
            {
                @Override
                public void run()
                {
                    try
                    {
                        InMemoryH2Test.this.taskService.complete(task.getId());
                    }
                    finally
                    {
                        latch.countDown();
                    }
                }

            }.start();
        }

        // Join with the workers
        latch.await();

        // Some more assertions
        assertNull(this.runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult());

        final List<String> stuffGlobal =
                (List<String>) this.historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(processInstanceId).variableName(VAR_STUFF_GLOBAL).singleResult().getValue();

        assertNotNull(stuffGlobal);
        assertEquals(values.size(), stuffGlobal.size());
        assertTrue(stuffGlobal.containsAll(values));
    }

    private static List<String> getMultiInstanceItems()
    {
        final List<String> result = new ArrayList<String>(TASK_COUNT);

        for(int i = 1; i <= TASK_COUNT; ++i)
        {
            result.add(String.valueOf(i));
        }

        return result;
    }

    private static <E> List<E> take(final RingCursor<E> ringCursor, final int count)
    {
        final List<E> result = new ArrayList<E>(count);

        for(int i = 0; i < count; ++i)
        {
            result.add(ringCursor.next());
        }

        return result;
    }
}
