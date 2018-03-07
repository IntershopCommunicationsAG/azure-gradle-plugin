package com.intershop.cloudops.azure.tasks;

import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountKey;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class DeleteBlobContainerTask extends AbstractAzureTask
{
    private Property<String> storageId = getProject().getObjects().property(String.class);
    private Property<String> containerName = getProject().getObjects().property(String.class);

    @Input
    public Property<String> getStorageId()
    {
        return storageId;
    }

    @Input
    public Property<String> getContainerName()
    {
        return containerName;
    }

    @TaskAction
    public void deleteBlobContainer()
    {
        String ctnrName = this.containerName.get();
        String storageId = this.storageId.get();

        StorageAccount sAccount;

        try
        {
            sAccount = getClient().storageAccounts().getById(storageId);
        }
        catch(Exception e)
        {
            throw new GradleException("invalid storageId or storage not found: " + storageId);
        }

        StorageAccountKey sKey = sAccount.getKeys().get(0);

        String sConnectionStr = new StringBuffer()
                        .append("DefaultEndpointsProtocol=https;")
                        .append("AccountName=").append(sAccount.name()).append(";")
                        .append("AccountKey=").append(sKey.value())
                        .toString();

        CloudStorageAccount csAccount = null;
        try
        {
            csAccount = CloudStorageAccount.parse(sConnectionStr);

            CloudBlobClient blobClient = csAccount.createCloudBlobClient();

            CloudBlobContainer container = blobClient.getContainerReference(ctnrName);

            container.deleteIfExists();
        }
        catch(Exception e)
        {
            throw new GradleException("BlobContainer cannot be deleted (" + ctnrName + "):" + e.getMessage());
        }
    }
}
