/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.services.query.events.handlers;

import java.util.Date;
import java.util.Optional;

import org.activiti.engine.ActivitiException;
import org.activiti.services.api.events.ProcessEngineEvent;
import org.activiti.services.query.es.model.TaskES;
import org.activiti.services.query.es.repository.TaskRepository;
import org.activiti.services.query.events.TaskCompletedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskCompletedEventHandler implements QueryEventHandler {

	private final TaskRepository taskRepository;

	@Autowired
	public TaskCompletedEventHandler(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	@Override
	public void handle(ProcessEngineEvent event) {
		TaskCompletedEvent taskCompletedEvent = (TaskCompletedEvent) event;
		TaskES eventTask = taskCompletedEvent.getTask();
		Optional<TaskES> findResult = taskRepository.findById(Long.parseLong(eventTask.getId()));
		if (findResult.isPresent()) {
			TaskES task = findResult.get();
			task.setStatus("COMPLETED");
			task.setLastModified(new Date(taskCompletedEvent.getTimestamp()));
			taskRepository.save(task);
		} else {
			throw new ActivitiException("Unable to find task with id: " + eventTask.getId());
		}
	}

	@Override
	public Class<? extends ProcessEngineEvent> getHandledEventClass() {
		return TaskCompletedEvent.class;
	}
}