package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class TestDeployTaskBuilder extends AbstractTaskBuilder
{
    public TestDeployTaskBuilder(String taskName, Project project,
                    AzureExtension azure)
    {
        super(taskName, project, azure);
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);

        task.setDescription("Azure Test Deployment");
    }
}
