package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.AzurePlugin;
import com.intershop.cloudops.azure.tasks.*;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Copy;

public class TestTemplateDeploymentTaskBuilder extends AbstractAzureTaskBuilder
{
    private String deploymentId;
    private String template;
    private String parameterFileName;

    public TestTemplateDeploymentTaskBuilder(String taskName, Project project,
                    AzureExtension azure)
    {
        super(taskName, project, azure);

        taskClass = AzureDeploymentTask.class;
    }

    public TestTemplateDeploymentTaskBuilder withDeploymentId(String deploymentId)
    {
        this.deploymentId = deploymentId;
        return this;
    }

    public TestTemplateDeploymentTaskBuilder withTemplate(String template)
    {
        this.template = template;
        return this;
    }

    public TestTemplateDeploymentTaskBuilder withParameterFileName(String parameterFileName)
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

        Copy azureTestBuildTask = (Copy)project.getTasks().findByName(AzurePlugin.PROCESS_TEST_TEMPLATES_TASK);

        GetFileContentTask getWSIdTask = (GetFileContentTask)project.getTasks()
                        .findByName(AzurePlugin.GET_WORKSPACE_ID_TASK);

        CreateResourceGroupTask createTestRGTask = (CreateResourceGroupTask)project.getTasks()
                        .findByName(AzurePlugin.CREATE_TEST_RG_TASK);

        UploadARMTemplatesTask uploadTestTemplateTask = (UploadARMTemplatesTask)project.getTasks()
                        .findByName(AzurePlugin.UPLOAD_TEST_TEMPLATE_TASK);

        Task testDeployTask = project.getTasks().findByName(AzurePlugin.TEST_DEPLOY_TASK);

        AzureDeploymentTask t = (AzureDeploymentTask)task;

        t.setDescription("Azure test template deployment: " + deploymentId);

        t.dependsOn(createTestRGTask, uploadTestTemplateTask);
        testDeployTask.dependsOn(t);

        t.getResourceGroup().set(createTestRGTask.getResourceGroup());
        t.getDeploymentId().set(deploymentId);
        t.getBlobContainer().set(createCtnrTask.getBlobContainer());
        t.getProjectDir().set(azure.getProjectName());
        t.getVersionDir().set(getWSIdTask.getContent());
        t.getTemplateName().set(template);
        t.getParameterFileName().set(parameterFileName);

    }
}
