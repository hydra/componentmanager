package com.seriouslypro.eda

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString


@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
class BOMItem {
    List<String> refdesList = []

    String name
    String value

    int quantity
}
