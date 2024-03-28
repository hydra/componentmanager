package com.seriouslypro.eda.part

import com.seriouslypro.eda.BOMItem

import java.util.regex.Pattern

class BOMItemPartSubstitutor {

    BOMItem buildSubstitute(BOMItem bomItem, PartSubstitution substitution) {
        return new BOMItem(
            name: substitution.name,
            value: substitution.value,
            refdesList: bomItem.refdesList,
            quantity: bomItem.quantity
        )
    }

    List<PartSubstitution> findSubstitutions(List<PartSubstitution> partSubstitutions, BOMItem bomItem) {
        List<PartSubstitution> options = partSubstitutions.findAll { partSubstitution ->

            boolean nameMatched =  partSubstitution.namePattern == bomItem.name
            boolean valueMatched = partSubstitution.valuePattern == bomItem.value

            if (nameMatched && valueMatched) {
                return true
            }

            Optional<Pattern> namePattern = PatternParser.parsePattern(partSubstitution.namePattern)
            Optional<Pattern> valuePattern = PatternParser.parsePattern(partSubstitution.valuePattern)

            nameMatched |= namePattern.present && bomItem.name ==~ namePattern.get()
            valueMatched |= valuePattern.present && bomItem.value ==~ valuePattern.get()

            return (nameMatched && valueMatched)
        }

        options
    }

}
