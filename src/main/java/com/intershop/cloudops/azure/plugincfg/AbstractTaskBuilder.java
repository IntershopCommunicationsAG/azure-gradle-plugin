package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.AzurePlugin;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;

public abstract class AbstractTaskBuilder
{

    protected String taskName;
    protected Class  taskClass;

    protected Project project;
    protected AzureExtension azure;

    public AbstractTaskBuilder(String taskName, Project project, AzureExtension azure)
    {
        this.project = project;
        this.azure = azure;
        this.taskName = taskName;
        this.taskClass = DefaultTask.class;
    }

    public Task build()
    {
        Task task = project.getTasks().create(taskName, taskClass);
        configure(task);

        return task;
    }

    void configure(Task task) {
        task.setGroup(AzurePlugin.AZURE_TASK_GROUP);
    };
}
