package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.AzurePlugin;
import com.intershop.cloudops.azure.tasks.CreateResourceGroupTask;
import com.intershop.cloudops.azure.tasks.GetFileContentTask;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class CreateTestRGTaskBuilder extends AbstractAzureTaskBuilder
{
    public CreateTestRGTaskBuilder(String taskName, Project project,
                    AzureExtension azure)
    {
        super(taskName, project, azure);

        this.taskClass = CreateResourceGroupTask.class;
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);

        GetFileContentTask getWSIdTask = (GetFileContentTask)project.getTasks()
                        .findByName(AzurePlugin.GET_WORKSPACE_ID_TASK);

        CreateResourceGroupTask t = (CreateResourceGroupTask)task;

        t.setDescription("Creates the azure resource group.");

        t.dependsOn(getWSIdTask);

        t.getResourceGroupName().set(getWSIdTask.getContent());
        t.getResourceGroupRegion().set(azure.getResourceGroupRegion());
    }
}
