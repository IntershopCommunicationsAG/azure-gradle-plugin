package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.tasks.CreateResourceGroupTask;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class CreateDeploymentRGTaskBuilder extends AbstractAzureTaskBuilder
{
    public CreateDeploymentRGTaskBuilder(String taskName, Project project,
                    AzureExtension azure)
    {
        super(taskName, project, azure);

        this.taskClass = CreateResourceGroupTask.class;
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);

        CreateResourceGroupTask t = (CreateResourceGroupTask)task;

        t.setDescription("Create the deployment resource group, if not exists.");

        t.getSubscriptionId().set(azure.getDeploymentSubscriptionId());
        t.getResourceGroupName().set(azure.getDeploymentResourceGroupName());
        t.getResourceGroupRegion().set(azure.getResourceGroupRegion());

        t.onlyIf(spec -> {
            return azure.getDeploymentResourceGroupName().get().length() > 0;
        });
    }
}
