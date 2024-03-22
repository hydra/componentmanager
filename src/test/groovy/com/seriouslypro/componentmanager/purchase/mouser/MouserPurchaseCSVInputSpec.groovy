package com.seriouslypro.componentmanager.purchase.mouser

import com.seriouslypro.csv.CSVInput
import com.seriouslypro.csv.CSVInputContext
import spock.lang.Specification

import java.time.LocalDate
import java.time.format.DateTimeFormatter;

class MouserPurchaseCSVInputSpec extends Specification {

    def 'extract purchases (file format circa 2023)'() {
        given:
            String inputFileName = "test"

            String content = 'Mouser Part No,Customer Part No.,Mfr. Part No.,Mfr.,Description,Qty.,Price,Your PO No.,Buyer,Sales Order No.,Order Date,Invoice No.,Invoice Date (mm/dd/yyyy)\n' +
                '910-SMD2SW015100G,,SMD2SW.015 100g,Chip Quik,,1,"13,94 €",18823669,MR CUSTOMER,250194294,21-Apr-20,-,-'
            Reader reader = new StringReader(content)

        and:
            ArrayList<MouserPurchase> expectedPurchases = [
                new MouserPurchase(manufacturer:'Chip Quik', orderDate: LocalDate.from(DateTimeFormatter.ISO_DATE.parse('2020-04-21')), currency: 'EUR', manufacturerPart:'SMD2SW.015 100g', description: '', quantity:1, orderNumber: 250194294, price:13.94, supplierPart: '910-SMD2SW015100G', supplier: 'Mouser')
            ]

        and:
            CSVInput csvInput = new MouserPurchaseCSVInput(inputFileName, reader)
            ArrayList<MouserPurchase> purchases = []

        when:
            csvInput.parseHeader()

            csvInput.parseLines { CSVInputContext context, MouserPurchase purchase, String[] line ->
                purchases << purchase
            }

        then:
            purchases == expectedPurchases
    }

    def 'extract purchases (format circa 2024/Q1)'() {
        given:
            String inputFileName = "test"

            String content = 'Mouser Part No,Customer Part No.,Mfr. Part No.,Mfr.,Description,Qty Shipped,Price,Your PO No.,Buyer,Sales Order No.,Order Date,Invoice No.,Dispatch Date\n' +
                '356-E32-S3DVKTC1N8R2,,ESP32-S3-DevKitC-1-N8R2,Espressif,ESP32-S3 General-Pur pose Development Boa,2,"€ 13,50",29534640,MR CUSTOMER,266224434,17-Jul-23,74736876,17-Jul-23'
            Reader reader = new StringReader(content)

        and:
            ArrayList<MouserPurchase> expectedPurchases = [
                new MouserPurchase(supplierPart:'356-E32-S3DVKTC1N8R2', manufacturerPart:'ESP32-S3-DevKitC-1-N8R2', manufacturer:'Espressif', description:'ESP32-S3 General-Pur pose Development Boa', quantity:2, price:13.50, currency:'EUR', orderDate:LocalDate.from(DateTimeFormatter.ISO_DATE.parse('2023-07-17')), orderNumber:266224434, supplier:'Mouser')
            ]

        and:
            CSVInput csvInput = new MouserPurchaseCSVInput(inputFileName, reader)
            ArrayList<MouserPurchase> purchases = []

        when:
            csvInput.parseHeader()

            csvInput.parseLines { CSVInputContext context, MouserPurchase purchase, String[] line ->
                purchases << purchase
            }

        then:
            purchases == expectedPurchases
    }
}