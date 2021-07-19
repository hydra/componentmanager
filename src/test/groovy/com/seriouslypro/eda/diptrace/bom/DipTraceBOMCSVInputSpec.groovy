package com.seriouslypro.eda.diptrace.bom

import com.seriouslypro.csv.CSVInput
import com.seriouslypro.csv.CSVInputContext
import com.seriouslypro.eda.BOMItem
import spock.lang.Specification

class DipTraceBOMCSVInputSpec extends Specification {

    def 'parse'() {
        given:
            String inputFileName = "test"
            String content = '"RefDes";"Value";"Name";"Quantity";"Manufacturer";"Datasheet";"Number of Pins";"Pattern"\n' +
                '"C1, C2, C5, C7, C9, C11, C12, C21, C22, C26, C29, C35";"100nF 6.3V 0402";"CAP_0402";"12";"";"";"2";"CAP_0402_201906"\n' +
                '"C3, C8, C23";"4.7uF 6.3V 0603 10%";"CAP_0603";"3";"TDK";"http://www.farnell.com/datasheets/2291921.pdf";"2";"CAP_0603"'
            Reader reader = new StringReader(content)

        and:
            List<BOMItem> expectedBOMItems = [
                new BOMItem(refdesList:['C1', 'C2', 'C5', 'C7', 'C9', 'C11', 'C12', 'C21', 'C22', 'C26', 'C29', 'C35'], name: 'CAP_0402', value: '100nF 6.3V 0402', quantity: 12),
                new BOMItem(refdesList:['C3', 'C8', 'C23'], name: 'CAP_0603', value: '4.7uF 6.3V 0603 10%', quantity: 3)
            ]

        and:
            CSVInput csvInput = new DipTraceBOMCSVInput(inputFileName, reader)
            ArrayList<BOMItem> bomItems = []

        when:
            csvInput.parseHeader()

            csvInput.parseLines { CSVInputContext context, BOMItem bomItem, String[] line ->
                bomItems << bomItem
            }

        then:
            bomItems == expectedBOMItems

    }
}
