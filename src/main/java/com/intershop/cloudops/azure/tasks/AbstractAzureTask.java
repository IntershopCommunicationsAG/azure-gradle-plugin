package com.intershop.cloudops.azure.tasks;

import com.intershop.cloudops.azure.AzureExtension;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

import java.util.*;

public abstract class AbstractAzureTask extends DefaultTask
{
    final private Property<String> subscriptionId = getProject().getObjects().property(String.class);

    @Input
    private String clientId;

    @Input
    private String domain;

    @Input
    private String secret;

    @Input
    public Property<String> getSubscriptionId()
    {
        return this.subscriptionId;
    };

    private Azure client;

    public String getClientId()
    {
        return clientId;
    }

    public void setClientId(String clientId)
    {
        this.clientId = clientId;
    }

    public String getDomain()
    {
        return domain;
    }

    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    public String getSecret()
    {
        return secret;
    }

    public void setSecret(String secret)
    {
        this.secret = secret;
    }

    protected Azure getClient(String subscriptionId)
    {
        ApplicationTokenCredentials creds = new ApplicationTokenCredentials(clientId, domain, secret,
                        AzureEnvironment.AZURE);

        return Azure.authenticate(creds).withSubscription(subscriptionId);
    }

    protected Azure getClient()
    {
        if (client == null)
        {
            synchronized(this)
            {
                if (client == null)
                {
                    client = getClient(subscriptionId.get());
                }
            }
        }

        return (client);
    }

    protected StorageAccount findStorageAccount(String storageId)
    {
        Azure client;

        if(storageId.startsWith("/subscriptions/")) {
            client = getClient(storageId.split("/")[2]);
        } else {
            client = getClient();
        }

        StorageAccount sAccount = client.storageAccounts().getById(storageId);

        if (sAccount == null) {
            throw new GradleException("invalid storageId or storage not found: " + storageId);
        }

        return sAccount;
    }

    protected String generateSASToken(CloudBlobContainer blobContainer, int timeSpan)
    {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, timeSpan);

        SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
        policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));
        policy.setSharedAccessExpiryTime(cal.getTime());

        try
        {
            String token = blobContainer.generateSharedAccessSignature(policy, null);
            return token;
        }
        catch(Exception e)
        {
            throw new GradleException("could not generate SASToken: " + e.getMessage());
        }
    }

    protected void deleteBlobDir(CloudBlobContainer container, String path) throws StorageException
    {
        for (ListBlobItem blobItem : container.listBlobs(path))
        {
            if (blobItem instanceof CloudBlockBlob)
            {
                ((CloudBlockBlob)blobItem).deleteIfExists();
            } else {
                String subdir = blobItem.getUri().getPath().replaceAll("^/[^/]*?/", "");
                deleteBlobDir(container,  subdir);
            }
        }
    }

    static public class Builder {
        private Project project;
        private AzureExtension azure;

        private String clientId;
        private String domain;
        private String secret;
        private String subscriptionId;
    }
}
