package de.draexlmaier.bpm.demo.mit.nonarquillian;

import static java.lang.String.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import net.sf.javagimmicks.collections.ArrayRing;
import net.sf.javagimmicks.collections.Ring;
import net.sf.javagimmicks.collections.RingCursor;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.task.Task;

import de.draexlmaier.bpm.demo.mit.ProcessConstants;

public class TestLogic implements ProcessConstants
{
    private static final Ring<String> values = new ArrayRing<>();

    private static final int TASK_COUNT = 100;

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
    public static void test(final ProcessEngine processEngine) throws InterruptedException
    {
        // Start a process
        final String processInstanceId =
                processEngine
                        .getRuntimeService()
                        .startProcessInstanceByKey(PROCESS_DEFINITION_KEY,
                                Collections.<String, Object> singletonMap(VAR_ITEMS, getMultiInstanceItems())).getId();

        // Query tasks
        final List<Task> tasks = processEngine.getTaskService().createTaskQuery().list();

        assertNotNull(tasks);
        assertEquals(TASK_COUNT, tasks.size());

        // Set local variables
        final RingCursor<String> ringCursor = values.cursor();
        for(final Task task : tasks)
        {
            processEngine.getTaskService().setVariableLocal(task.getId(), VAR_STUFF_LOCAL, take(ringCursor, 3));
        }

        final CountDownLatch latch = new CountDownLatch(TASK_COUNT);

        // Complete tasks multi-threaded
        int theadCounter = 0;
        for(final Task task : tasks)
        {
            // ///////////////////////////////////////////// Single threaded //////////////////////////////////////////////////////////////
//            processEngine.getTaskService().complete(task.getId());
//            latch.countDown();

            // ///////////////////////////////////////////// Multi threaded //////////////////////////////////////////////////////////////
            new Thread("TaskCompleter" + (++theadCounter))
            {
                @Override
                public void run()
                {
                    try
                    {
                        processEngine.getTaskService().complete(task.getId());
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
        assertNull(processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId)
                .singleResult());

        final List<String> stuffGlobal =
                (List<String>) processEngine.getHistoryService().createHistoricVariableInstanceQuery()
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
            result.add(valueOf(i));
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
