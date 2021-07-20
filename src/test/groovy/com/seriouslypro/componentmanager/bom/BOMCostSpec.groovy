package com.seriouslypro.componentmanager.bom


import com.seriouslypro.test.TestResources
import org.springframework.boot.test.OutputCapture
import spock.lang.Specification

class BOMCostSpec extends Specification implements TestResources {

    @org.junit.Rule
    OutputCapture capture = new OutputCapture()

    void setup() {
        System.out.flush()
    }

    def 'cost'() {
        given:
            File configFile = createTemporaryFileFromResource(temporaryFolder, testResource("/test.config"))
            String configFileName = configFile.absolutePath

        and:
            File purchasesFile = copyResourceToTemporaryFolder(temporaryFolder,testResource('/test-purchases.csv'))
            File bomFile = copyResourceToTemporaryFolder(temporaryFolder,testResource('/test-bom.csv'))
            File partMappingsFile = copyResourceToTemporaryFolder(temporaryFolder,testResource('/test-partmappings.csv'))
            File partSubstitutionsFile = copyResourceToTemporaryFolder(temporaryFolder,testResource('/test-partsubstitutions.csv'))

        and:
            String[] args = [
                "-c",
                "-cfg", configFileName,
                "-cu", "USD",
                "-p", purchasesFile.absolutePath,
                "-b", bomFile.absolutePath,
                "-pm", partMappingsFile.absolutePath,
                "-ps", partSubstitutionsFile.absolutePath
            ]

        when:
            Integer returnCode = BOMCost.processArgs(args)

        then:
            noExceptionThrown()

        and:
            String capturedOutput = capture.toString()
            !capturedOutput.empty

        and:
            capturedOutput.contains("Cost: 0.0387 USD")

        and:
            returnCode == 0
    }
}
