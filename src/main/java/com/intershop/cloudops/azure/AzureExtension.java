package com.intershop.cloudops.azure;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

import java.io.File;

import static java.lang.System.getProperty;
import static java.lang.System.getenv;

public class AzureExtension
{
    final static public String ID_FILE_NAME = "azure/.workspaceId";
    final static public String TEST_ENV_FILE_NAME = "azure/testEnv.properties";

    final static public String CLIENTID_ENV = "AZURE_CLIENT_ID";
    final static public String CLIENTID_PRJ = "azureClientId";

    final static public String DOMAIN_ENV = "AZURE_CLIENT_DOMAIN";
    final static public String DOMAIN_PRJ = "azureClientDomain";

    final static public String SECRET_ENV = "AZURE_CLIENT_SECRET";
    final static public String SECRET_PRJ = "azureClientSecret";

    final static public String SUBSCRIPTION_ENV = "AZURE_SUBSCRIPTION_ID";
    final static public String SUBSCRIPTION_PRJ = "azureSubscriptionId";

    final static public String WS_ID_PREFIX_ENV = "AZURE_WORKSPACE_ID_PREFIX";
    final static public String WS_ID_PREFIX_PRJ = "azureWorkspaceIdPrefix";
    final static public String WS_ID_PREFIX_DEFAULT = "test";

    final static public String RESOURCE_RANDOMID_ENV = "AZURE_RESOURCE_RANDOM_ID";
    final static public String RESOURCE_RANDOMID_PRJ = "azureResourceRandomId";
    final static public String RESOURCE_RANDOMID_DEFAULT = "";

    final static public String RG_REGION_ENV = "AZURE_RESOURCEGROUP_REGION";
    final static public String RG_REGION_PRJ = "azureResourceGroupRegion";
    final static public String RG_REGION_DEFAULT = "northeurope";

    final static public String STRG_ID_ENV = "AZURE_STORAGE_ID";
    final static public String STRG_ID_PRJ = "azureStorageId";

    final static public String STRG_CONTAINER_ENV = "AZURE_STORAGE_CONTAINER";
    final static public String STRG_CONTAINER_PRJ = "azureStorageContainer";
    final static public String STRG_CONTAINER_DEFAULT = "templates";

    final static public String AZURE_BUILD_VERSION_ENV = "AZURE_BUILD_VERSION";
    final static public String AZURE_BUILD_VERSION_PRJ = "azureBuildVersion";

    final static public String PROJECT_NAME_ENV = "AZURE_PROJECT_NAME";
    final static public String PROJECT_NAME_PRJ = "azureProjectName";

    final static public String AZURE_DEPLOYMENT_RG_NAME_ENV = "AZURE_DEPLOYMENT_RG_NAME";
    final static public String AZURE_DEPLOYMENT_RG_NAME_PRJ = "azureDeploymentResourceGroupName";
    final static public String AZURE_DEPLOYMENT_RG_NAME_DEFAULT = "";

    final static public String AZURE_DEPLOYMENT_RG_REGION_ENV = "AZURE_DEPLOYMENT_RG_REGION";
    final static public String AZURE_DEPLOYMENT_RG_REGION_PRJ = "azureDeploymentResourceGroupRegion";

    final static public String AZURE_DEPLOYMENT_SUBSCRIPTION_ENV = "AZURE_DEPLOYMENT_SUBSCRIPTION_ID";
    final static public String AZURE_DEPLOYMENT_SUBSCRIPTION_PRJ = "azureDeploymentSubscriptionId";

    final static public String TEMPLATE_SRC_DIR = "src/azure/templates";
    final static public String TEMPLATE_TEST_SRC_DIR = "src/azure/testTemplates";

    final static public String TEMPLATE_BUILD_DIR = "azure/templates";
    final static public String TEMPLATE_TEST_BUILD_DIR = "azure/testTemplates";

    final private RegularFileProperty idFile;
    final private RegularFileProperty testEnvFile;
    final private DirectoryProperty templateSrcDir;
    final private DirectoryProperty templateTestSrcDir;
    final private DirectoryProperty templateBuildDir;
    final private DirectoryProperty templateTestBuildDir;

