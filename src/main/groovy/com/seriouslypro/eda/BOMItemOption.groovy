package com.seriouslypro.eda

import com.seriouslypro.eda.part.PartMapping
import com.seriouslypro.eda.part.PartSubstitution
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
class BOMItemOption {
    BOMItem originalItem // before substitution
    BOMItem item
    List<PartMapping> options = []
}
