package com.seriouslypro.componentmanager.purchasecombiner

import com.seriouslypro.test.TestResources
import org.junit.rules.TemporaryFolder
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
            File sourceDirectoryLCSC = temporaryFolder.newFolder('LCSC')
            File sourceDirectoryMouser = temporaryFolder.newFolder('Mouser')

        and:
            copyResource(sourceDirectoryLCSC, testResource('/LCSC/20210128VAXS.csv'))
            copyResource(sourceDirectoryLCSC, testResource('/LCSC/WM210614632W.csv'))

        and:
            copyResource(sourceDirectoryMouser, testResource('/Mouser/MouserSearch1216PM.csv'))

        and:
            String[] args = [
                "-u",
                "-cfg", configFileName,
                "-sd", sourceDirectoryLCSC,
                "-sd", sourceDirectoryMouser,
                "-s", sheetId
            ]

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
