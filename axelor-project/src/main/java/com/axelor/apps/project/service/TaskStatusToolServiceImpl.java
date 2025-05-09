/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.db.TaskStatusProgressByCategory;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.TaskStatusRepository;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppProject;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TaskStatusToolServiceImpl implements TaskStatusToolService {

  protected AppProjectService appProjectService;
  protected TaskStatusProgressByCategoryService taskStatusProgressByCategoryService;
  protected TaskStatusRepository taskStatusRepository;

  @Inject
  public TaskStatusToolServiceImpl(
      AppProjectService appProjectService,
      TaskStatusProgressByCategoryService taskStatusProgressByCategoryService,
      TaskStatusRepository taskStatusRepository) {
    this.appProjectService = appProjectService;
    this.taskStatusProgressByCategoryService = taskStatusProgressByCategoryService;
    this.taskStatusRepository = taskStatusRepository;
  }

  @Override
  public Optional<TaskStatus> getCompletedTaskStatus(Project project, ProjectTask projectTask) {
    if (project == null
        || projectTask == null
        || project.getTaskStatusManagementSelect()
            == ProjectRepository.TASK_STATUS_MANAGEMENT_NONE) {
      return Optional.empty();
    }

    AppProject appProject = appProjectService.getAppProject();
    boolean enableTaskStatusManagementByCategory = false;
    if (appProject != null) {
      enableTaskStatusManagementByCategory = appProject.getEnableStatusManagementByTaskCategory();
    }

    if (enableTaskStatusManagementByCategory
        && project.getTaskStatusManagementSelect()
            == ProjectRepository.TASK_STATUS_MANAGEMENT_CATEGORY
        && projectTask.getProjectTaskCategory() != null
        && projectTask.getProjectTaskCategory().getCompletedTaskStatus() != null) {
      return Optional.ofNullable(projectTask.getProjectTaskCategory().getCompletedTaskStatus());
    }

    if (Arrays.asList(
                ProjectRepository.TASK_STATUS_MANAGEMENT_PROJECT,
                ProjectRepository.TASK_STATUS_MANAGEMENT_CATEGORY)
            .contains(project.getTaskStatusManagementSelect())
        && project.getCompletedTaskStatus() != null) {
      return Optional.ofNullable(project.getCompletedTaskStatus());
    }

    return Optional.empty();
  }

  @Override
  public Set<TaskStatus> getTaskStatusSet(Project project, ProjectTask projectTask) {
    if (project == null
        || projectTask == null
        || project.getTaskStatusManagementSelect()
            == ProjectRepository.TASK_STATUS_MANAGEMENT_NONE) {
      return null;
    }

    AppProject appProject = appProjectService.getAppProject();
    boolean enableTaskStatusManagementByCategory = false;
    if (appProject != null) {
      enableTaskStatusManagementByCategory = appProject.getEnableStatusManagementByTaskCategory();
    }

    if (enableTaskStatusManagementByCategory
        && project.getTaskStatusManagementSelect()
            == ProjectRepository.TASK_STATUS_MANAGEMENT_CATEGORY
        && projectTask.getProjectTaskCategory() != null) {
      return projectTask.getProjectTaskCategory().getProjectTaskStatusSet();
    }

    if (project.getTaskStatusManagementSelect()
        == ProjectRepository.TASK_STATUS_MANAGEMENT_PROJECT) {
      return project.getProjectTaskStatusSet();
    }

    return null;
  }

  @Override
  public String checkCompletedTaskStatus(Project project, ProjectTask projectTask) {
    if (project == null
        || projectTask == null
        || project.getTaskStatusManagementSelect()
            == ProjectRepository.TASK_STATUS_MANAGEMENT_NONE) {
      return "";
    }

    boolean enableTaskStatusManagementByCategory =
        Optional.ofNullable(appProjectService.getAppProject())
            .map(AppProject::getEnableStatusManagementByTaskCategory)
            .orElse(false);

    if (enableTaskStatusManagementByCategory
        && project.getTaskStatusManagementSelect()
            == ProjectRepository.TASK_STATUS_MANAGEMENT_CATEGORY
        && projectTask.getProjectTaskCategory() != null
        && projectTask.getProjectTaskCategory().getCompletedTaskStatus() == null) {
      return I18n.get(
          ProjectExceptionMessage.CATEGORY_COMPLETED_TASK_STATUS_MISSING_WITHOUT_DEFAULT_STATUS);
    }

    if (project.getTaskStatusManagementSelect() == ProjectRepository.TASK_STATUS_MANAGEMENT_PROJECT
        && project.getCompletedTaskStatus() == null) {
      return I18n.get(
          ProjectExceptionMessage.PROJECT_COMPLETED_TASK_STATUS_MISSING_WITHOUT_DEFAULT_STATUS);
    }

    return "";
  }

  @Override
  public List<TaskStatusProgressByCategory> getUnmodifiedTaskStatusProgressByCategoryList(
      TaskStatus taskStatus) {
    AppProject appProject = appProjectService.getAppProject();
    if (taskStatus == null
        || taskStatus.getId() == null
        || appProject == null
        || !appProject.getEnableStatusManagementByTaskCategory()
        || !appProject.getSelectAutoProgressOnProjectTask()) {
      return new ArrayList<>();
    }

    taskStatus = taskStatusRepository.find(taskStatus.getId());
    return Query.of(TaskStatusProgressByCategory.class)
        .filter("self.taskStatus.id = :taskStatusId AND self.isCustomized = false")
        .bind("taskStatusId", taskStatus.getId())
        .fetch();
  }

  @Override
  public void updateExistingProgressOnCategory(TaskStatus taskStatus) {
    List<TaskStatusProgressByCategory> taskStatusProgressByCategoryList =
        getUnmodifiedTaskStatusProgressByCategoryList(taskStatus);
    if (!ObjectUtils.isEmpty(taskStatusProgressByCategoryList)) {
      taskStatusProgressByCategoryService.updateExistingProgressWithValue(
          taskStatusProgressByCategoryList, taskStatus.getDefaultProgress());
    }
  }
}
