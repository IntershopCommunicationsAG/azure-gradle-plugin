package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.AzurePlugin;
import com.intershop.cloudops.azure.tasks.CreateBlobContainerTask;
import com.intershop.cloudops.azure.tasks.CreateResourceGroupTask;
import com.intershop.cloudops.azure.tasks.GetFileContentTask;
import com.intershop.cloudops.azure.tasks.PrepareTestEnvTask;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class PrepareTestEnvTaskBuilder extends AbstractTaskBuilder
{
    public PrepareTestEnvTaskBuilder(String taskName, Project project,
                    AzureExtension azure)
    {
        super(taskName, project, azure);

        taskClass = PrepareTestEnvTask.class;
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);

        Task azureTestDeployTask = project.getTasks()
                        .findByName(AzurePlugin.TEST_DEPLOY_TASK);

        CreateBlobContainerTask createCtnrTask = (CreateBlobContainerTask)project.getTasks()
                        .findByName(AzurePlugin.CREATE_CTNR_TASK);

        CreateResourceGroupTask createTestRGTask = (CreateResourceGroupTask)project.getTasks()
                        .findByName(AzurePlugin.CREATE_TEST_RG_TASK);

        GetFileContentTask getWSIdTask = (GetFileContentTask)project.getTasks()
                        .findByName(AzurePlugin.GET_WORKSPACE_ID_TASK);

        PrepareTestEnvTask t = (PrepareTestEnvTask)task;

        t.setDescription("Preparation of Azure Tests");

        t.dependsOn(azureTestDeployTask);

        t.getTestEnvFile().set(azure.getTestEnvFile());
        t.getContainerBaseUrl().set(createCtnrTask.getBaseURL());
        t.getContainerDir().set(getWSIdTask.getContent());
        t.getSASToken().set(createCtnrTask.getsASToken());
        t.getResourceGroupName().set(createTestRGTask.getResourceGroupName());
        t.getClientId().set(azure.getClientId());
        t.getDomain().set(azure.getDomain());
        t.getSecret().set(azure.getSecret());
        t.getSubscriptionId().set(azure.getSubscriptionId());
    }
}
