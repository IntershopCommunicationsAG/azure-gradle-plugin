package com.intershop.cloudops.azure.tasks

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class GenerateWorkspaceIdTaskSpec extends Specification {
    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()

    Project project
    GenerateWorkspaceIdTask task
    RegularFileProperty idFile

    def setup() {
        project = ProjectBuilder.newInstance()
                .withProjectDir(testProjectDir.root)
                .build()

        task = project.task("taskUnderTest", type: GenerateWorkspaceIdTask)

        idFile = project.layout.fileProperty()
        idFile.set(testProjectDir.newFile("idFile"))
    }

    def "an id with the structure <prefix>-<projectName>-##### is stored"() {
        given:
        task.getIdFile().set(idFile)
        task.setWorkspaceIdPrefix(prefix)
        task.setProjectName(projectName)

        when:
        idFile.get().asFile.delete()
        task.generateWorkspaceId()

        then: "idFile is generated"
        idFile.get().asFile.exists()

        and: "the generted id has an expected value"
        idFile.get().asFile.text.matches(expectedId)

        where:
        prefix | projectName | expectedId
        "a"    | "b"         | "a-b-\\d{5}"
        "a-A"  | "b-B"       | "aa-bb-\\d{5}"
        ""     | ""          | "\\d{5}"
    }
}
