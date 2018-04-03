package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import com.intershop.cloudops.azure.AzurePlugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.testing.Test;

import java.io.File;

public class MonitorTaskBuilder extends AbstractTaskBuilder
{
    SourceSet sourceSet;

    public MonitorTaskBuilder(String taskName, Project project,
                    AzureExtension azure)
    {
        super(taskName, project, azure);

        taskClass = Test.class;
    }

    public MonitorTaskBuilder withSourceSet(SourceSet s)
    {
        this.sourceSet = s;

        return this;
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);

        Task prepareMonitorEnvTask = project.getTasks().findByName(AzurePlugin.PREPARE_MONITOR_ENV_TASK);

        Test t = (Test)task;

        t.setDescription("Test execution of the azureTest source set against production deployment.");

        t.dependsOn(prepareMonitorEnvTask);

        t.setClasspath(sourceSet.getRuntimeClasspath());
        t.setTestClassesDirs(sourceSet.getOutput().getClassesDirs());

        t.systemProperty("testEnvPath", azure.getMonitorEnvFile().get().getAsFile().getAbsolutePath());

        t.setBinResultsDir(new File(project.getBuildDir(), "monitor-test-results/binary/test"));

        t.getReports().getHtml().setDestination(new File(project.getBuildDir(), "reports/monitor-test-results" ));
        t.getReports().getJunitXml().setDestination(new File(project.getBuildDir(), "monitor-test-results" ));
    }
}