    final private Property<String> projectName;
    final private Property<String> version;

    final private Property<String> deploymentResourceGroupName;
    final private Property<Region> deploymentResourceGroupRegion;
    final private Property<String> deploymentSubscriptionId;

    private String clientId;
    private String domain;
    private String secret;
    private String subscriptionId;
    private String workspaceIdPrefix;
    private String resourceRandomId;
    private Region resourceGroupRegion;
    private String storageId;
    private String storageContainer;

    private Project project;

    public AzureExtension(Project project)
    {
        this.project = project;

        this.clientId = getVariable(project, CLIENTID_ENV, CLIENTID_PRJ, null);
        this.domain = getVariable(project, DOMAIN_ENV, DOMAIN_PRJ, null);
        this.secret = getVariable(project, SECRET_ENV, SECRET_PRJ, null);
        this.subscriptionId = getVariable(project, SUBSCRIPTION_ENV, SUBSCRIPTION_PRJ, null);

        this.workspaceIdPrefix = getVariable(project, WS_ID_PREFIX_ENV, WS_ID_PREFIX_PRJ,
                        WS_ID_PREFIX_DEFAULT);

        this.resourceRandomId = getVariable(project, RESOURCE_RANDOMID_ENV, RESOURCE_RANDOMID_PRJ,
                        RESOURCE_RANDOMID_DEFAULT);

        this.resourceGroupRegion = getRegion(getVariable(project, RG_REGION_ENV, RG_REGION_PRJ, RG_REGION_DEFAULT));

        this.storageId = getVariable(project, STRG_ID_ENV, STRG_ID_PRJ, null);

        this.storageContainer = getVariable(project, STRG_CONTAINER_ENV, STRG_CONTAINER_PRJ, STRG_CONTAINER_DEFAULT);
        this.version = project.getObjects().property(String.class);
        this.version.set(getVariable(project, AZURE_BUILD_VERSION_ENV, AZURE_BUILD_VERSION_PRJ, ""));

        this.projectName = project.getObjects().property(String.class);
        this.projectName.set(getVariable(project, PROJECT_NAME_ENV, PROJECT_NAME_PRJ, project.getName()));

        this.idFile = project.getLayout().fileProperty();
        this.idFile.set(new File(project.getBuildDir(), ID_FILE_NAME));

        this.testEnvFile = project.getLayout().fileProperty();
        this.testEnvFile.set(new File(project.getBuildDir(), TEST_ENV_FILE_NAME));

        this.templateSrcDir = project.getLayout().directoryProperty();
        this.templateSrcDir.set(new File(project.getRootDir(), TEMPLATE_SRC_DIR));

        this.templateTestSrcDir = project.getLayout().directoryProperty();
        this.templateTestSrcDir.set(new File(project.getRootDir(), TEMPLATE_TEST_SRC_DIR));

        this.templateBuildDir = project.getLayout().directoryProperty();
        this.templateBuildDir.set(new File(project.getBuildDir(), TEMPLATE_BUILD_DIR));

        this.templateTestBuildDir = project.getLayout().directoryProperty();
        this.templateTestBuildDir.set(new File(project.getBuildDir(), TEMPLATE_TEST_BUILD_DIR));

        this.deploymentResourceGroupName = project.getObjects().property(String.class);
        this.deploymentResourceGroupName
                        .set(getVariable(project, AZURE_DEPLOYMENT_RG_NAME_ENV, AZURE_DEPLOYMENT_RG_NAME_PRJ, ""));

        this.deploymentSubscriptionId = project.getObjects().property(String.class);
        this.deploymentSubscriptionId
                        .set(getVariable(project, AZURE_DEPLOYMENT_SUBSCRIPTION_ENV, AZURE_DEPLOYMENT_SUBSCRIPTION_PRJ,
                                        this.subscriptionId));

        this.deploymentResourceGroupRegion = project.getObjects().property(Region.class);
        this.deploymentResourceGroupRegion.set(getRegion(getVariable(project, AZURE_DEPLOYMENT_RG_REGION_ENV,
                        AZURE_DEPLOYMENT_RG_REGION_PRJ, this.resourceGroupRegion.name())));
    }

