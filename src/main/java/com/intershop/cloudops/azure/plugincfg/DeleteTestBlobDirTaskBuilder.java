package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.AzurePlugin;
import com.intershop.cloudops.azure.tasks.CreateBlobContainerTask;
import com.intershop.cloudops.azure.tasks.DeleteUploadDirTask;
import com.intershop.cloudops.azure.tasks.GetFileContentTask;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class DeleteTestBlobDirTaskBuilder extends AbstractAzureTaskBuilder
{
    public DeleteTestBlobDirTaskBuilder(String taskName, Project project,
                    AzureExtension azure)
    {
        super(taskName, project, azure);

        taskClass = DeleteUploadDirTask.class;
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);

        CreateBlobContainerTask createCtnrTask = (CreateBlobContainerTask)project.getTasks()
                        .findByName(AzurePlugin.CREATE_CTNR_TASK);

        GetFileContentTask getWSIdTask = (GetFileContentTask)project.getTasks()
                        .findByName(AzurePlugin.GET_WORKSPACE_ID_TASK);

        DeleteUploadDirTask t = (DeleteUploadDirTask)task;

        t.setDescription("Removes templates, uploaded for test deployment, from the 'templates 'BlobContainer.");

        t.dependsOn(createCtnrTask, getWSIdTask);

        t.getBlobContainer().set(createCtnrTask.getBlobContainer());
        t.getProjectDir().set(azure.getProjectName());
        t.getVersionDir().set(getWSIdTask.getContent());
    }
}
