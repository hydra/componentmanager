package com.seriouslypro.componentmanager.purchasecombiner

import com.seriouslypro.test.TestResources
import org.springframework.boot.test.OutputCapture
import spock.lang.Specification

class PurchaseCombinerSpec extends Specification implements TestResources {

    @org.junit.Rule
    OutputCapture capture = new OutputCapture()

    /*
        Example sheet: https://docs.google.com/spreadsheets/d/1ercprkhq2sgZA0bfLPEqt_no3pZPJE9bxUEwaIFa3bo

        Requires valid `credentials\test-credentials.json` that grants this application access to suitable spreadsheet
     */

    void setup() {
        System.out.flush()
    }

    def 'update'() {
        given:

            File configFile = createTemporaryFileFromResource(temporaryFolder, testResource("/test.config"))
            String configFileName = configFile.absolutePath

            String sheetId = "1ercprkhq2sgZA0bfLPEqt_no3pZPJE9bxUEwaIFa3bo"

        and:
            String sourceDirectory = temporaryFolder.getRoot()

        and:
            copyResourceToTemporaryFolder(temporaryFolder,testResource('/20210128VAXS.csv'))
            copyResourceToTemporaryFolder(temporaryFolder,testResource('/WM210614632W.csv'))


        and:
            String[] args = ["-u", "-cfg", configFileName, "-sd", sourceDirectory, "-s", sheetId]

        when:
            Integer returnCode = PurchaseCombiner.processArgs(args)

        then:
            noExceptionThrown()

        and:
            String capturedOutput = capture.toString()
            !capturedOutput.empty

        and:
            !capturedOutput.contains("invalid parameter combinations")

        and:
            returnCode == 0
    }
}
