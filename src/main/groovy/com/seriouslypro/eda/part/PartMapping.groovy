package com.seriouslypro.eda.part

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class PartMapping {
    // EDA
    String namePattern
    String valuePattern

    // Ordering information
    String code
    String manufacturer
}
