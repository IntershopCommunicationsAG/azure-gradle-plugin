
import com.microsoft.azure.AzureEnvironment
import com.microsoft.azure.credentials.ApplicationTokenCredentials
import com.microsoft.azure.management.Azure
import com.microsoft.azure.management.resources.Deployment
import com.microsoft.azure.management.resources.DeploymentMode
import com.microsoft.azure.management.resources.ResourceGroup
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy
import jdk.nashorn.internal.parser.JSONParser
import org.gradle.internal.impldep.org.testng.internal.PropertiesFile
import spock.lang.Shared
import spock.lang.Specification

class SampleSpec extends Specification {

    @Shared Properties testEnv
    
    def setupSpec() {
        def testEnvPath = System.properties.get("testEnvPath");
        println "testEnvPath: $testEnvPath"

        testEnv = new Properties();

        try {
            InputStream input = new FileInputStream(testEnvPath);

            testEnv.load(input);
        } catch(Exception e) {
            e.printStackTrace()
        }
    }

    def setup() {
    }

    def "test environment is set"() {
        expect:
        null != testEnv.'azureClientId'
        null != testEnv.'azureClientDomain'
        null != testEnv.'azureClientSecret'
        null != testEnv.'azureSubscriptionId'
        null != testEnv.'azureResourceGroupName'
        null != testEnv.'azureContainerBaseURL'
        null != testEnv.'azureContainerDir'
        null != testEnv.'azureSaasToken'
    }
}

