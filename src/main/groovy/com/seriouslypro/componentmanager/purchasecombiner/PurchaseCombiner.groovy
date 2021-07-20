package com.seriouslypro.componentmanager.purchasecombiner

import groovy.cli.picocli.CliBuilder
import groovy.cli.picocli.OptionAccessor

class PurchaseCombiner {

    static Integer processArgs(String[] args) {
        CliBuilder builder = new CliBuilder(usage: 'purchasecombiner')
        builder.v('version')
        builder.s(args:1, argName: 'sheet', 'sheet id')
        builder.c(args:1, argName: 'credentials', 'credentials json file/url')
        builder.sd(args:1, argName: 'sourceDirectory', 'source directory')
        builder.cfg(args:1, argName: 'config', 'configuration file (in "key=value" format)')

        builder.u('update')

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

        String credentialsFileName = config.getOrDefault("credentials","credentials.json")
        String sheetId = config.getOrDefault("sheetId","")
        List<String> sourceDirectories = []

        if (options.s) {
            sheetId = options.s
        }

        if (options.c) {
            credentialsFileName = options.c
        }

        if (options.sd) {
            sourceDirectories = options.parseResult.matchedOption("sd").typedValues().flatten() as List<String>
        }

        if (options.u) {
            boolean haveRequiredOptions = !sheetId.empty && sourceDirectories

            if (haveRequiredOptions) {
                PurchaseCombinerGoogleSheetsUpdater updater = new PurchaseCombinerGoogleSheetsUpdater(
                    sheetId: sheetId,
                    credentialsFileName: credentialsFileName,
                    sourceDirectories: sourceDirectories,
                )
                updater.update()
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
        System.out.println('PurchaseCombiner (C) 2021 Dominic Clifton')
        System.out.println('Written by Dominic Clifton')
    }
}
