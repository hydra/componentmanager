package com.seriouslypro.componentmanager.bom

import au.com.bytecode.opencsv.CSVWriter
import com.seriouslypro.componentmanager.purchase.Purchase
import com.seriouslypro.eda.BOMItemOption
import com.seriouslypro.eda.part.PartMapping
import groovy.cli.picocli.CliBuilder
import groovy.cli.picocli.OptionAccessor

import java.time.format.DateTimeFormatter

class BOMCost {

    static Integer processArgs(String[] args) {
        CliBuilder builder = new CliBuilder(usage: 'bomcost')
        builder.v('version')
        builder.p(args:1, argName: 'purchases', 'purchases file/url')
        builder.b(args:1, argName: 'bom', 'BOM file/url')
        builder.pm(args:1, argName: 'partmappings', 'part mappings file/url')
        builder.ps(args:1, argName: 'partsubstitutions', 'part substitutions file/url')
        builder.cfg(args:1, argName: 'config', 'configuration file (in "key=value" format)')
        builder.o(args:1, argName: 'output', 'output file (csv)')
        builder.os(args:1, argName: 'outputsubstitutions', 'output substitutions file (csv)')

        builder.c('calculate')

        OptionAccessor options = builder.parse(args)

        if (!options || options.getParseResult().originalArgs().size() == 0) {
            about()
            builder.usage()
            return -1
        }

        if (options.v) {
            about();
            InputStream stream = this.getClass().getResourceAsStream('/version.properties')

            Properties versionProperties = new Properties()
            versionProperties.load(stream as InputStream)
            String version = 'v' + versionProperties.get('version')

            System.out.println(version)
            return 0
        }

        Properties config = new Properties()
        if (options.cfg) {
            String configFileName = options.cfg
            InputStream inputStream = new FileInputStream(configFileName)
            config.load(inputStream)
        }

        String purchases = config.getOrDefault("purchases","purchases.csv")
        if (options.p) {
            purchases = options.p
        }

        String bom = config.getOrDefault("bom","bom.csv")
        if (options.b) {
            bom = options.b
        }

        String partMappings = config.getOrDefault("partmappings","partmappings.csv")
        if (options.pm) {
            partMappings = options.pm
        }

        String partSubstitutions = config.getOrDefault("partsubstitutions","partsubstitutions.csv")
        if (options.ps) {
            partSubstitutions = options.ps
        }

        if (options.c) {
            boolean haveRequiredOptions = !purchases.empty && !bom.empty

            if (haveRequiredOptions) {

                BOMCostCalculator calculator = new BOMCostCalculator(
                    bomFileName: bom,
                    purchasesFileName: purchases,
                    edaPartMappingsFileName: partMappings,
                    edaSubstitutionsFileName: partSubstitutions
                )
                BOMCostResult result = calculator.calculate()
                dumpBOMCostResult(result)

                if (options.o) {
                    writeOutputCSV(options.o, result)
                }

                if (options.os) {
                    writeSubstitutionsSCV(options.os, result)
                }
                return 0
            }
        }

        about()

        System.out.println('invalid parameter combinations')
        builder.usage()
        return -1
    }

    static void writeOutputCSV(String outputFileName, BOMCostResult bomCostResult) {
        Writer writer = new FileWriter(new File(outputFileName))
        CSVWriter csvWriter = new CSVWriter(writer)
        String[] headers = ["REFDES", "QUANTITY", "NAME", "VALUE", "SUBSTITUTE_NAME", "SUBSTITUTE_VALUE", "MANUFACTURER", "PART_CODE", "SUPPLIER", "ORDER_REFERENCE", "ORDER_DATE", "UNIT_PRICE", "LINE_PRICE", "CURRENCY"]
        csvWriter.writeNext(headers)

        def matchedBomItemOptions = bomCostResult.purchaseMapping.findAll { k, v -> v.present }

        matchedBomItemOptions.eachWithIndex { BOMItemOption bomItemOption, Optional<Purchase> optionalPurchase, int i ->
            Purchase purchase = optionalPurchase.get()

            String formattedOrderDate = DateTimeFormatter.ISO_DATE.format(purchase.date)

            String[] values = [
                bomItemOption.item.refdesList.join(', '),
                bomItemOption.item.quantity,
                bomItemOption.originalItem.name,
                bomItemOption.originalItem.value,
                bomItemOption.item.name,
                bomItemOption.item.value,
                purchase.manufacturer,
                purchase.partCode,
                purchase.supplier,
                purchase.orderReference,
                formattedOrderDate,
                purchase.unitPrice,
                purchase.unitPrice * bomItemOption.item.quantity,
                purchase.currency
            ]
            csvWriter.writeNext(values)
        }

        def unmatchedBomItemOptions = bomCostResult.purchaseMapping.findResults { k, v -> !v.present ? k : null }

        unmatchedBomItemOptions.each { bomItemOption ->

            String[] values = [
                bomItemOption.item.refdesList.join(', '),
                bomItemOption.item.quantity,
                bomItemOption.originalItem.name,
                bomItemOption.originalItem.value,
                bomItemOption.item.name,
                bomItemOption.item.value
            ]
            csvWriter.writeNext(values)
        }

        csvWriter.close()
    }


