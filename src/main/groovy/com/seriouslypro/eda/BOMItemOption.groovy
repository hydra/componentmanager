package com.seriouslypro.eda

import com.seriouslypro.eda.part.PartMapping
import groovy.transform.ToString

@ToString
class BOMItemOption {
    BOMItem item
    List<PartMapping> options = []
}
