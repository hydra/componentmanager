package com.seriouslypro.componentmanager.bom

import com.seriouslypro.test.TestResources
import io.github.joke.spockoutputcapture.OutputCapture
import spock.lang.Specification

class BOMCostSpec extends Specification implements TestResources {

    @OutputCapture capture

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
            File outputCSVFile = new File(temporaryFolder.getRoot(), "output.csv")

        and:
            String[] args = [
                "-c",
                "-cfg", configFileName,
                "-p", purchasesFile.absolutePath,
                "-b", bomFile.absolutePath,
                "-pm", partMappingsFile.absolutePath,
                "-ps", partSubstitutionsFile.absolutePath,
                "-o", outputCSVFile.absolutePath
            ]

            // TODO unmatched entries
            // TODO substitutes file

        and:
            String[] expectedLines = [
                "CAP_0402, 100nF 6.3V 0402 -> CAP_0402, 100nF 50V 0402 -> Manufacturer: Walsin Tech Corp, Part Code: 0402B104K500CT, Supplier: LCSC, Order reference: WM210706790W, Order date: 2021-07-06, Unit price: 0.0024 USD",
                "CAP_0603, 4.7uF 6.3V 0603 10% -> Manufacturer: Samsung Electro-Mechanics, Part Code: CL10A475KQ8NNNC, Supplier: LCSC, Order reference: 20190604YOTN, Order date: 2019-06-04, Unit price: 0.0033 USD",
                "LED_0603, GREEN -> Manufacturer: BrightLed, Part Code: BL-0603-GREEN-A34, Supplier: AliExpress, Order reference: 506908342095201, Order date: 2018-10-18, Unit price: 0.0043 GBP"
            ]

        and:
            String outputCSVContent = '''"REFDES","QUANTITY","NAME","VALUE","SUBSTITUTE_NAME","SUBSTITUTE_VALUE","MANUFACTURER","PART_CODE","SUPPLIER","ORDER_REFERENCE","ORDER_DATE","UNIT_PRICE","LINE_PRICE","CURRENCY"
"C1, C2, C5, C7, C9, C11, C12, C21, C22, C26, C29, C35","12","CAP_0402","100nF 6.3V 0402","CAP_0402","100nF 50V 0402","Walsin Tech Corp","0402B104K500CT","LCSC","WM210706790W","2021-07-06","0.0024","0.0288","USD"
"C3, C8, C23","3","CAP_0603","4.7uF 6.3V 0603 10%","CAP_0603","4.7uF 6.3V 0603 10%","Samsung Electro-Mechanics","CL10A475KQ8NNNC","LCSC","20190604YOTN","2019-06-04","0.0033","0.0099","USD"
"D1, D2","2","LED_0603","GREEN","LED_0603","GREEN","BrightLed","BL-0603-GREEN-A34","AliExpress","506908342095201","2018-10-18","0.0043","0.0086","GBP"
'''

        when:
            Integer returnCode = BOMCost.processArgs(args)

        then:
            noExceptionThrown()

        and:
            String capturedOutput = capture.toString()
            !capturedOutput.empty

        and:
            expectedLines.each { expectedLine ->
                assert capturedOutput.contains(expectedLine)
            }

        and:
            capturedOutput.contains("Cost: [USD:0.0387, GBP:0.0086]")

        and:
            returnCode == 0

        and:
            outputCSVFile.text == outputCSVContent
    }
}
