package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.AzurePlugin;
import com.intershop.cloudops.azure.tasks.CreateBlobContainerTask;
import com.intershop.cloudops.azure.tasks.UploadARMTemplatesTask;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class PublishTaskBuilder extends AbstractAzureTaskBuilder
{
    public PublishTaskBuilder(String taskName, Project project,
                    AzureExtension azure)
    {
        super(taskName, project, azure);

        this.taskClass = UploadARMTemplatesTask.class;
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);

        CreateBlobContainerTask createCtnrTask = (CreateBlobContainerTask)project.getTasks()
                        .findByName(AzurePlugin.CREATE_CTNR_TASK);

        Task processTemplatesTask = project.getTasks().findByName(AzurePlugin.PROCESS_TEMPLATES_TASK);

        Task azureTestTask = project.getTasks().findByName(AzurePlugin.TEST_TASK);

        UploadARMTemplatesTask t = (UploadARMTemplatesTask)task;

        t.setDescription("Publish arm templates.");

        t.dependsOn(createCtnrTask, processTemplatesTask, azureTestTask);

        t.getBlobContainer().set(createCtnrTask.getBlobContainer());
        t.getTemplateBuildDir().set(azure.getTemplateBuildDir());
        t.getVersionDir().set(azure.getVersion());
        t.getProjectDir().set(azure.getProjectName());

        // skip task, if no version is configured or the version is already published
        t.onlyIf(s -> {
            String buildVersion = azure.getVersion().get();
            CloudBlobContainer ctnr = createCtnrTask.getBlobContainer().get();

            try
            {
                return (buildVersion.length() != 0) && !(ctnr.getDirectoryReference(buildVersion).listBlobs().iterator()
                                .hasNext());
            }
            catch(Exception e)
            {
                return false;
            }
        });
    }
}
