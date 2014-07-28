/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations under the License.
 */
package org.camunda.bpm.unittest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SimpleTestCase
{
    @Rule
    public ProcessEngineRule rule = new ProcessEngineRule();

    private RuntimeService runtimeService;
    private ManagementService managementService;

    @Before
    public void setup()
    {
        this.runtimeService = this.rule.getRuntimeService();
        this.managementService = this.rule.getManagementService();
    }

    @Test
    @Deployment(resources = { "testProcess.bpmn", "testCalledProcess.bpmn" })
    public void testCorrectRetries()
    {
        final ProcessInstance pi = this.runtimeService.startProcessInstanceByKey("testProcess");

        final Job job =
                this.managementService.createJobQuery().processInstanceId(pi.getProcessInstanceId()).withRetriesLeft()
                        .active().singleResult();

        assertNotNull(job);
        assertEquals(0, job.getRetries()); // In the process, we have written "R0/PT5M" - as proposed within SUPPORT-998 to have no retries
    }

    @Test
    @Deployment(resources = { "testProcess.bpmn", "testCalledProcess.bpmn" })
    public void testCorrectRetryCountDown()
    {
        final ProcessInstance pi = this.runtimeService.startProcessInstanceByKey("testProcess");

        Job job =
                this.managementService.createJobQuery().processInstanceId(pi.getProcessInstanceId()).withRetriesLeft()
                        .active().singleResult();

        assertNotNull(job);
        assertEquals(3, job.getRetries()); // See testcase above - this is already not OK

        try
        {
            this.managementService.executeJob(job.getId());
        }
        catch(final RuntimeException ex)
        {
            assertEquals("Expected Exception!", ex.getMessage());
        }

        job =
                this.managementService.createJobQuery().processInstanceId(pi.getProcessInstanceId()).withRetriesLeft()
                        .active().singleResult();

        // After failing, the retries left count should have been reduced by one (it is an async task)
        assertNotNull(job);
        assertEquals(2, job.getRetries());
    }
}
