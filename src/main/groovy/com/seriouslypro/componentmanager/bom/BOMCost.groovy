package com.seriouslypro.componentmanager.bom

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
                return 0
            }
        }

        about()

        System.out.println('invalid parameter combinations')
        builder.usage()
        return -1
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
            message += " -> Manufacturer: ${purchase.manufacturer}, Part Code: ${purchase.partCode}, Supplier: ${purchase.supplier}, Order reference: ${purchase.orderReference}, Order date: ${formattedOrderDate}, Unit price: ${purchase.unitPrice} ${purchase.currency}"

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
