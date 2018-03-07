package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.tasks.AbstractAzureTask;
import org.gradle.api.Project;
import org.gradle.api.Task;

public abstract class AbstractAzureTaskBuilder extends AbstractTaskBuilder
{
    public AbstractAzureTaskBuilder(String taskName, Project project, AzureExtension azure)
    {
        super(taskName, project, azure);

        this.taskClass = AbstractAzureTask.class;
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);
        AbstractAzureTask t = (AbstractAzureTask)task;

        t.setClientId(azure.getClientId());
        t.setSecret(azure.getSecret());
        t.setDomain(azure.getDomain());
        t.getSubscriptionId().set(azure.getSubscriptionId());
    }
}
