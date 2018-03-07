package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.AzurePlugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class TriggerCleanTestEnvTaskBuilder extends AbstractTaskBuilder
{
    public TriggerCleanTestEnvTaskBuilder(String taskName, Project project,
                    AzureExtension azure)
    {
        super(taskName, project, azure);
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);

        Task cleanupTestEnvTask = project.getTasks().findByName(AzurePlugin.CLEANUP_TEST_ENV_TASK);

        Task createTestRGTask = project.getTasks().findByName(AzurePlugin.CREATE_TEST_RG_TASK);

        Task uploadTestTemplateTask = project.getTasks().findByPath(AzurePlugin.UPLOAD_TEST_TEMPLATE_TASK);

        task.setDescription("Ensures the deletion of all test resources after azureTest was called");

        task.finalizedBy(cleanupTestEnvTask);

        createTestRGTask.mustRunAfter(task);

        uploadTestTemplateTask.mustRunAfter(task);
    }
}
