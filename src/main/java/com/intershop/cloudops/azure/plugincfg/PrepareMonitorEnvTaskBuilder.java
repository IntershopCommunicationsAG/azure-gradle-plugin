package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.AzurePlugin;
import com.intershop.cloudops.azure.tasks.CreateBlobContainerTask;
import com.intershop.cloudops.azure.tasks.CreateResourceGroupTask;
import com.intershop.cloudops.azure.tasks.GetFileContentTask;
import com.intershop.cloudops.azure.tasks.PrepareTestEnvTask;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class PrepareMonitorEnvTaskBuilder extends AbstractTaskBuilder
{
    public PrepareMonitorEnvTaskBuilder(String taskName, Project project,
                    AzureExtension azure)
    {
        super(taskName, project, azure);

        taskClass = PrepareTestEnvTask.class;
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);

        CreateBlobContainerTask createCtnrTask = (CreateBlobContainerTask)project.getTasks()
                        .findByName(AzurePlugin.CREATE_CTNR_TASK);

        CreateResourceGroupTask createRGTask = (CreateResourceGroupTask)project.getTasks()
                        .findByName(AzurePlugin.CREATE_DEPLOY_RG_TASK);

        PrepareTestEnvTask t = (PrepareTestEnvTask)task;

        t.setDescription("Preparation of Azure Monitor Task");

        t.dependsOn(createCtnrTask, createRGTask);

        t.getTestEnvFile().set(azure.getMonitorEnvFile());
        t.getContainerBaseUrl().set(createCtnrTask.getBaseURL());
        t.getContainerDir().set(azure.getDeploymentResourceGroupName());
        t.getSASToken().set(createCtnrTask.getsASToken());
        t.getResourceGroupName().set(azure.getDeploymentResourceGroupName());
        t.getClientId().set(azure.getClientId());
        t.getDomain().set(azure.getDomain());
        t.getSecret().set(azure.getSecret());
        t.getSubscriptionId().set(azure.getDeploymentSubscriptionId());
    }
}
