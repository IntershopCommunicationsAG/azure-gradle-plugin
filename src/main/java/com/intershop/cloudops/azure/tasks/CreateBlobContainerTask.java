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

public class CreateBlobContainerTask extends AbstractAzureTask
{
    private int tokenTimeSpan = 60;

    private Property<String> storageId = getProject().getObjects().property(String.class);
    private Property<String> containerName = getProject().getObjects().property(String.class);
    private Property<String> sASToken = getProject().getObjects().property(String.class);
    private Property<String> baseURL = getProject().getObjects().property(String.class);

    private Property<CloudBlobContainer> blobContainer = getProject().getObjects().property(CloudBlobContainer.class);

    public Property<CloudBlobContainer> getBlobContainer()
    {
        return blobContainer;
    }

    @Input
    public void setTokenTimeSpan(int tokenTimeSpan)
    {
        this.tokenTimeSpan = tokenTimeSpan;
    }

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

    public Property<String> getsASToken()
    {
        return sASToken;
    }

    public Property<String> getBaseURL()
    {
        return baseURL;
    }

    @TaskAction
    public void createStrgContainer()
    {
        String ctnrName = this.containerName.get().trim().toLowerCase();
        if (ctnrName.length() == 0)
        {
            throw new GradleException("containerName must not be empty");
        }

        String storageId = this.storageId.get().trim();

        StorageAccount sAccount;

        sAccount = findStorageAccount(storageId);

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

            container.createIfNotExists();

            this.blobContainer.set(container);
        }
        catch(Exception e)
        {
            throw new GradleException("BlobContainer cannot be created (" + ctnrName + "):" + e.getMessage());
        }

        this.sASToken.set(generateSASToken(blobContainer.get(), tokenTimeSpan));

        this.baseURL.set(blobContainer.get().getUri().toString());
    }
}
