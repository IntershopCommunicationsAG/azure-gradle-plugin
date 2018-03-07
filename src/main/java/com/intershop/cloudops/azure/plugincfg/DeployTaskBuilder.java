package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.AzurePlugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class DeployTaskBuilder extends AbstractTaskBuilder
{
    public DeployTaskBuilder(String taskName, Project project,
                    AzureExtension azure)
    {
        super(taskName, project, azure);
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);

        task.setDescription("Azure deployment");
    }
}
