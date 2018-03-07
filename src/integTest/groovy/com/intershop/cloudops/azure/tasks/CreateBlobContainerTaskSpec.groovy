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

class CreateBlobContainerTaskSpec extends Specification {
    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()

    @Shared AzureUtils azureUtils
    @Shared StorageAccount storage
    @Shared CloudBlobClient blobClient

    Project project
    CreateBlobContainerTask task

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

        task = project.task("taskUnderTest", type: CreateBlobContainerTask)

        azureUtils.configureAzureTask(task)
    }

    def "createStrgContainer()"() {
        CloudBlobContainer blobContainer

        given:
        task.getStorageId().set(storage.id())
        task.getContainerName().set(containerName)


        when:
        task.createStrgContainer()
        blobContainer = task.getBlobContainer().get()

        then: "blob is created"
        blobContainer.getName() == containerReference

        and: "remove the blob"
        blobContainer.delete()

        where:
        containerName  | containerReference
        "ctnr01"       | "ctnr01"
        " ctnr02 "     | "ctnr02"
    }

    def "accessing a blob with using getBaseURL() and getSASToken()"() {
        CloudBlobContainer blobContainer
        CloudBlockBlob blob

        String baseUrl
        String token
        URL blobUrl

        given:
        task.getStorageId().set(storage.id())
        task.getContainerName().set(containerName)

        when:
        task.createStrgContainer()
        blobContainer = task.getBlobContainer().get()

        then: "container is created is created"
        blobContainer.getName() == containerName

        when: "upload a blob"
        blob = blobContainer.getBlockBlobReference blobName
        blob.uploadText blobContent

        baseUrl = task.getBaseURL().get()
        token = task.getsASToken().get()

        blobUrl = "$baseUrl/$blobName?$token".toURL()

        then: "blob is available "
        blobUrl.text == blobContent

        and: "remove the container"
        blobContainer.delete()

        where:
        containerName      | blobName   | blobContent
        "crtblbctnrsas01"  | "blob.txt" | "Hello Blob"
    }
}
