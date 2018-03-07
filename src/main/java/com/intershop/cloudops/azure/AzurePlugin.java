package com.intershop.cloudops.azure;

import com.intershop.cloudops.azure.plugincfg.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.util.HashMap;
import java.util.Map;

public class AzurePlugin implements Plugin<Project>
{
    final static public String PLUGIN_ID = "com.intershop.gradle.plugin.azure";

    final static public String AZURE_EXTENSION = "azure";
    final static public String AZURE_TASK_GROUP = "Azure";

    final static public String GENERATE_WORKSPACE_ID_TASK = "generateWorkspaceId";
    final static public String GET_WORKSPACE_ID_TASK = "getWorkspaceId";
    final static public String CREATE_CTNR_TASK = "getBlobContainer";
    final static public String DELETE_TEST_BLOB_DIR = "deleteTestBlobDir";
    final static public String CREATE_TEST_RG_TASK = "createTestResourceGroup";
    final static public String DELETE_TEST_RG_TASK = "deleteTestResourceGroup";
    final static public String UPLOAD_TEST_TEMPLATE_TASK = "uploadARMTemplatesForTest";
    final static public String PROCESS_TEST_TEMPLATES_TASK = "processARMTemplatesForTest";
    final static public String TEST_TASK = "azureTest";
    final static public String PREPARE_TEST_ENV_TASK = "prepareTestEnvironment";
    final static public String CLEANUP_TEST_ENV_TASK = "azureTestClean";
    final static public String TEST_DEPLOY_TASK = "azureTestDeploy";
    final static public String TRIGGER_CLEAN_TEST_ENV = "triggerCleanTestEnvironment";
    final static public String PROCESS_TEMPLATES_TASK = "processARMTemplates";
    final static public String AZURE_PUBLISH_TASK = "azurePublish";
    final static public String CREATE_DEPLOY_RG_TASK = "createDeploymentResourceGroup";
    final static public String UPLOAD_DEPLOY_TEMPLATE_TASK = "uploadARMTemplatesForDeployment";
    final static public String DEPLOY_TASK = "azureDeploy";

    final static public String AZURE_TEST_COMPILE_CONF = "azureTestCompile";
    final static public String AZURE_TEST_RUNTIME_CONF = "azureTestRuntime";

    final static public String AZURE_TEST_SOURCE_SET = "azureTest";

    private Configuration azureTestCompileConf;
    private Configuration azureRuntimeConf;

    private SourceSet azureTestSourceSet;

    @Override
    public void apply(Project project)
    {
        Map plugins = new HashMap();
        plugins.put("plugin", "groovy");
        project.apply(plugins);

        AzureExtension azure = project.getExtensions().create(AZURE_EXTENSION, AzureExtension.class, project);

        prepareConfigurations(project);

        prepareSourceSet(project);

        prepareTasks(project, azure);

        // register the default deployment
        addDeployment(project, azure, "azuredeploy", "azuredeploy.json", "azuredeploy.parameters.json",
                        "azuredeploy.parameters.json");
    }


    public void addDeployment(Project project, AzureExtension azure, String deploymentId,
                    String template,
                    String parameterFileName,
                    String testParameterFileName)
    {
        assert deploymentId.length() != 0 : "withDeploymentId must not be empty";

        createDeploymentTask(project, azure, deploymentId, template, parameterFileName);
        createTestDeploymentTask(project, azure, deploymentId, template, testParameterFileName);
    }


