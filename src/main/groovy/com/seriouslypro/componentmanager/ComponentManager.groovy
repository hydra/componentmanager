package com.seriouslypro.componentmanager

import groovy.cli.picocli.CliBuilder
import groovy.cli.picocli.OptionAccessor

/**
 * This is just a skeleton program and has no functionality.
 *
 * @see BOMCost
 * @see PurchaseCombiner
 */
class ComponentManager {

    public static void main(String [] args) {
        processArgs(args)
    }

    static def processArgs(String[] args) {
        CliBuilder builder = new CliBuilder(usage: 'componentmanager')
        builder.v('version')

        OptionAccessor options = builder.parse(args)

        if (!options || options.getParseResult().originalArgs().size() == 0) {
            about()
            builder.usage()
            System.exit(-1)
        }
    }

    private static void about() {
        System.out.println('ComponentManager (C) 2023-2024 Dominic Clifton')
        System.out.println('Written by Dominic Clifton')
    }
}
