package de.draexlmaier.bpm.demo.mit.nonarquillian;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineTestCase;

import de.draexlmaier.bpm.demo.mit.ProcessConstants;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
public class InMemoryH2Test extends ProcessEngineTestCase implements ProcessConstants
{
    @Deployment(resources = "process.bpmn")
    public void test() throws InterruptedException
    {
        TestLogic.test(this.processEngine);
    }
}