    public String getClientId()
    {
        return clientId;
    }

    public String getDomain()
    {
        return domain;
    }

    public String getSecret()
    {
        return secret;
    }

    public String getSubscriptionId()
    {
        return subscriptionId;
    }

    public String getResourceRandomId()
    {
        return this.resourceRandomId;
    }

    public Region getResourceGroupRegion()
    {
        return resourceGroupRegion;
    }

    public String getWorkspaceIdPrefix()
    {
        return workspaceIdPrefix;
    }

    public RegularFileProperty getIdFile()
    {
        return idFile;
    }

    public RegularFileProperty getTestEnvFile()
    {
        return testEnvFile;
    }

    public DirectoryProperty getTemplateTestSrcDir()
    {
        return templateTestSrcDir;
    }

    public DirectoryProperty getTemplateSrcDir()
    {
        return templateSrcDir;
    }

    public DirectoryProperty getTemplateBuildDir()
    {
        return templateBuildDir;
    }

    public DirectoryProperty getTemplateTestBuildDir()
    {
        return templateTestBuildDir;
    }

    public String getStorageId()
    {
        return storageId;
    }

    public Property<String> getVersion()
    {
        return this.version;
    }

    public Property<String> getProjectName()
    {
        return this.projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName.set(projectName);
    }

    public String getStorageContainer()
    {
        return this.storageContainer;
    }

    public Property<String> getDeploymentResourceGroupName()
    {
        return deploymentResourceGroupName;
    }

    public Property<Region> getDeploymentResourceGroupRegion()
    {
        return deploymentResourceGroupRegion;
    }

    public void setDeploymentResourceGroupRegion(String label)
    {
        this.deploymentResourceGroupRegion.set(getRegion(label));
    }

    public Property<String> getDeploymentSubscriptionId()
    {
        return deploymentSubscriptionId;
    }

    public void setDeploymentSubscriptionId(String id)
    {
        this.deploymentSubscriptionId.set(id);
    }

    public void setBuildVersion(String version)
    {
        this.version.set(version);
    }

    public void setDeploymentResourceGroupName(String rgName) {
        this.deploymentResourceGroupName.set(rgName);
    }

    public void addDeployment(String deploymentId, String template)
    {
        addDeployment(deploymentId, template, "", "");
    }

    public void addDeployment(String deploymentId, String template, String parameterFileName)
    {
        addDeployment(deploymentId, template, parameterFileName, parameterFileName);
    }

    public void addDeployment(String deploymentId, String template, String parameterFileName,
                    String testParameterFileName)
    {
        AzurePlugin plugin = (AzurePlugin)project.getPlugins().findPlugin(AzurePlugin.class);

        plugin.addDeployment(project, this, deploymentId, template, parameterFileName, testParameterFileName);
    }

    private String getVariable(Project project, String envVar, String projectVar, String defaultValue)
    {
        String value = getenv().get(envVar);

        if (value == null)
        {
            value = getProperty(projectVar);
        }

        if (value == null)
        {
            value = (String)project.getProperties().get(projectVar);
        }

        if (value == null)
        {
            value = defaultValue;
        }

        if (value == null)
        {
            String msg = new StringBuffer()
                            .append("missing environment variable '")
                            .append(envVar)
                            .append("' or property '")
                            .append(projectVar)
                            .toString();

            project.getLogger().error(msg);

            throw new GradleException(msg);
        }
        return (value == null ? "" : value.trim());
    }

    private Region getRegion(String label)
    {
        Region region = Region.findByLabelOrName(label);

        if (region == null)
        {
            String msg = new StringBuffer()
                            .append("invalid azure region: ")
                            .append(label)
                            .toString();

            project.getLogger().error(msg);

            throw new GradleException(msg);
        }

        return (region);
    }

    private Region getRegion(Project project, String rgRegionEnv, String rgRegionPrj, String rgRegionDefault)
    {
        String label = getVariable(project, rgRegionEnv, rgRegionPrj, rgRegionDefault);

        return getRegion(label);
    }
}