    private void prepareTasks(Project project, AzureExtension azure)
    {
        new GenerateWSITaskBuilder(GENERATE_WORKSPACE_ID_TASK, project, azure).build();

        new GetWSITaskBuilder(GET_WORKSPACE_ID_TASK, project, azure).build();

        new ProcessTestTemplatesTaskBuilder(PROCESS_TEST_TEMPLATES_TASK, project, azure)
                        .withTemplateRenderer(createTemplateRender(createTestTemplateRendererModel(azure))).build();

        new ProcessTemplatesTaskBuilder(PROCESS_TEMPLATES_TASK, project, azure)
                        .withTemplateRenderer(createTemplateRender(createTemplateRendererModel(azure))).build();

        new CreateTestRGTaskBuilder(CREATE_TEST_RG_TASK, project, azure).build();

        new DeleteTestRGTaskBuilder(DELETE_TEST_RG_TASK, project, azure).build();

        new CreateCtnrTaskBuilder(CREATE_CTNR_TASK, project, azure).build();

        new DeleteTestBlobDirTaskBuilder(DELETE_TEST_BLOB_DIR, project, azure).build();

        new UploadTestTemplateTaskBuilder(UPLOAD_TEST_TEMPLATE_TASK, project, azure).build();

        new TestDeployTaskBuilder(TEST_DEPLOY_TASK, project, azure).build();

        new PrepareTestEnvTaskBuilder(PREPARE_TEST_ENV_TASK, project, azure).build();

        new CleanupTestEnvTaskBuilder(CLEANUP_TEST_ENV_TASK, project, azure).build();

        new TriggerCleanTestEnvTaskBuilder(TRIGGER_CLEAN_TEST_ENV, project, azure).build();

        new TestTaskBuilder(TEST_TASK, project, azure)
                        .withSourceSet(azureTestSourceSet).build();

        new DeployTaskBuilder(DEPLOY_TASK, project, azure).build();

        new PublishTaskBuilder(AZURE_PUBLISH_TASK, project, azure).build();

        new CreateDeploymentRGTaskBuilder(CREATE_DEPLOY_RG_TASK, project, azure).build();

        new UploadDeploymentTemplateTaskBuilder(UPLOAD_DEPLOY_TEMPLATE_TASK, project, azure).build();
    }

    private void createTestDeploymentTask(Project project, AzureExtension azure, String deploymentId, String template,
                    String testParameterFileName)
    {
        String testDeplTaskName = TEST_DEPLOY_TASK + "_" + deploymentId;

        Task oldTask = project.getTasks().findByName(testDeplTaskName);
        if (oldTask != null)
        {
            project.getLogger().info("replacing deployment task: " + testDeplTaskName);
            project.getTasks().remove(oldTask);
        }

        new TestTemplateDeploymentTaskBuilder(testDeplTaskName, project, azure)
                        .withDeploymentId(deploymentId)
                        .withTemplate(template)
                        .withParameterFileName(testParameterFileName)
                        .build();
    }

    private void createDeploymentTask(Project project, AzureExtension azure, String deploymentId, String template,
                    String parameterFileName)
    {
        String deplTaskName = DEPLOY_TASK + "_" + deploymentId;

        Task oldTask = project.getTasks().findByName(deplTaskName);
        if (oldTask != null)
        {
            project.getLogger().info("replacing deployment task: " + deplTaskName);
            project.getTasks().remove(oldTask);
        }

        new TemplateDeploymentTaskBuilder(deplTaskName, project, azure)
                        .withDeploymentId(deploymentId)
                        .withTemplate(template)
                        .withParameterFileName(parameterFileName)
                        .build();
    }

    private void prepareSourceSet(Project project)
    {
        ConfigurationContainer confs = project.getConfigurations();

        JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
        azureTestSourceSet = javaConvention.getSourceSets().create(AZURE_TEST_SOURCE_SET);

        azureTestSourceSet.java(x -> {
        });
        azureTestSourceSet.resources(x -> {
        });

        azureTestSourceSet.setCompileClasspath(project.files(confs.getByName(AZURE_TEST_COMPILE_CONF)));
        azureTestSourceSet.setRuntimeClasspath(
                        project.files(azureTestSourceSet.getOutput(), azureTestSourceSet.getCompileClasspath(),
                                        confs.getByName(AZURE_TEST_RUNTIME_CONF)));
    }

    private void prepareConfigurations(Project project)
    {
        ConfigurationContainer confs = project.getConfigurations();
        azureTestCompileConf = confs.create(AZURE_TEST_COMPILE_CONF, (Configuration c) ->
        {
            c.extendsFrom(confs.getByName("testCompile"));
        });

        azureRuntimeConf = confs.create(AZURE_TEST_RUNTIME_CONF, (Configuration c) ->
        {
            c.extendsFrom(confs.getByName("testRuntime"));
        });
    }

    private Transformer<String, String> createTemplateRender(JtwigModel model)
    {
        return new org.gradle.api.Transformer<String, String>()
        {
            public String transform(String original)
            {
                JtwigTemplate template = JtwigTemplate.inlineTemplate(original);
                return template.render(model);
            }
        };
    }

    private JtwigModel createTemplateRendererModel(AzureExtension azure)
    {
        JtwigModel model = JtwigModel
                        .newModel();

        return model;
    }

    private JtwigModel createTestTemplateRendererModel(AzureExtension azure)
    {
        JtwigModel model = createTemplateRendererModel(azure)
                        .with("RANDOM_TEST_ID", azure.getResourceRandomId());

        return model;
    }
}
