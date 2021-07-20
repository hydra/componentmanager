package com.seriouslypro.componentmanager.bom

import com.seriouslypro.test.TestResources
import spock.lang.Specification

import static com.seriouslypro.componentmanager.currency.Currency.GBP
import static com.seriouslypro.componentmanager.currency.Currency.USD

class BOMCostCalculatorSpec extends Specification implements TestResources {

    def 'calculate'() {
        given:
            String purchases = 'Date,Order Reference,Supplier,Line Item,Supplier Reference,Part,Manufacturer,Description,Quantity,Unit Price,Line Price,Currency\n' +
                '2021/7/6,WM210706790W,LCSC,2,C152814,0402B104K500CT,Walsin Tech Corp,"100nF ±10% 50V X7R 0402 Multilayer Ceramic Capacitors MLCC,SMD/SMT RoHS",10000,0.0024,24.0000,USD\n' +
                '2020/5/6,20200506CEUC,LCSC,5,C301927,0402B103K500CT,Walsin Tech Corp,"10nF ±10% 50V X7R 0402 Multilayer Ceramic Capacitors MLCC,SMD/SMT RoHS",10000,0.0014,14.2800,USD\n' +
                '2019/12/14,20190604YOTN,LCSC,1,C8032,CL10A475KQ8NNNC,Samsung Electro-Mechanics,"4.7uF ±10% 6.3V X5R 0603 Multilayer Ceramic Capacitors MLCC,SMD/SMT RoHS",4000,0.0033,13.2000,USD\n' +
                '2010/10/18,506908342095201,AliExpress,1,32695104679,BL-0603-GREEN-A34,BrightLed,Led Lamp SMD Led Diode SMD 0603 Green 515-525nm 20mA 3V 4000pcs super-bright-leds,4000,0.0043,17.2200,GBP\n' +
                '2019/12/11,201912113RST,LCSC,6,C395307,AFC10-S10QCC-00,JUSHUO,Header Male Blade 0.039”(1.00mm) 10 SMD Wire To Board / Wire To Wire Connector RoHS,180,0.0617,11.1031,USD'
            File purchasesFile = createTemporaryFile(temporaryFolder, "purchases.csv", purchases.getBytes("UTF-8"))

            String bom = '"RefDes";"Value";"Name";"Quantity";"Manufacturer";"Datasheet";"Number of Pins";"Pattern"\n' +
                '"C1, C2, C5, C7, C9, C11, C12, C21, C22, C26, C29, C35";"100nF 6.3V 0402";"CAP_0402";"12";"";"";"2";"CAP_0402_201906"\n' +
                '"C3, C8, C23";"4.7uF 6.3V 0603 10%";"CAP_0603";"3";"TDK";"http://www.farnell.com/datasheets/2291921.pdf";"2";"CAP_0603"\n' +
                '"D1, D2";"GREEN";"LED_0603";"2";"";"";"2";"LED_0603_WURTH"\n' +
                '"J6";"IO_1";"JST-SH 10WAY";"1";"JST";"http://www.jst-mfg.com/product/pdf/eng/eSH.pdf";"12";"JST-SH-10P (SideEntry)"'
            File bomFile = createTemporaryFile(temporaryFolder, "bom.csv", bom.getBytes("UTF-8"))

            String edaPartMappings = 'Name Pattern,Value Pattern,Part Code,Manufacturer\n' +
                'CAP_0402,10nF 6.3V 0402,MC0402X103K6R3CT,Multicomp\n' +
                'CAP_0402,10nF 50V 0402,0402B103K500CT,Walsin Tech Corp\n' +
                'CAP_0402,100nF 6.3V 0402,MC0402X104K6R3CT,Multicomp\n' +
                'CAP_0402,100nF 50V 0402,0402B104K500CT,Walsin Tech Corp\n' +
                'CAP_0603,4.7uF 6.3V 0603 10%,CL10A475KQ8NNNC,Samsung Electro-Mechanics\n' +
                'LED_0603,GREEN,BL-0603-GREEN-A34,BrightLed\n' +
                'JST-SH 10WAY,/.*/,AFC10-S10QCC-00,JUSHUO'
            File edaPartMappingsFile = createTemporaryFile(temporaryFolder, "edapartmappings.csv", edaPartMappings.getBytes("UTF-8"))

            String edaSubstitutions = 'Name Pattern,Value Pattern,Name,Value\n' +
                'CAP_0402,10nF 6.3V 0402,CAP_0402,10nF 50V 0402\n' +
                'CAP_0402,100nF 6.3V 0402,CAP_0402,100nF 50V 0402'
            File edaSubstitutionsFile = createTemporaryFile(temporaryFolder, "edasubstitutions.csv", edaSubstitutions.getBytes("UTF-8"))

        and:
            BOMCostCalculator calculator = new BOMCostCalculator(
                purchasesFileName: purchasesFile.absolutePath,
                bomFileName: bomFile.absolutePath,
                edaPartMappingsFileName: edaPartMappingsFile.absolutePath,
                edaSubstitutionsFileName: edaSubstitutionsFile.absolutePath
            )

        when:
            BOMCostResult result = calculator.calculate()

        then:
            noExceptionThrown()

        and:
            result.cost == [
                (USD): (0.0024 * 12) + (0.0033 * 3) + (0.0617 * 1),
                (GBP): (0.0043 * 2)
            ]
    }
}
