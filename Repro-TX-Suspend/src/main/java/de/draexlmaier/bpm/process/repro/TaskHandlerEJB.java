/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.draexlmaier.bpm.process.repro;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.camunda.bpm.engine.cdi.BusinessProcess;
import org.camunda.bpm.engine.task.Task;

/**
 * @author Daniel Meyer
 *
 */
@Stateless
public class TaskHandlerEJB implements TaskHandlerEJBLocal {

  @Inject
  private BusinessProcess businessProcess;

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public boolean handleTask(Task task) {

    this.businessProcess.setTask(task);

    boolean result = false;

    try {
      // Do the real work
      result = true;

      // Flush data
      this.businessProcess.flushVariableCache();
      this.businessProcess.saveTask();

    } catch(Exception e) {
      logIncident(task, e);

    } finally {
      // Disassociate task
      this.businessProcess.stopTask();

    }

    return result;

  }

  private void logIncident(final Task task, final Exception ex) {

      IncidentLogger.logIncident(task.getExecutionId(), "polling.exception", ex.getMessage());

  }


}
