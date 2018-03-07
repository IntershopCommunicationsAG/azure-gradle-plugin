package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.AzurePlugin;
import com.intershop.cloudops.azure.tasks.GenerateWorkspaceIdTask;
import com.intershop.cloudops.azure.tasks.GetFileContentTask;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class GetWSITaskBuilder extends AbstractTaskBuilder
{
    public GetWSITaskBuilder(String taskName, Project project,
                    AzureExtension azure)
    {
        super(taskName, project, azure);

        this.taskClass = GetFileContentTask.class;
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);

        GenerateWorkspaceIdTask generateWSIdTask =
                        (GenerateWorkspaceIdTask)project.getTasks().findByName(AzurePlugin.GENERATE_WORKSPACE_ID_TASK);

        GetFileContentTask t = (GetFileContentTask)task;

        t.setDescription("Determine the azure resource group name related for the current workspace");

        t.dependsOn(generateWSIdTask);

        t.getFile().set(azure.getIdFile());
    }
}
