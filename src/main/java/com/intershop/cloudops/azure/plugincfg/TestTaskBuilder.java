package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.AzurePlugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.testing.Test;

public class TestTaskBuilder extends AbstractTaskBuilder
{
    SourceSet sourceSet;

    public TestTaskBuilder(String taskName, Project project,
                    AzureExtension azure)
    {
        super(taskName, project, azure);

        taskClass = Test.class;
    }

    public TestTaskBuilder withSourceSet(SourceSet s)
    {
        this.sourceSet = s;

        return this;
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);

        Task prepareTestEnvTask = project.getTasks().findByName(AzurePlugin.PREPARE_TEST_ENV_TASK);

        Task cleanupTestEnvTask = project.getTasks().findByName(AzurePlugin.CLEANUP_TEST_ENV_TASK);

        Task triggerTestCleanUpTask = project.getTasks().findByName(AzurePlugin.TRIGGER_CLEAN_TEST_ENV);

        Test t = (Test)task;

        t.setDescription("Test execution of the azureTest source set.");

        t.dependsOn(prepareTestEnvTask, triggerTestCleanUpTask);

        t.finalizedBy(cleanupTestEnvTask);

        t.setClasspath(sourceSet.getRuntimeClasspath());
        t.setTestClassesDirs(sourceSet.getOutput().getClassesDirs());

        t.systemProperty("testEnvPath", azure.getTestEnvFile().get().getAsFile().getAbsolutePath());

    }
}
