package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.AzurePlugin;
import com.intershop.cloudops.azure.tasks.DeleteResourceGroupTask;
import com.intershop.cloudops.azure.tasks.GetFileContentTask;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class DeleteTestRGTaskBuilder extends AbstractAzureTaskBuilder
{
    public DeleteTestRGTaskBuilder(String taskName, Project project,
                    AzureExtension azure)
    {
        super(taskName, project, azure);

        taskClass = DeleteResourceGroupTask.class;
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);

        GetFileContentTask getWSIdTask = (GetFileContentTask)project.getTasks()
                        .findByName(AzurePlugin.GET_WORKSPACE_ID_TASK);

        DeleteResourceGroupTask t = (DeleteResourceGroupTask)task;

        t.setDescription("Deletes the azure test resource group.");

        t.dependsOn(getWSIdTask);

        t.getResourceGroupName().set(getWSIdTask.getContent());

    }
}
