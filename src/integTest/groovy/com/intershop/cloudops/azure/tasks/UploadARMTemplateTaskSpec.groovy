package com.intershop.cloudops.azure.tasks

import com.intershop.cloudops.azure.utils.AzureUtils
import com.microsoft.azure.management.Azure
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext
import com.microsoft.azure.management.storage.StorageAccount
import com.microsoft.azure.storage.blob.CloudBlobClient
import com.microsoft.azure.storage.blob.CloudBlobContainer
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

class UploadARMTemplateTaskSpec extends Specification {
    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()

    @Shared AzureUtils azureUtils
    @Shared StorageAccount storage
    @Shared CloudBlobClient blobClient

    Project project
    UploadARMTemplatesTask task

    CloudBlobContainer blobContainer
    File templatDir

    def setupSpec() {
        azureUtils = AzureUtils.getInstance()
        Azure azure = azureUtils.getAzureClient()
        storage = azureUtils.getStorageAccount()
        blobClient = azureUtils.getCloudBlobClient();
    }

    def setup() {
        project = ProjectBuilder.newInstance()
                .withProjectDir(testProjectDir.root)
                .build()

        task = project.task("taskUnderTest", type: UploadARMTemplatesTask)

        azureUtils.configureAzureTask(task)

        blobContainer = azureUtils.cloudBlobClient.getContainerReference(SdkContext.randomResourceName("upldrmtmplt",15))
        blobContainer.createIfNotExists()

        templatDir = testProjectDir.newFolder("templates")
    }

    def cleanup() {
        blobContainer.delete()
    }

    def "uploadARMTemplates()"() {
        given:
        new File(templatDir, "subdir").mkdirs()
        new File(templatDir, "a.txt").write("foo")
        new File(templatDir, "subdir/b.txt").write("foo")

        task.getTemplateBuildDir().set(templatDir)
        task.getBlobContainer().set(blobContainer)
        task.getProjectDir().set(projectDir)
        task.getVersionDir().set(versionDir)

        when:
        task.uploadARMTemplates()

        then:
        blobContainer.getBlockBlobReference("$projectDir/$versionDir/a.txt").exists()
        blobContainer.getBlockBlobReference("$projectDir/$versionDir/subdir/b.txt").exists()

        where:
        projectDir  | versionDir
        "project"       | "version"
    }

}
