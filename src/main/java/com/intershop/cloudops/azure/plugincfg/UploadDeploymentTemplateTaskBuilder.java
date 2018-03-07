package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.AzurePlugin;
import com.intershop.cloudops.azure.tasks.CreateBlobContainerTask;
import com.intershop.cloudops.azure.tasks.UploadARMTemplatesTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Copy;

public class UploadDeploymentTemplateTaskBuilder extends AbstractAzureTaskBuilder
{
    public UploadDeploymentTemplateTaskBuilder(String taskName, Project project,
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

        Copy azureBuildTask = (Copy)project.getTasks().findByName(AzurePlugin.PROCESS_TEMPLATES_TASK);

        Task testTask = project.getTasks().findByName(AzurePlugin.TEST_TASK);

        UploadARMTemplatesTask t = (UploadARMTemplatesTask)task;

        t.setDescription("Upload arm templates to azure for a deployment.");

        t.dependsOn(createCtnrTask, azureBuildTask, testTask);

        t.getBlobContainer().set(createCtnrTask.getBlobContainer());
        t.getTemplateBuildDir().set(azure.getTemplateBuildDir());
        t.getVersionDir().set(azure.getDeploymentResourceGroupName());
        t.getProjectDir().set(azure.getProjectName());
        t.onlyIf(spec -> {
            return azure.getDeploymentResourceGroupName().get().length() > 0;
        });
    }
}
