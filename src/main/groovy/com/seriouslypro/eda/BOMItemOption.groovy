package com.seriouslypro.eda

import com.seriouslypro.eda.part.PartMapping
import com.seriouslypro.eda.part.PartSubstitution
import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class BOMItemOption {
    BOMItem originalItem // before substitution
    BOMItem item
    List<PartMapping> options = []
}
