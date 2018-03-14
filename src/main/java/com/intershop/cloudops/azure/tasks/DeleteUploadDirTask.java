package com.intershop.cloudops.azure.tasks;

import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ListBlobItem;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class DeleteUploadDirTask extends AbstractAzureTask
{
    private Property<CloudBlobContainer> blobContainer = getProject().getObjects().property(CloudBlobContainer.class);
    private Property<String> projectDir = getProject().getObjects().property(String.class);
    private Property<String> versionDir = getProject().getObjects().property(String.class);

    @Input
    public Property<CloudBlobContainer> getBlobContainer()
    {
        return blobContainer;
    }

    @Input
    public Property<String> getProjectDir()
    {
        return projectDir;
    }

    @Input
    public Property<String> getVersionDir()
    {
        return versionDir;
    }

    @TaskAction
    public void deleteBlobDir()
    {
        CloudBlobContainer container = blobContainer.get();
        String uploadDir = new StringBuffer()
                        .append(projectDir.get())
                        .append("/")
                        .append(versionDir.get())
                        .toString();

        getLogger().info("container: " + container.getUri());
        getLogger().info("uploadDir: " + uploadDir);

        try
        {
            deleteBlobDir(container, uploadDir);
        }
        catch(Exception e)
        {
            throw new GradleException("ARM templates upload failed: " + e.getMessage());
        }

    }
}