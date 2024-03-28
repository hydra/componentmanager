package com.seriouslypro.componentmanager.purchasecombiner

import com.seriouslypro.test.TestResources
import io.github.joke.spockoutputcapture.OutputCapture
import spock.lang.Specification

/**
 Example sheet: https://docs.google.com/spreadsheets/d/1ercprkhq2sgZA0bfLPEqt_no3pZPJE9bxUEwaIFa3bo

 Requires valid `credentials\test-credentials.json` that grants this application access to suitable spreadsheet

 @See com.seriouslypro.googlesheets.CredentialFactory
 */
class PurchaseCombinerSpec extends Specification implements TestResources {

    @OutputCapture capture

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
            File sourceDirectoryFarnell = temporaryFolder.newFolder('Farnell')

        and:
            copyResource(sourceDirectoryLCSC, testResource('/LCSC/20210128VAXS.csv'))
            copyResource(sourceDirectoryLCSC, testResource('/LCSC/WM210614632W.csv'))

        and:
            copyResource(sourceDirectoryMouser, testResource('/Mouser/MouserSearch1216PM.csv'))

        and:
            copyResource(sourceDirectoryFarnell, testResource('/Farnell/78254006-ORDERLINEOrderDetail.csv'))

        and:
            String[] args = [
                "-u",
                "-cfg", configFileName,
                "-sd", sourceDirectoryLCSC,
                "-sd", sourceDirectoryMouser,
                "-sd", sourceDirectoryFarnell,
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
