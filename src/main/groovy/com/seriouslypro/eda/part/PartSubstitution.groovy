package com.seriouslypro.eda.part

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class PartSubstitution {
    // EDA
    String namePattern
    String valuePattern

    // Substitution information
    String name
    String value
}
