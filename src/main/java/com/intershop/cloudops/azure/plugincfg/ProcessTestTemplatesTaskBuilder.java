package com.intershop.cloudops.azure.plugincfg;

import com.intershop.cloudops.azure.AzureExtension;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.file.CopySpec;
import org.gradle.api.tasks.Copy;

public class ProcessTestTemplatesTaskBuilder extends AbstractTaskBuilder
{
    private Transformer<String, String> templateRenderer;

    public ProcessTestTemplatesTaskBuilder(String taskName, Project project,
                    AzureExtension azure)
    {
        super(taskName, project, azure);

        taskClass = Copy.class;
    }

    public ProcessTestTemplatesTaskBuilder withTemplateRenderer(Transformer<String, String> renderer)
    {
        this.templateRenderer = renderer;
        return this;
    }

    @Override
    void configure(Task task)
    {
        super.configure(task);

        Copy t = (Copy)task;

        t.setDescription("Prepares azure templates for test deployments.");

        t.from(azure.getTemplateSrcDir(), (CopySpec spec) -> {
            spec.filter(templateRenderer);
        });

        t.from(azure.getTemplateTestSrcDir(), (CopySpec spec) -> {
            spec.filter(templateRenderer);
        });

        t.into(azure.getTemplateTestBuildDir());
    }
}
