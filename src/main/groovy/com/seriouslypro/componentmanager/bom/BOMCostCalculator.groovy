package com.seriouslypro.componentmanager.bom


import com.seriouslypro.componentmanager.purchase.Purchase
import com.seriouslypro.componentmanager.purchase.PurchaseCSVInput
import com.seriouslypro.csv.CSVInput
import com.seriouslypro.csv.CSVInputContext
import com.seriouslypro.eda.BOMItem
import com.seriouslypro.eda.BOMItemOption
import com.seriouslypro.eda.diptrace.bom.BOMItemOptionsPartMapper
import com.seriouslypro.eda.diptrace.bom.DipTraceBOMCSVInput
import com.seriouslypro.eda.part.BOMItemPartSubstitutor
import com.seriouslypro.eda.part.PartMappings
import com.seriouslypro.eda.part.PartMapping
import com.seriouslypro.eda.part.PartSubstitution
import com.seriouslypro.eda.part.PartSubstitutor
import com.seriouslypro.pnpconvert.FileTools
import groovy.transform.ToString

interface BOMItemMatchingStrategy {
    boolean matches(Purchase candidate, BOMItem bomItem)
}

@ToString(includeNames = true, includePackage = false)
class NameOnlyItemMatchingStrategy implements BOMItemMatchingStrategy {

    @Override
    boolean matches(Purchase candidate, BOMItem bomItem) {
        bomItem.name && bomItem.name == candidate.partCode
    }
}

interface BOMOptionMatchingStrategy {
    boolean matches(Purchase candidate, PartMapping partMapping)
}

@ToString(includeNames = true, includePackage = false)
class ExactOptionMatchingStrategy implements BOMOptionMatchingStrategy {

    boolean matches(Purchase candidate, PartMapping partMapping) {
        candidate.partCode == partMapping.partCode && candidate.manufacturer.toLowerCase() == partMapping.manufacturer.toLowerCase()
    }
}

class BOMCostCalculator {

    String bomFileName
    String purchasesFileName
    String edaPartMappingsFileName
    String edaSubstitutionsFileName

    PartMappings partMapper = new PartMappings()
    PartSubstitutor partSubstitutor = new PartSubstitutor()
    BOMItemPartSubstitutor bomPartSubstitutor = new BOMItemPartSubstitutor()

    List<BOMItemMatchingStrategy> itemMatchingStrategies = [
        new NameOnlyItemMatchingStrategy()
    ]

    List<BOMOptionMatchingStrategy> optionMatchingStrategies = [
        new ExactOptionMatchingStrategy()
    ]

    BOMCostResult calculate() {

        if (edaPartMappingsFileName) {
            partMapper.loadFromCSV(edaPartMappingsFileName)
        }

        if (edaSubstitutionsFileName) {
            partSubstitutor.loadFromCSV(edaSubstitutionsFileName)
        }

        Reader bomReader = FileTools.openFileOrUrl(bomFileName)

        CSVInput bomCSVInput = new DipTraceBOMCSVInput(bomFileName, bomReader)
        List<BOMItemOption> bomItemOptions = []

        bomCSVInput.parseHeader()
        bomCSVInput.parseLines { CSVInputContext context, BOMItem bomItem, String[] line ->

            List<PartSubstitution> partSubstitutions = bomPartSubstitutor.findSubstitutions(partSubstitutor.partSubstitutions, bomItem)
            BOMItem substitute = chooseAndBuildSubstitute(bomItem, partSubstitutions)

            List<PartMapping> options = new BOMItemOptionsPartMapper().buildOptions(partMapper.partMappings, substitute)
            bomItemOptions << new BOMItemOption(originalItem: bomItem, item: substitute, options: options)
        }

        Reader purchasesReader = FileTools.openFileOrUrl(purchasesFileName)
        CSVInput purchasesCSVInput = new PurchaseCSVInput(purchasesFileName, purchasesReader)
        List<Purchase> purchases = []
        purchasesCSVInput.parseHeader()
        purchasesCSVInput.parseLines { CSVInputContext context, Purchase purchase, String[] line ->
            purchases << purchase
        }


        BOMCostResult result = new BOMCostResult()

        bomItemOptions.eachWithIndex { BOMItemOption bomItemOption, int i ->
            Optional<Purchase> optionalPurchase = findPurchase(purchases, bomItemOption)

            result.purchaseMapping[bomItemOption] = optionalPurchase

            if (optionalPurchase.present) {
                Purchase purchase = optionalPurchase.get()

                BigDecimal bomItemCost = purchase.unitPrice * bomItemOption.item.quantity
                if (!result.cost.containsKey(purchase.currency)) {
                    result.cost[purchase.currency] = 0.0
                }
                result.cost[purchase.currency] += bomItemCost
            }
        }

        return result
    }

    BOMItem chooseAndBuildSubstitute(BOMItem bomItem, List<PartSubstitution> partSubstitutions) {
        if (partSubstitutions.empty) {
            return bomItem
        }

        PartSubstitution selectedSubstitution = partSubstitutions.first()

        BOMItem substitute = bomPartSubstitutor.buildSubstitute(bomItem, selectedSubstitution)

        return substitute
    }

    Optional<Purchase> findPurchase(List<Purchase> purchases, BOMItemOption bomItemOption) {
        Debug.trace("Finding purchase for bomItemOption: $bomItemOption")
        Purchase matched = purchases.findResult { purchase ->
            Debug.trace("Order Reference: ${purchase.orderReference}, Part: ${purchase.partCode}, Manufacturer: ${purchase.manufacturer}")
            itemMatches(purchase, bomItemOption)
        }
        Optional.ofNullable(matched)
    }

    Purchase itemMatches(Purchase purchase, BOMItemOption bomItemOption) {

        List<Tuple2> results = []

        itemMatchingStrategies.each { BOMItemMatchingStrategy strategy ->
            if (strategy.matches(purchase, bomItemOption.item)) {
                results << new Tuple2(strategy, bomItemOption.item)
            }
        }

        optionMatchingStrategies.each { BOMOptionMatchingStrategy strategy ->
            bomItemOption.options.each { partMapping ->
                if (strategy.matches(purchase, partMapping)) {
                    results << new Tuple2(strategy, partMapping)
                }
            }
        }

        Debug.trace(results.toString())
        if (results.empty) {
            return null
        }

        return purchase
    }
}
