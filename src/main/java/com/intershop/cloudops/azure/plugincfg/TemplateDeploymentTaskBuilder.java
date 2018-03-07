package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.AzurePlugin;
import com.intershop.cloudops.azure.tasks.AzureDeploymentTask;
import com.intershop.cloudops.azure.tasks.CreateBlobContainerTask;
import com.intershop.cloudops.azure.tasks.CreateResourceGroupTask;
import com.intershop.cloudops.azure.tasks.UploadARMTemplatesTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Copy;

public class TemplateDeploymentTaskBuilder extends AbstractAzureTaskBuilder
{
    private String deploymentId;
    private String template;
    private String parameterFileName;

    public TemplateDeploymentTaskBuilder(String taskName, Project project, AzureExtension azure)
    {
        super(taskName, project, azure);

        taskClass = AzureDeploymentTask.class;
    }

    public TemplateDeploymentTaskBuilder withDeploymentId(String deploymentId)
    {
        this.deploymentId = deploymentId;
        return this;
    }

    public TemplateDeploymentTaskBuilder withTemplate(String template)
    {
        this.template = template;
        return this;
    }

    public TemplateDeploymentTaskBuilder withParameterFileName(String parameterFileName)
    {
        this.parameterFileName = parameterFileName;
        return this;
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);

        CreateBlobContainerTask createCtnrTask = (CreateBlobContainerTask)project.getTasks()
                        .findByName(AzurePlugin.CREATE_CTNR_TASK);

        Copy azureBuildTask = (Copy)project.getTasks().findByName(AzurePlugin.PROCESS_TEMPLATES_TASK);

        CreateResourceGroupTask createDeploymentRGTask = (CreateResourceGroupTask)project.getTasks()
                        .findByName(AzurePlugin.CREATE_DEPLOY_RG_TASK);

        UploadARMTemplatesTask uploadDeploymentTemplateTask = (UploadARMTemplatesTask)project.getTasks()
                        .findByName(AzurePlugin.UPLOAD_DEPLOY_TEMPLATE_TASK);

        Task deployTask = project.getTasks().findByName(AzurePlugin.DEPLOY_TASK);

        AzureDeploymentTask t = (AzureDeploymentTask)task;

        t.setDescription("Azure Template deployment: " + deploymentId);

        t.dependsOn(createDeploymentRGTask, uploadDeploymentTemplateTask);
        deployTask.dependsOn(t);

        t.getSubscriptionId().set(azure.getDeploymentSubscriptionId());
        t.getVersionDir().set(azure.getDeploymentResourceGroupName());
        t.getResourceGroup().set(createDeploymentRGTask.getResourceGroup());
        t.getDeploymentId().set(deploymentId);
        t.getBlobContainer().set(createCtnrTask.getBlobContainer());
        t.getProjectDir().set(azure.getProjectName());
        t.getTemplateName().set(template);
        t.getParameterFileName().set(parameterFileName);
    }
}
