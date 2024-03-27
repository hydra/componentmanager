package com.seriouslypro.eda.diptrace.bom

import com.seriouslypro.eda.BOMItem
import com.seriouslypro.eda.part.PartMapping
import com.seriouslypro.eda.part.PatternParser

import java.util.regex.Pattern

class BOMItemOptionsPartMapper {
    List<PartMapping> buildOptions(List<PartMapping> partMappings, BOMItem bomItem) {
        List<PartMapping> options = partMappings.findAll { partMapping ->

            boolean nameMatched = partMapping.namePattern == bomItem.name
            boolean valueMatched = partMapping.valuePattern == bomItem.value

            if (nameMatched && valueMatched) {
                return true
            }

            Optional<Pattern> namePattern = PatternParser.parsePattern(partMapping.namePattern)
            Optional<Pattern> valuePattern = PatternParser.parsePattern(partMapping.valuePattern)

            nameMatched |= namePattern.present && bomItem.name ==~ namePattern.get()
            valueMatched |= valuePattern.present && bomItem.value ==~ valuePattern.get()

            return (nameMatched && valueMatched)
        }

        options
    }

}