    static void writeSubstitutionsSCV(String outputFileName, BOMCostResult bomCostResult) {
        Writer writer = new FileWriter(new File(outputFileName))
        CSVWriter csvWriter = new CSVWriter(writer)
        String[] headers = ["LINE_TYPE", "REFDES", "QUANTITY", "ORIGINAL_NAME", "ORIGINAL_VALUE", "SUBSTITUTE_NAME", "SUBSTITUTE_VALUE", "MANUFACTURER", "PART_CODE"]
        csvWriter.writeNext(headers)

        bomCostResult.purchaseMapping.each {bomItemOption, optionalPurchase ->

            String[] values = [
                "ORIGINAL",
                bomItemOption.item.refdesList.join(', '),
                bomItemOption.item.quantity,
                bomItemOption.originalItem.name,
                bomItemOption.originalItem.value
            ]

            csvWriter.writeNext(values)

            bomItemOption.options.eachWithIndex { PartMapping partMapping, int index ->

                ArrayList<String> substitutionValues = [
                    "SUBSTITUTION",
                    "",
                    "",
                    "",
                    ""
                ]

                substitutionValues.addAll([
                    bomItemOption.item.name,
                    bomItemOption.item.value,
                    partMapping.manufacturer,
                    partMapping.partCode
                ])
                csvWriter.writeNext(substitutionValues as String[])
            }
        }
        csvWriter.close()
    }

    static void dumpBOMCostResult(BOMCostResult bomCostResult) {

        def matchedBomItemOptions = bomCostResult.purchaseMapping.findAll { k, v -> v.present }

        matchedBomItemOptions.eachWithIndex { BOMItemOption bomItemOption, Optional<Purchase> optionalPurchase, int i ->
            Purchase purchase = optionalPurchase.get()

            String formattedOrderDate = DateTimeFormatter.ISO_DATE.format(purchase.date)
            String message = "${bomItemOption.originalItem.name}, ${bomItemOption.originalItem.value}"
            if (bomItemOption.originalItem != bomItemOption.item) {
                message += " -> ${bomItemOption.item.name}, ${bomItemOption.item.value}"
            }
            message += " -> Manufacturer: ${purchase.manufacturer}, Part Code: ${purchase.partCode}, Supplier: ${purchase.supplier}, Order reference: ${purchase.orderReference}, Order date: ${formattedOrderDate}, Unit price: ${purchase.unitPrice} ${purchase.currency}, RefDes: ${bomItemOption.item.refdesList}"

            System.out.println(message)
        }

        def unmatchedBomItemOptions = bomCostResult.purchaseMapping.findResults { k, v -> !v.present ? k : null }

        if (unmatchedBomItemOptions) {
            System.out.println("Unmatched BOM items")
            unmatchedBomItemOptions.each { bomItemOption ->
                System.out.println("${bomItemOption.item.name}, ${bomItemOption.item.value}, ${bomItemOption.item.refdesList}")
                String indentation = "\t"
                if (bomItemOption.originalItem != bomItemOption.item) {
                    System.out.println("${indentation}Substituted from ${bomItemOption.originalItem.name}, ${bomItemOption.originalItem.value}")
                }
                bomItemOption.options.eachWithIndex { PartMapping partMapping, int index ->
                    System.out.println("${indentation}${index} -> Manufacturer: ${partMapping.manufacturer}, Part Code: ${partMapping.partCode}")
                }
            }
        }

        System.out.println("Cost: ${bomCostResult.cost}")
    }

    public static void main(String [] args) {
        System.exit(processArgs(args))
    }

    private static void about() {
        System.out.println('BOMCost (C) 2021 Dominic Clifton')
        System.out.println('Written by Dominic Clifton')
    }
}
