package com.intershop.cloudops.azure.utils

import com.intershop.cloudops.azure.AzureExtension
import com.intershop.cloudops.azure.tasks.AbstractAzureTask
import com.microsoft.azure.AzureEnvironment
import com.microsoft.azure.credentials.ApplicationTokenCredentials
import com.microsoft.azure.management.Azure
import com.microsoft.azure.management.storage.StorageAccount
import com.microsoft.azure.management.storage.StorageAccountKey
import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.blob.CloudBlobClient
import spock.lang.Specification

class AzureUtils {

    private static AzureUtils instance

    private String clientId
    private String secret
    private String domain
    private String subscriptionId
    private String resourceGroupName
    private String storageAccountName

    final static public AzureUtils getInstance()
    {
        if (instance == null)
            synchronized(this.class) {
               if(instance == null)
                   instance = new AzureUtils()
            }

        return instance
    }

    public AzureUtils() {
        clientId = System.getenv(AzureExtension.CLIENTID_ENV)
        secret = System.getenv(AzureExtension.SECRET_ENV)
        domain = System.getenv(AzureExtension.DOMAIN_ENV)
        subscriptionId = System.getenv(AzureExtension.SUBSCRIPTION_ENV)
        resourceGroupName = System.getenv("AZURE_RESOURCE_GROUP_NAME")
        storageAccountName = System.getenv("AZURE_STORAGE_ACCOUNT_NAME")
    }

    String getClientId() {
        return clientId
    }

    String getSecret() {
        return secret
    }

    String getDomain() {
        return domain
    }

    String getSubscriptionId() {
        return subscriptionId
    }

    String getResourceGroupName() {
        return resourceGroupName
    }

    String getStorageAccountName() {
        return storageAccountName
    }

    void configureAzureTask(AbstractAzureTask task) {
        task.setClientId(clientId)
        task.setSecret(secret)
        task.setDomain(domain)
        task.getSubscriptionId().set(subscriptionId)
    }

    Azure getAzureClient() {
        ApplicationTokenCredentials creds = new ApplicationTokenCredentials(clientId, domain, secret,
                AzureEnvironment.AZURE);

        Azure.authenticate(creds).withSubscription(subscriptionId);
    }

    StorageAccount getStorageAccount() {
        return azureClient.storageAccounts().getByResourceGroup(resourceGroupName, storageAccountName)
    }

    CloudBlobClient getCloudBlobClient() {
        StorageAccountKey sKey = storageAccount.getKeys().get(0)

        String sConnectionStr = new StringBuffer()
                .append("DefaultEndpointsProtocol=https;")
                .append("AccountName=")
                .append(storageAccountName)
                .append(";")
                .append("AccountKey=")
                .append(sKey.value())
                .toString()

        CloudStorageAccount csAccount = CloudStorageAccount.parse(sConnectionStr)

        return csAccount.createCloudBlobClient();
    }
}
