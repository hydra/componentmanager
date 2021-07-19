package com.seriouslypro.componentmanager.bom

import com.seriouslypro.componentmanager.currency.Currency
import groovy.cli.picocli.CliBuilder
import groovy.cli.picocli.OptionAccessor

class BOMCost {

    static Integer processArgs(String[] args) {
        CliBuilder builder = new CliBuilder(usage: 'bomcost')
        builder.v('version')
        builder.p(args:1, argName: 'purchases', 'purhases file/url')
        builder.b(args:1, argName: 'bom', 'BOM file/url')
        builder.cu(args:1, argName: 'currency', 'currency')
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

        String currencyCode = config.getOrDefault("currency","USD")
        if (options.cu) {
            currencyCode = options.cu
        }

        if (options.c) {
            boolean haveRequiredOptions = !purchases.empty && !bom.empty && !currencyCode.empty

            if (haveRequiredOptions) {

                Currency currency = currencyCode as Currency

                BOMCostCalculator calculator = new BOMCostCalculator(
                    bomFileName: bom,
                    purchasesFileName: purchases,
                    currency: currency,
                )
                calculator.calculate()
                return 0
            }
        }

        about()

        System.out.println('invalid parameter combinations')
        builder.usage()
        return -1
    }

    public static void main(String [] args) {
        System.exit(processArgs(args))
    }

    private static void about() {
        System.out.println('BOMCost (C) 2021 Dominic Clifton')
        System.out.println('Written by Dominic Clifton')
    }
}
