package com.intershop.cloudops.azure.tasks;

import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.DeploymentMode;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.StopExecutionException;
import org.gradle.api.tasks.TaskAction;

import java.net.URISyntaxException;

public class AzureDeploymentTask extends AbstractAzureTask
{
    final private int TOKEN_TIME_SPAN = 30;
    final private String CONTENT_VERSION = "1.0.0.0";

    final private Property<ResourceGroup> resourceGroup = getProject().getObjects().property(ResourceGroup.class);
    final private Property<CloudBlobContainer> blobContainer = getProject().getObjects()
                    .property(CloudBlobContainer.class);
    final private Property<String> deploymentId = getProject().getObjects().property(String.class);
    final private Property<String> projectDir = getProject().getObjects().property(String.class);
    final private Property<String> versionDir = getProject().getObjects().property(String.class);
    final private Property<String> templateName = getProject().getObjects().property(String.class);
    final private Property<String> parameterFileName = getProject().getObjects().property(String.class);

    final private Property<Deployment> deployment = getProject().getObjects().property(Deployment.class);

    @Input
    public Property<ResourceGroup> getResourceGroup()
    {
        return resourceGroup;
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


    @Input
    public Property<String> getTemplateName()
    {
        return templateName;
    }

    @Input
    public Property<String> getParameterFileName()
    {
        return parameterFileName;
    }

    @Input
    public Property<String> getDeploymentId()
    {
        return deploymentId;
    }

    public Property<Deployment> getDeployment()
    {
        return deployment;
    }

    @TaskAction
    void deploy()
    {
        if (templateName.get().length() == 0)
        {
            throw new GradleException("withTemplate name must not be empty");
        }

        try
        {
            Deployment deployment = null;

            String templateLink = createLink(blobContainer.get(), projectDir.get(), versionDir.get(), templateName.get());

            getLogger().info("templateLink: " + templateLink);

            if (parameterFileName.get().length() == 0)
            {
                deployment = getClient().deployments()
                                .define(deploymentId.get())
                                .withExistingResourceGroup(resourceGroup.get().name())
                                .withTemplateLink(templateLink, CONTENT_VERSION)
                                .withParameters("{}")
                                .withMode(DeploymentMode.INCREMENTAL)
                                .create();
            }
            else
            {
                String parameterLink = createLink(blobContainer.get(), projectDir.get(), versionDir.get(), parameterFileName.get());

                getLogger().info("parameterLink: " + parameterLink);

                deployment = getClient().deployments()
                                .define(deploymentId.get())
                                .withExistingResourceGroup(resourceGroup.get().name())
                                .withTemplateLink(templateLink, CONTENT_VERSION)
                                .withParametersLink(parameterLink, CONTENT_VERSION)
                                .withMode(DeploymentMode.INCREMENTAL)
                                .create();
            }

            getLogger().info("deployment output: " + deployment.outputs().toString());

            this.deployment.set(deployment);
        }
        catch(Exception e)
        {
            throw new GradleException(
                            "withTemplate deployed error (templateName: " + templateName.get() + "):" + e.getMessage());
        }
    }

    private String createLink(CloudBlobContainer blobContainer, String projectDir, String versionDir, String fileName)
                    throws URISyntaxException, StorageException
    {
        String blobRef = new StringBuffer()
                        .append(projectDir)
                        .append("/")
                        .append(versionDir)
                        .append("/")
                        .append(fileName)
                        .toString();

        if (!blobContainer.getBlockBlobReference(blobRef).exists())
        {
            throw new StopExecutionException("blob file does not exists: " + blobRef);
        }

        String link = new StringBuffer()
                        .append(blobContainer.getUri().toASCIIString())
                        .append("/")
                        .append(blobRef)
                        .append("?")
                        .append(generateSASToken(blobContainer, TOKEN_TIME_SPAN))
                        .toString();
        return link;
    }
}
