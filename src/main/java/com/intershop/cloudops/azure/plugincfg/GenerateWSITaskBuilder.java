package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.tasks.GenerateWorkspaceIdTask;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class GenerateWSITaskBuilder extends AbstractTaskBuilder
{
    public GenerateWSITaskBuilder(String taskName, Project project,
                    AzureExtension azure)
    {
        super(taskName, project, azure);

        this.taskClass = GenerateWorkspaceIdTask.class;
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);

        GenerateWorkspaceIdTask t = (GenerateWorkspaceIdTask)task;

        t.setDescription("Generate and store an azure resource group name for the current workspace.");
        t.setWorkspaceIdPrefix(azure.getWorkspaceIdPrefix());
        t.getIdFile().set(azure.getIdFile());
        t.getProjectName().set(azure.getProjectName());
    }
}
