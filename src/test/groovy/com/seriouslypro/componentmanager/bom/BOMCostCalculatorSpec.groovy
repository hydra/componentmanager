package com.seriouslypro.componentmanager.bom

import com.seriouslypro.componentmanager.purchase.Purchase
import com.seriouslypro.eda.BOMItem
import com.seriouslypro.eda.BOMItemOption
import com.seriouslypro.eda.part.PartMapping
import com.seriouslypro.test.TestResources
import spock.lang.Specification

import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
                '"J6";"IO_1";"JST-SH 10WAY";"1";"JST";"http://www.jst-mfg.com/product/pdf/eng/eSH.pdf";"12";"JST-SH-10P (SideEntry)"\n' +
                '"SP1";"";"Solder Pad";"1";"";"";"1";"SP_SQUARE_2x2"'
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

        and:
            BOMItem bomItem1 = new BOMItem(refdesList: ["C1", "C2", "C5", "C7", "C9", "C11", "C12", "C21", "C22", "C26", "C29", "C35"], name:"CAP_0402", value:"100nF 6.3V 0402", quantity: 12)
            BOMItem bomItem1Substitute = new BOMItem(refdesList: ["C1", "C2", "C5", "C7", "C9", "C11", "C12", "C21", "C22", "C26", "C29", "C35"], name:"CAP_0402", value:"100nF 50V 0402", quantity: 12)
            PartMapping partMapping1 = new PartMapping(namePattern: "CAP_0402", valuePattern: "100nF 50V 0402", partCode: "0402B104K500CT", manufacturer: "Walsin Tech Corp")

            BOMItem bomItem2 = new BOMItem(refdesList: ["C3", "C8", "C23"], name: "CAP_0603", value: "4.7uF 6.3V 0603 10%", quantity: 3)
            PartMapping partMapping2 = new PartMapping(namePattern: "CAP_0603", valuePattern: "4.7uF 6.3V 0603 10%", partCode: "CL10A475KQ8NNNC", manufacturer: "Samsung Electro-Mechanics")

            BOMItem bomItem3 = new BOMItem(refdesList: ["D1", "D2"], name: "LED_0603", value: "GREEN", quantity: 2)
            PartMapping partMapping3 = new PartMapping(namePattern: "LED_0603", valuePattern: "GREEN", partCode: "BL-0603-GREEN-A34", manufacturer: "BrightLed")

            BOMItem bomItem4 = new BOMItem(refdesList: ["J6"], name: "JST-SH 10WAY", value: "IO_1", quantity: 1)
            PartMapping partMapping4 = new PartMapping(namePattern: "JST-SH 10WAY", valuePattern: "/.*/", partCode: "AFC10-S10QCC-00", manufacturer: "JUSHUO")

            BOMItem bomItem5 = new BOMItem(refdesList: ["SP1"], name: "Solder Pad", value: "", quantity: 1)

            BOMItemOption bomItemOption1 = new BOMItemOption(originalItem: bomItem1, item: bomItem1Substitute, options: [partMapping1])
            BOMItemOption bomItemOption2 = new BOMItemOption(originalItem: bomItem2, item: bomItem2, options: [partMapping2])
            BOMItemOption bomItemOption3 = new BOMItemOption(originalItem: bomItem3, item: bomItem3, options: [partMapping3])
            BOMItemOption bomItemOption4 = new BOMItemOption(originalItem: bomItem4, item: bomItem4, options: [partMapping4])
            BOMItemOption bomItemOption5 = new BOMItemOption(originalItem: bomItem5, item: bomItem5, options: [])

        and:
            Purchase purchase1 = new Purchase(date: LocalDate.from(DateTimeFormatter.ISO_DATE.parse("2021-07-06")), orderReference: "WM210706790W", supplier: "LCSC", lineItem: 2, supplierReference: "C152814", partCode: "0402B104K500CT", manufacturer: "Walsin Tech Corp", description: "100nF ±10% 50V X7R 0402 Multilayer Ceramic Capacitors MLCC,SMD/SMT RoHS", Quantity: 10000, unitPrice: 0.0024, linePrice: 24.0000, currency: USD)
            Purchase purchase2 = new Purchase(date: LocalDate.from(DateTimeFormatter.ISO_DATE.parse("2019-12-14")), orderReference: "20190604YOTN", supplier: "LCSC", lineItem: 1, supplierReference: "C8032", partCode: "CL10A475KQ8NNNC", manufacturer: "Samsung Electro-Mechanics", description: "4.7uF ±10% 6.3V X5R 0603 Multilayer Ceramic Capacitors MLCC,SMD/SMT RoHS", Quantity: 4000, unitPrice: 0.0033, linePrice: 13.2000, currency: USD)
            Purchase purchase3 = new Purchase(date: LocalDate.from(DateTimeFormatter.ISO_DATE.parse("2010-10-18")), orderReference: "506908342095201", supplier: "AliExpress", lineItem: 1, supplierReference: "32695104679", partCode: "BL-0603-GREEN-A34", manufacturer: "BrightLed", description: "Led Lamp SMD Led Diode SMD 0603 Green 515-525nm 20mA 3V 4000pcs super-bright-leds", Quantity: 4000, unitPrice: 0.0043, linePrice: 17.2200, currency: GBP)
            Purchase purchase4 = new Purchase(date: LocalDate.from(DateTimeFormatter.ISO_DATE.parse("2019-12-11")), orderReference: "201912113RST", supplier: "LCSC", lineItem: 6, supplierReference: "C395307", partCode: "AFC10-S10QCC-00", manufacturer: "JUSHUO", description: "Header Male Blade 0.039”(1.00mm) 10 SMD Wire To Board / Wire To Wire Connector RoHS", Quantity: 180, unitPrice: 0.0617, linePrice: 11.1031, currency: USD)

        when:
            BOMCostResult result = calculator.calculate()

        then:
            noExceptionThrown()

        and:
            result.cost == [
                (USD): (0.0024 * 12) + (0.0033 * 3) + (0.0617 * 1),
                (GBP): (0.0043 * 2)
            ]

        and:
            result.purchaseMapping == [
                (bomItemOption1): Optional.of(purchase1),
                (bomItemOption2): Optional.of(purchase2),
                (bomItemOption3): Optional.of(purchase3),
                (bomItemOption4): Optional.of(purchase4),
                (bomItemOption5): Optional.empty()
            ]
    }
}
