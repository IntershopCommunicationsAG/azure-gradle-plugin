package com.intershop.cloudops.azure.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GetFileContentTask extends DefaultTask
{
    private RegularFileProperty file = getProject().getLayout().fileProperty();

    private Property<String> content = getProject().getObjects().property(String.class);

    @Input
    public RegularFileProperty getFile()
    {
        return this.file;
    }

    public Property<String> getContent()
    {
        return content;
    }

    @TaskAction
    public void readFile()
    {
        try
        {
            String fPath = file.get().getAsFile().getAbsolutePath();

            String contentStr = new String(Files.readAllBytes(Paths.get(fPath)));

            getLogger().info("contentStr: " + contentStr);
            this.content.set(contentStr);
        }
        catch(IOException ioe)
        {
            throw new GradleException("error when reading file: " + ioe.getMessage());
        }
    }

}
