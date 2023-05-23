package com.seriouslypro.eda.part

import com.seriouslypro.eda.BOMItem
import spock.lang.Specification

class PartSubstitutorSpec extends Specification {

    def 'exact match'() {
        given:
            def partSubstitutions = [
                    new PartSubstitution(
                            namePattern: "NAME_NOT_PATTERN",
                            valuePattern: "VALUE_NOT_PATTERN",
                            name: "NAME",
                            value: "PATTERN",
                    )
            ]
            def partSubstitutor = new PartSubstitutor(partSubstitutions: partSubstitutions)
            def bomItem = new BOMItem(name: "NAME_NOT_PATTERN", value: "VALUE_NOT_PATTERN", quantity: 1, refdesList: ["J1", "J2"])
        when:
            def result = partSubstitutor.findSubstitutions(bomItem)

        then:
            result != []
    }

    def 'match name pattern'() {
        given:
            def partSubstitutions = [
                new PartSubstitution(
                    namePattern: "/.*/",
                    valuePattern: "",
                    name: "U.FL-R-SMT-1(10)",
                    value: "",
                )
            ]
            def partSubstitutor = new PartSubstitutor(partSubstitutions: partSubstitutions)
            def bomItem = new BOMItem(name: "U.FL-R-SMT-1(10) / 20441-001E-01", value: "", quantity: 1, refdesList: ["J1", "J2"])
        when:
            def result = partSubstitutor.findSubstitutions(bomItem)

        then:
            result != []
    }

    def 'match value pattern'() {
        given:
            def partSubstitutions = [
                    new PartSubstitution(
                            namePattern: "U.FL-R-SMT-1(10) / 20441-001E-01",
                            valuePattern: "/.*/",
                            name: "U.FL-R-SMT-1(10)",
                            value: "",
                    )
            ]
            def partSubstitutor = new PartSubstitutor(partSubstitutions: partSubstitutions)
            def bomItem = new BOMItem(name: "U.FL-R-SMT-1(10) / 20441-001E-01", value: "", quantity: 1, refdesList: ["J1", "J2"])
        when:
            def result = partSubstitutor.findSubstitutions(bomItem)

        then:
            result != []
    }
}
