package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.AzurePlugin;
import com.intershop.cloudops.azure.tasks.CreateBlobContainerTask;
import com.intershop.cloudops.azure.tasks.GetFileContentTask;
import com.intershop.cloudops.azure.tasks.UploadARMTemplatesTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Copy;

public class UploadTestTemplateTaskBuilder extends AbstractAzureTaskBuilder
{
    public UploadTestTemplateTaskBuilder(String taskName, Project project,
                    AzureExtension azure)
    {
        super(taskName, project, azure);

        taskClass = UploadARMTemplatesTask.class;
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);

        CreateBlobContainerTask createCtnrTask = (CreateBlobContainerTask)project.getTasks()
                        .findByName(AzurePlugin.CREATE_CTNR_TASK);

        Copy azureTestBuildTask = (Copy)project.getTasks().findByName(AzurePlugin.PROCESS_TEST_TEMPLATES_TASK);

        GetFileContentTask getWSIdTask = (GetFileContentTask)project.getTasks()
                        .findByName(AzurePlugin.GET_WORKSPACE_ID_TASK);

        UploadARMTemplatesTask t = (UploadARMTemplatesTask)task;

        t.setDescription("Upload arm templates for a test deployment.");

        t.dependsOn(createCtnrTask, azureTestBuildTask, getWSIdTask);

        t.getBlobContainer().set(createCtnrTask.getBlobContainer());
        t.getTemplateBuildDir().set(azure.getTemplateTestBuildDir());
        t.getVersionDir().set(getWSIdTask.getContent());
        t.getProjectDir().set(azure.getProjectName());
    }
}
