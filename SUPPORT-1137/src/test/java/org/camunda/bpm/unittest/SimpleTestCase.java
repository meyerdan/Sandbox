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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SimpleTestCase
{

    private final static Logger LOGGER = Logger.getLogger(SimpleTestCase.class.getName());

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
    @Deployment(resources = { "testProcess.bpmn" })
    public void shouldExecuteProcess()
    {

        final ProcessInstance pi = this.runtimeService.startProcessInstanceByKey("testProcess");

        executeJobs(pi.getId());

        final Job job = this.managementService.createJobQuery().singleResult();
        assertNotNull(job);
        assertEquals(0, job.getRetries());

    }

    public void executeJobs(final String processInstanceId)
    {
        while(true)
        {
            // try to find all jobs for this process that have retries left
            final JobQuery jobQuery = this.managementService.createJobQuery().withRetriesLeft().active();
            if(processInstanceId != null)
            {
                jobQuery.processInstanceId(processInstanceId);
            }
            final List<Job> jobs = jobQuery.list();

            // if not jobs found with retries left we are finished
            if((jobs == null) || jobs.isEmpty())
            {
                break;
            }

            // execute each job
            for(final Job job : jobs)
            {
                final String jobId = job.getId();
                try
                {
                    this.managementService.executeJob(jobId);
                }
                catch(final Exception ex)
                {
                    LOGGER.log(Level.WARNING, "Exception thrown while manually executing job " + jobId, ex);
                }
            }
        }

        // find and execute subprocesses of the process
        final List<ProcessInstance> subprocesses =
                this.runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstanceId).list();
        if((subprocesses != null))
        {
            for(final ProcessInstance sub : subprocesses)
            {
                executeJobs(sub.getProcessInstanceId());
            }
        }

    }

}
