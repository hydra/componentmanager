package com.seriouslypro.eda.part

import com.seriouslypro.csv.*
import com.seriouslypro.eda.BOMItem
import com.seriouslypro.pnpconvert.FileTools

import java.util.regex.Matcher
import java.util.regex.Pattern

class PartSubstitutor {

    List<PartSubstitution> partSubstitutions = []

    List<PartSubstitution> findSubstitutions(BOMItem bomItem) {
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

    BOMItem buildSubstitute(BOMItem bomItem, PartSubstitution substitution) {
        return new BOMItem(
            name: substitution.name,
            value: substitution.value,
            refdesList: bomItem.refdesList,
            quantity: bomItem.quantity
        )
    }

    static enum EDAPartSubstitutionCSVColumn implements CSVColumn<EDAPartSubstitutionCSVColumn> {
        NAME_PATTERN,
        VALUE_PATTERN,
        NAME,
        VALUE

        EDAPartSubstitutionCSVColumn(List<String> aliases = []) {
            this.aliases = aliases
        }
    }

    void loadFromCSV(String fileName) {
        Reader reader = FileTools.openFileOrUrl(fileName)
        loadFromCSV(fileName, reader)
    }

    void loadFromCSV(String reference, Reader reader) {

        CSVLineParser<PartSubstitution, EDAPartSubstitutionCSVColumn> edaPartSubstitutionParser = new CSVLineParserBase<PartSubstitution, EDAPartSubstitutionCSVColumn>() {

            @Override
            PartSubstitution parse(CSVInputContext context, String[] rowValues) {

                def namePattern = rowValues[columnIndex(context, EDAPartSubstitutionCSVColumn.NAME_PATTERN)].trim()
                def valuePattern = rowValues[columnIndex(context, EDAPartSubstitutionCSVColumn.VALUE_PATTERN)].trim()
                def name = rowValues[columnIndex(context, EDAPartSubstitutionCSVColumn.NAME)].trim()
                def value = rowValues[columnIndex(context, EDAPartSubstitutionCSVColumn.VALUE)].trim()

                if (!(namePattern && name)) {
                    throw new CSVInput.CSVParseException("one or more missing values")
                }

                return new PartSubstitution(
                    namePattern: namePattern,
                    valuePattern: valuePattern,
                    name: name,
                    value: value,
                )
            }
        }

        CSVHeaderParser<EDAPartSubstitutionCSVColumn> edaPartSubstitutionHeaderParser = new CSVHeaderParserBase<EDAPartSubstitutionCSVColumn>() {
            @Override
            EDAPartSubstitutionCSVColumn parseHeader(CSVInputContext context, String headerValue) {
                EDAPartSubstitutionCSVColumn.fromString(EDAPartSubstitutionCSVColumn, headerValue) as EDAPartSubstitutionCSVColumn
            }
        }

        CSVInput<PartSubstitution, EDAPartSubstitutionCSVColumn> csvInput = new CSVInput<PartSubstitution, EDAPartSubstitutionCSVColumn>(reference, reader, edaPartSubstitutionHeaderParser, edaPartSubstitutionParser)
        csvInput.parseHeader()

        csvInput.parseLines { CSVInputContext context, PartSubstitution partSubstitution, String[] line ->
            partSubstitutions.add(partSubstitution)
        }

        csvInput.close()
    }
}
