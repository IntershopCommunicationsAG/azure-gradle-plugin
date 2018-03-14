package com.intershop.cloudops.azure.tasks

import com.intershop.cloudops.azure.utils.AzureUtils
import com.microsoft.azure.management.Azure
import com.microsoft.azure.management.storage.StorageAccount
import com.microsoft.azure.storage.blob.CloudBlobClient
import com.microsoft.azure.storage.blob.CloudBlobContainer
import com.microsoft.azure.storage.blob.CloudBlockBlob
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

class DeleteUploadDirTaskSpec extends Specification {
    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()

    @Shared AzureUtils azureUtils
    @Shared StorageAccount storage
    @Shared CloudBlobClient blobClient

    Project project
    DeleteUploadDirTask task
    File templateDir
    File sampleFile

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

        task = project.task("taskUnderTest", type: DeleteUploadDirTask)

        azureUtils.configureAzureTask(task)

        templateDir = testProjectDir.newFolder("templates")
        templateDir.mkdirs()
        sampleFile = new File(templateDir, "sample.txt")
        sampleFile << "Hello World"
    }

    def "deleteBlobDir()"() {
        CloudBlobContainer blobContainer = blobClient.getContainerReference("templates")
        blobContainer.createIfNotExists()

        given:
        task.getBlobContainer().set(blobContainer)
        task.getVersionDir().set(versionDir)
        task.getProjectDir().set(projectDir)

        String uploadDir = "$projectDir/$versionDir"

        CloudBlockBlob blob1 = blobContainer.getBlockBlobReference("$uploadDir/foo.txt");
        CloudBlockBlob blob2 = blobContainer.getBlockBlobReference("$uploadDir/sub/foo.txt");

        when:
        blob1.upload(new FileInputStream(sampleFile), sampleFile.size());
        blob2.upload(new FileInputStream(sampleFile), sampleFile.size());

        then:
        blob1.exists()
        blob2.exists()

        when:
        task.deleteBlobDir()

        then: "blobs are deleted"
        !blob1.exists()
        !blob2.exists()

        where:
        projectDir    | versionDir
        "testproject" | "testversion"
    }
}

