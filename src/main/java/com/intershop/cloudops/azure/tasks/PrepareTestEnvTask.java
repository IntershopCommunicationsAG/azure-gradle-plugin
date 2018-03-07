package com.intershop.cloudops.azure.tasks;

import com.intershop.cloudops.azure.AzureExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

public class PrepareTestEnvTask extends DefaultTask
{
    // TODO: deployment outputs should be stored and handed over to the test task

    final private RegularFileProperty testEnvFile = getProject().getLayout().fileProperty();
    final private Property<String> clientId = getProject().getObjects().property(String.class);
    final private Property<String> secret = getProject().getObjects().property(String.class);
    final private Property<String> domain = getProject().getObjects().property(String.class);
    final private Property<String> subscriptionId = getProject().getObjects().property(String.class);

    final private Property<String> resourceGroupName = getProject().getObjects().property(String.class);
    final private Property<String> containerBaseUrl = getProject().getObjects().property(String.class);
    final private Property<String> containerDir = getProject().getObjects().property(String.class);
    final private Property<String> sasToken = getProject().getObjects().property(String.class);

    @Input
    public RegularFileProperty getTestEnvFile()
    {
        return testEnvFile;
    }

    @Input
    public Property<String> getClientId()
    {
        return clientId;
    }

    @Input
    public Property<String> getSecret()
    {
        return secret;
    }

    @Input
    public Property<String> getDomain()
    {
        return domain;
    }

    @Input
    public Property<String> getSubscriptionId()
    {
        return subscriptionId;
    }

    @Input
    public Property<String> getResourceGroupName()
    {
        return resourceGroupName;
    }

    @Input
    public Property<String> getContainerDir()
    {
        return containerDir;
    }

    @Input
    public Property<String> getContainerBaseUrl()
    {
        return containerBaseUrl;
    }

    @Input
    public Property<String> getSASToken()
    {
        return sasToken;
    }

    @TaskAction
    public void prepareTestEnv()
    {
        Properties env = new Properties();

        env.setProperty(AzureExtension.CLIENTID_PRJ, this.clientId.get());
        env.setProperty(AzureExtension.SECRET_PRJ, this.secret.get());
        env.setProperty(AzureExtension.DOMAIN_PRJ, this.domain.get());
        env.setProperty(AzureExtension.SUBSCRIPTION_PRJ, this.subscriptionId.get());
        env.setProperty("azureResourceGroupName", this.resourceGroupName.get());
        env.setProperty("azureContainerBaseURL", this.containerBaseUrl.get());
        env.setProperty("azureContainerDir", this.containerDir.get());
        env.setProperty("azureSaasToken", this.sasToken.get());

        try
        {
            File file = this.testEnvFile.get().getAsFile();
            FileOutputStream fileOut = new FileOutputStream(file);
            env.store(fileOut, "Test Environment");
            fileOut.close();
        }
        catch(Exception e)
        {
            throw new GradleException("failed to store test environment properties: " + e.getMessage());
        }
    }
}
