package org.mockito.release.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.mockito.release.exec.Exec
import org.mockito.release.git.Git
import org.mockito.release.steps.Steps

class ReleasePlugin implements Plugin<Project> {
  void apply(Project project) {
    def steps = Steps.newSteps()
    def gitTool = Git.gitTool(Exec.getProcessRunner(project.getProjectDir()))
    project.extensions.create("releaseSteps", ReleaseExtension, steps, gitTool)
    def task = project.tasks.create("release", ReleaseTask)
    task.steps = steps

    project.tasks.addRule("releaseStep<Number> - performs given release step") { String taskName ->
      if (taskName.startsWith("releaseStep")) {
        String number = taskName - "releaseStep"
        def step = steps.getStep(number as int)
        def stepTask = project.tasks.create(taskName)
        stepTask.doLast { step.perform() }
      }
    }

    project.tasks.addRule("rollbackStep<Number> - rolls back given release step") { String taskName ->
      if (taskName.startsWith("rollbackStep")) {
        String number = taskName - "rollbackStep"
        def step = steps.getStep(number as int)
        def stepTask = project.tasks.create(taskName)
        stepTask.doLast {
          def r = step.getRollback()
          if (r == null) {
            logger.lifecycle("No rollback for step {}", step)
          } else {
            r.perform()
          }
        }
      }
    }
  }
}
