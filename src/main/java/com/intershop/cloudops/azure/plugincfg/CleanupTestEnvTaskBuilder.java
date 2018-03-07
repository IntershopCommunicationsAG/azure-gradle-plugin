package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.AzurePlugin;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;

public class CleanupTestEnvTaskBuilder extends AbstractTaskBuilder
{
    public CleanupTestEnvTaskBuilder(String taskName, Project project,
                    AzureExtension azure)
    {
        super(taskName, project, azure);

        taskClass = DefaultTask.class;
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);

        Task deleteTestBlobDirTask = project.getTasks().findByName(AzurePlugin.DELETE_TEST_BLOB_DIR);

        Task deleteTestRGTask = project.getTasks().findByName(AzurePlugin.DELETE_TEST_RG_TASK);

        Task cleanTask = project.getTasks().findByName("clean");

        task.setDescription("Cleanup after a test run");

        task.finalizedBy(deleteTestBlobDirTask, deleteTestRGTask);

        if(cleanTask != null) cleanTask.dependsOn(task);

        task.doLast(new Action() {
            @Override public void execute(Object o)
            {
                File file = azure.getTestEnvFile().get().getAsFile();

                if (file.exists())
                {
                    file.delete();
                }
            }
        });
    }
}
