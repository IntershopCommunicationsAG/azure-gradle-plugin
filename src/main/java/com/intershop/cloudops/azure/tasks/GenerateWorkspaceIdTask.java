package com.intershop.cloudops.azure.tasks;

import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GenerateWorkspaceIdTask extends DefaultTask
{
    final private RegularFileProperty idFile = getProject().getLayout().fileProperty();

    final private Property<String> projectName = getProject().getObjects().property(String.class);

    private String workspaceIdPrefix = "test";

    @Input
    public void setWorkspaceIdPrefix(String workspaceIdPrefix)
    {
        this.workspaceIdPrefix = workspaceIdPrefix;
    }

    @Input
    public Property<String> getProjectName()
    {
        return this.projectName;
    }

    @OutputFile
    public RegularFileProperty getIdFile()
    {
        return idFile;
    }

    @TaskAction
    public void generateWorkspaceId()
    {
        if (idFile.getAsFile().get().exists()) {
            throw new StopExecutionException("A workspaceID file already exists. Use 'clean' for deleting it.");
        }

        String wsid = new StringBuilder()
                        .append(this.workspaceIdPrefix.toLowerCase().replaceAll("[^a-z0-9]", ""))
                        .append('-')
                        .append(this.projectName.get().toLowerCase().replaceAll("[^a-z0-9]", ""))
                        .append('-')
                        .append(SdkContext.randomResourceName("", 6))
                        .toString()
                        .replaceAll("^-+", "");

        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(idFile.getAsFile().get()));
            writer.write(wsid);
            writer.close();
        }
        catch(IOException e)
        {
            throw new TaskExecutionException(this, e);
        }
    }

}
