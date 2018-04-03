package com.intershop.cloudops.azure

import com.intershop.cloudops.azure.utils.AzureUtils
import com.microsoft.azure.management.Azure
import com.microsoft.azure.management.resources.Deployment
import com.microsoft.azure.management.resources.ResourceGroup
import com.microsoft.azure.management.storage.StorageAccount
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification


class AzurePluginSpec extends Specification {

    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()

    @Shared
    AzureUtils azureUtils

    @Shared
    Azure azure;

    @Shared
    StorageAccount storageAccount;

    File buildFile
    File propertiesFile

    File workspaceIdFile
    File azureSrcDir
    File azureBuildDir
    File azureTestSrcDir
    File azureTestBuildDir

    def setupSpec() {
        azureUtils = AzureUtils.instance

        azure = azureUtils.azureClient
        storageAccount = azureUtils.storageAccount
    }

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        propertiesFile = testProjectDir.newFile('gradle.properties')
        workspaceIdFile = new File(testProjectDir.root, "build/$AzureExtension.ID_FILE_NAME")
        azureSrcDir = new File(testProjectDir.root, AzureExtension.TEMPLATE_SRC_DIR)
        azureTestSrcDir = new File(testProjectDir.root, AzureExtension.TEMPLATE_TEST_SRC_DIR)
        azureBuildDir = new File(testProjectDir.root, "build/${AzureExtension.TEMPLATE_BUILD_DIR}")
        azureTestBuildDir = new File(testProjectDir.root, "build/${AzureExtension.TEMPLATE_TEST_BUILD_DIR}")
    }

    def "workspaceId handling"() {
        def workspaceId

        given:
        setupTestProject(projectName)
        propertiesFile << """
            azureProjectName = mytestproject
        """

        when: "execute 'generateWorkspaceId'"
        def result = gradle 'generateWorkspaceId', '-is'

        then: "task executed successfully"
        result.task(":generateWorkspaceId").outcome == TaskOutcome.SUCCESS

        and: "workspaceId File exists"
        workspaceIdFile.exists()
        workspaceIdFile.text.matches "test-mytestproject-\\d{5}"

        when: "execute 'getWorkspaceId'"
        workspaceId = workspaceIdFile.text
        result = gradle 'getWorkspaceId', '-is'

        then: "'generateWorkspaceId' is called but skipped"
        result.task(":generateWorkspaceId").outcome == TaskOutcome.UP_TO_DATE
        result.task(":getWorkspaceId").outcome == TaskOutcome.SUCCESS

        and: "'getWorkspaceInfo' read the workspaceIF from the file (@see GetFileContentTask)"
        result.output.readLines().any { it.matches("contentStr: " + workspaceId) }

        where:
        projectName = 'testproject01'
    }

    def "create/deletete Test ResourceGroup "() {
        def workspaceId

        given:
        setupTestProject(projectName)
        gradle 'getWorkspaceId'

        expect: 'test resource group does not exist'
        !azure.resourceGroups().contain(workspaceIdFile.text)

        when:
        def result = gradle 'createTestResourceGroup', '-is'

        then:
        result.task(":getWorkspaceId").outcome == TaskOutcome.SUCCESS
        result.task(":createTestResourceGroup").outcome == TaskOutcome.SUCCESS

        and: "a resource group with name equals to the workspaceId is created"
        azure.resourceGroups().contain(workspaceIdFile.text)

        when:
        result = gradle 'deleteTestResourceGroup'

        then:
        result.task(":getWorkspaceId").outcome == TaskOutcome.SUCCESS
        result.task(":deleteTestResourceGroup").outcome == TaskOutcome.SUCCESS

        and: "test resource group is deleted"
        !azure.resourceGroups().contain(workspaceIdFile.text)

        where:
        projectName = 'testproject01'
    }

    def "process ARM templates"() {
        def workspaceId

        given:
        setupTestProject(projectName)

        new File(azureSrcDir,     'a.txt') << 'foo'
        new File(azureTestSrcDir, 'b.txt') << 'bar'

        gradle 'getWorkspaceId'
        workspaceId = workspaceIdFile.text

        when:
        def result = gradle 'processARMTemplates', '-is'

        then: "expected tasks were executed"
        result.task(':processARMTemplates').outcome == TaskOutcome.SUCCESS

        and: "azureBuildDir exists"
        azureBuildDir.exists() && azureBuildDir.isDirectory()

        and: "a.txt from azureSrcDir is processed"
        azureBuildDir.list().contains('a.txt')

        and: "b.txt from azureTestSrcDir is not processed"
        !azureBuildDir.list().contains('b.txt')

        where:
        projectName = 'testproject01'
    }

    def "process ARM templates for test"() {
        def workspaceId

        given:
        setupTestProject(projectName)

        new File(azureSrcDir,     'a.txt') << 'from src'
        new File(azureSrcDir,     'b.txt') << 'from src'
        new File(azureTestSrcDir, 'a.txt') << 'from testSrc'
        new File(azureTestSrcDir, 'c.txt') << 'from testSrc'

        gradle 'getWorkspaceId'
        workspaceId = workspaceIdFile.text

        when:
        def result = gradle 'processARMTemplatesForTest', '-is'

        then: "expected tasks were executed"
        result.task(':processARMTemplatesForTest').outcome == TaskOutcome.SUCCESS

        and: "azureBuildTestDir exists"
        azureTestBuildDir.exists() && azureTestBuildDir.isDirectory()

        and: "resources from srcDir and testSrc were processed"
        azureTestBuildDir.list().contains('a.txt')
        azureTestBuildDir.list().contains('b.txt')
        azureTestBuildDir.list().contains('c.txt')

        and: "a.txt from testSrc is used"
        new File(azureTestBuildDir, 'a.txt').text == 'from testSrc'

        where:
        projectName = 'testproject01'
    }

    def "upload/remove templates for tests"() {

        def workspaceId

        given:
        setupTestProject(projectName)
        gradle 'getWorkspaceId'
        workspaceId = workspaceIdFile.text

        when:
        def result = gradle 'uploadARMTemplatesForTest', '-is'

        then:
        result.task(':getWorkspaceId').outcome == TaskOutcome.SUCCESS
        result.task(':processARMTemplatesForTest').outcome == TaskOutcome.SUCCESS
        result.task(':getBlobContainer').outcome == TaskOutcome.SUCCESS
        result.task(':uploadARMTemplatesForTest').outcome == TaskOutcome.SUCCESS

        and: "all templates from the templateSrcDir are copied into the testBuildDir"
        azureTestSrcDir.listFiles().each { src ->
            def buildPath = src.absolutePath.replace(azureTestSrcDir.absolutePath, azureTestBuildDir.absolutePath)

            azureTestBuildDir.listFiles().any() { bld ->
                (bld.absolutePath == buildPath) && (src.length() == bld.length())
            }
        }

        and: "all templates from the SrcDir are copied into the testBuildDir"
        azureSrcDir.listFiles().each { src ->
            def buildPath = src.absolutePath.replace(azureSrcDir.absolutePath, azureTestBuildDir.absolutePath)

            azureTestBuildDir.listFiles().any() { bld ->
                bld.absolutePath == buildPath
            }
        }

        and: "all files from the test build dir were uploaded"
        // TODO: check, if all files were uploaded
        true

        when:
        result = gradle 'deleteTestBlobDir'

        then:
        // TODO: check, the blob container dir was deleted
        true

        where:
        projectName = 'testproject01'
    }

    def "azureTestDeploy"() {
        def workspaceId

        given:
        setupTestProject(projectName)
        gradle 'getWorkspaceId'
        workspaceId = workspaceIdFile.text

        expect: 'test resource group does not exist'
        !azure.resourceGroups().contain(workspaceId)

        when:
        def result = gradle 'azureTestDeploy', '-is'
        Deployment deployment = azure.deployments().getByResourceGroup(workspaceId, "azuredeploy")

        then: "expected tasks were triggered"
        result.task(":getWorkspaceId")?.outcome == TaskOutcome.SUCCESS
        result.task(":processARMTemplatesForTest")?.outcome == TaskOutcome.SUCCESS
        result.task(":createTestResourceGroup")?.outcome == TaskOutcome.SUCCESS
        result.task(":getBlobContainer")?.outcome == TaskOutcome.SUCCESS
        result.task(":processARMTemplatesForTest")?.outcome == TaskOutcome.SUCCESS
        result.task(":uploadARMTemplatesForTest")?.outcome == TaskOutcome.SUCCESS
        result.task(":azureTestDeploy_azuredeploy")?.outcome == TaskOutcome.SUCCESS
        result.task(":azureTestDeploy")?.outcome == TaskOutcome.SUCCESS


        and: 'test resource group exists'
        azure.resourceGroups().contain(workspaceId)

        and: "default deployment  (azuredeploy) was executed"
        deployment != null
        deployment.provisioningState() == "Succeeded"

        when:
        result = gradle 'clean'

        then:
        !azure.resourceGroups().contain(workspaceId)

        where:
        projectName = 'testproject01'
    }

    def "auzureTest"() {
        def workspaceId

        given:
        setupTestProject(projectName)
        gradle 'getWorkspaceId'
        workspaceId = workspaceIdFile.text

        when:
        def result = gradle 'azureTest', '-s'

        then: "check task execution"
        result.task(":triggerCleanTestEnvironment")?.outcome ==TaskOutcome.UP_TO_DATE
        result.task(":azureTestDeploy")?.outcome == TaskOutcome.SUCCESS
        result.task(":prepareTestEnvironment")?.outcome == TaskOutcome.SUCCESS
        result.task(":azureTest")?.outcome == TaskOutcome.SUCCESS
        result.task(":azureTestClean")?.outcome == TaskOutcome.SUCCESS
        result.task(":deleteTestBlobDir")?.outcome == TaskOutcome.SUCCESS
        result.task(":deleteTestResourceGroup")?.outcome == TaskOutcome.SUCCESS

        where:
        projectName = 'testproject01'
    }

    def "azurePublish"() {
        given:
        setupTestProject(projectName)
        propertiesFile << """
            azureBuildVersion = 0.9
        """

        when:
        def result = gradle 'azurePublish', '-i', '-x', 'azureTest'

        then: "expected tasks were triggered"
        result.task(":getBlobContainer")?.outcome == TaskOutcome.SUCCESS
        result.task(":processARMTemplates")?.outcome == TaskOutcome.SUCCESS
        result.task(":azurePublish")?.outcome == TaskOutcome.SUCCESS

        //TODO: check the storage account

        where:
        projectName = 'testproject01'
    }

    def "azureMonitor"() {
        given:
        setupTestProject(projectName)
        propertiesFile << """
            azureBuildVersion = 0.9
            azureDeploymentResourceGroupName = ${azureUtils.resourceGroupName}
        """

        when:
        def result = gradle 'azureDeploy', '-is', '-x', 'azureTest'

        then: "expected tasks were triggered"
        result.task(":azureDeploy")?.outcome == TaskOutcome.SUCCESS

        when:
        result = gradle 'azureMonitor', '-is'

        then:
        true

        where:
        projectName = 'testproject02'
    }


    private setupTestProject(def projectName) {
        new AntBuilder().sequential {
            delete(dir: testProjectDir.root.toPath())

            copy(toDir: testProjectDir.root.toPath()) {
                fileset(dir: new File(this.class.classLoader.getResource(projectName).file).absolutePath) {
                    include:
                    ('**/*')
                }
            }
        }

        propertiesFile << """
            azureStorageId = ${storageAccount.id()}
        """
    }

    private def gradle(String[] args) {
        return GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(args)
                .withPluginClasspath()
                .build()
    }
}