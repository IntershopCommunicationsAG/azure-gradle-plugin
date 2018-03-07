package com.intershop.cloudops.azure.tasks;

import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.FileInputStream;

public class UploadARMTemplatesTask extends AbstractAzureTask
{
    private DirectoryProperty templateBuildDir = getProject().getLayout().directoryProperty();
    private Property<CloudBlobContainer> blobContainer = getProject().getObjects().property(CloudBlobContainer.class);
    private Property<String> versionDir = getProject().getObjects().property(String.class);
    private Property<String> projectDir = getProject().getObjects().property(String.class);

    @Input
    public DirectoryProperty getTemplateBuildDir()
    {
        return templateBuildDir;
    }

    @Input
    public Property<CloudBlobContainer> getBlobContainer()
    {
        return blobContainer;
    }

    @Input
    public Property<String> getVersionDir()
    {
        return versionDir;
    }

    @Input
    public Property<String> getProjectDir()
    {
        return projectDir;
    }


    @TaskAction
    public void uploadARMTemplates()
    {
        CloudBlobContainer container = blobContainer.get();
        String versionDir = this.versionDir.get();
        String projectDir = this.projectDir.get();

        final String uploadDir = new StringBuffer()
                        .append(projectDir)
                        .append("/")
                        .append(versionDir)
                        .append("/")
                        .toString();

        getLogger().info("uploadDir: " + uploadDir);

        // delete old blobs
        try
        {
            for (ListBlobItem blobItem : container.listBlobs(uploadDir))
            {
                if (blobItem instanceof CloudBlob)
                {
                    CloudBlob blob = (CloudBlob)blobItem;
                    blob.deleteIfExists();
                }
            }
        }
        catch(Exception e)
        {
            throw new GradleException("unable to cleanup the Blob Directory: " + e.getMessage());
        }

        templateBuildDir.getAsFileTree().visit((FileTreeElement fte) -> {
            if (!fte.isDirectory())
            {
                try
                {
                    CloudBlockBlob blob = container.getBlockBlobReference(uploadDir + fte.getRelativePath().getPathString());
                    blob.upload(new FileInputStream(fte.getFile()), fte.getSize());
                }
                catch(Exception e)
                {
                    throw new GradleException("ARM templates upload failed: " + e.getMessage());
                }
            }
        });
    }
}
