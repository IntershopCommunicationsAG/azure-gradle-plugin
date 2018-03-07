package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.AzurePlugin;
import com.intershop.cloudops.azure.tasks.AbstractAzureTask;
import com.intershop.cloudops.azure.tasks.CreateBlobContainerTask;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class CreateCtnrTaskBuilder extends AbstractAzureTaskBuilder
{
    public CreateCtnrTaskBuilder(String taskName, Project project, AzureExtension azure)
    {
        super(taskName, project, azure);

        this.taskClass = CreateBlobContainerTask.class;
        this.taskName = AzurePlugin.CREATE_CTNR_TASK;
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);

        CreateBlobContainerTask t = (CreateBlobContainerTask)task;

        t.setDescription("Create/Ensure the 'templates' - BlobContainer at the storage account");

        t.getStorageId().set(azure.getStorageId());
        t.getContainerName().set(azure.getStorageContainer());
    }
}
