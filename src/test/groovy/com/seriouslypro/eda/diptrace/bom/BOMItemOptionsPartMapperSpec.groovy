package com.seriouslypro.eda.diptrace.bom

import com.seriouslypro.eda.BOMItem
import com.seriouslypro.eda.part.PartMapping
import spock.lang.Specification
import spock.lang.Unroll

class BOMItemOptionsPartMapperSpec extends Specification {

    def 'buildOption for empty item'() {
        when:
            List<PartMapping> options = new BOMItemOptionsPartMapper().buildOptions([], new BOMItem())

        then:
            options == []
    }

    static PartMapping nameAndValueMatch = new PartMapping(namePattern: 'nameMatch1', valuePattern: 'valueMatch1', partCode: 'partCode1', manufacturer: 'manufacturer1')

    @Unroll
    def 'buildOption for using exact match - #scenario'() {
        given:
            BOMItem bomItem = new BOMItem(name: name, value: value)

        and:
            List<PartMapping> partMappings = [
                nameAndValueMatch
            ]

        when:
            List<PartMapping> options = new BOMItemOptionsPartMapper().buildOptions(partMappings, bomItem)

        then:
            options == expectedOptions

        where:
            scenario                     | name         | value         | expectedOptions
            'no matches'                 | 'name1'      | 'value1'      | []
            'name exact match only'      | 'nameMatch1' | 'value1'      | []
            'value exact match only'     | 'name1'      | 'valueMatch1' | []
            'name and value exact match' | 'nameMatch1' | 'valueMatch1' | [nameAndValueMatch]
    }

    static PartMapping nameAndValuePatternMatch = new PartMapping(namePattern: '/name(.+)1/', valuePattern: '/value(.+)1/', partCode: 'partCode2', manufacturer: 'manufacturer2')
    static PartMapping namePatternMatch = new PartMapping(namePattern: '/name(.+)1/', valuePattern: 'value1', partCode: 'partCode2', manufacturer: 'manufacturer2')
    static PartMapping valuePatternMatch = new PartMapping(namePattern: 'name1', valuePattern: '/value(.+)1/', partCode: 'partCode2', manufacturer: 'manufacturer2')

    @Unroll
    def 'buildOption for using regular expressions - #scenario'() {
        given:
            BOMItem bomItem = new BOMItem(name: name, value: value)

        and:
            List<PartMapping> partMappings = [
                nameAndValueMatch,
                namePatternMatch,
                valuePatternMatch,
                nameAndValuePatternMatch
            ]

        when:
            List<PartMapping> options = new BOMItemOptionsPartMapper().buildOptions(partMappings, bomItem)

        then:
            options == expectedOptions

        where:
            scenario                     | name         | value         | expectedOptions
            'no matches'                 | 'other1'     | 'other2'      | []
            'name exact match only'      | 'nameMatch1' | 'value1'      | [namePatternMatch]
            'value exact match only'     | 'name1'      | 'valueMatch1' | [valuePatternMatch]
            'name and value match'       | 'nameMatch1' | 'valueMatch1' | [nameAndValueMatch, nameAndValuePatternMatch]
    }
}
