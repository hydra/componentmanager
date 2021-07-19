package com.seriouslypro.componentmanager.bom

import com.seriouslypro.componentmanager.currency.Currency
import com.seriouslypro.componentmanager.purchase.Purchase
import com.seriouslypro.componentmanager.purchase.PurchaseCSVInput
import com.seriouslypro.csv.CSVInput
import com.seriouslypro.csv.CSVInputContext
import com.seriouslypro.eda.BOMItem
import com.seriouslypro.eda.BOMItemOption
import com.seriouslypro.eda.diptrace.bom.DipTraceBOMCSVInput
import com.seriouslypro.eda.part.PartMapper
import com.seriouslypro.eda.part.PartMapping
import com.seriouslypro.pnpconvert.FileTools
import groovy.transform.ToString

interface BOMItemMatchingStrategy {
    boolean matches(Purchase candidate, BOMItem bomItem)
}

@ToString(includeNames = true, includePackage = false)
class NameOnlyItemMatchingStrategy implements BOMItemMatchingStrategy {

    @Override
    boolean matches(Purchase candidate, BOMItem bomItem) {
        bomItem.name && bomItem.name == candidate.part
    }
}

interface BOMOptionMatchingStrategy {
    boolean matches(Purchase candidate, PartMapping partMapping)
}

@ToString(includeNames = true, includePackage = false)
class ExactOptionMatchingStrategy implements BOMOptionMatchingStrategy {

    boolean matches(Purchase candidate, PartMapping partMapping) {
        candidate.part == partMapping.code && candidate.manufacturer == partMapping.manufacturer
    }
}

class BOMCostCalculator {
    String bomFileName
    String purchasesFileName
    Currency currency
    String edaPartMappingsFileName

    PartMapper partMapper = new PartMapper()

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

        Reader bomReader = FileTools.openFileOrUrl(bomFileName)

        CSVInput bomCSVInput = new DipTraceBOMCSVInput(bomFileName, bomReader)
        List<BOMItemOption> bomItemOptions = []

        bomCSVInput.parseHeader()
        bomCSVInput.parseLines { CSVInputContext context, BOMItem bomItem, String[] line ->
            List<PartMapping> options = partMapper.buildOptions(bomItem)
            bomItemOptions << new BOMItemOption(item: bomItem, options: options)
        }

        Reader purchasesReader = FileTools.openFileOrUrl(purchasesFileName)
        CSVInput purchasesCSVInput = new PurchaseCSVInput(purchasesFileName, purchasesReader)
        List<Purchase> purchases = []
        purchasesCSVInput.parseHeader()
        purchasesCSVInput.parseLines { CSVInputContext context, Purchase purchase, String[] line ->
            purchases << purchase
        }


        BOMCostResult result = new BOMCostResult()
        result.currency = currency

        bomItemOptions.eachWithIndex { BOMItemOption bomItemOption, int i ->
            Optional<Purchase> optionalPurchase = findPurchase(purchases, bomItemOption)
            if (optionalPurchase.present) {
                Purchase purchase = optionalPurchase.get()

                BigDecimal totalCost = purchase.unitPrice * bomItemOption.item.quantity
                result.cost += totalCost
            }
        }

        return result
    }

    Optional<Purchase> findPurchase(List<Purchase> purchases, BOMItemOption bomItemOption) {
        System.out.println("Finding purchase for bomItemOption: $bomItemOption")
        Purchase matched = purchases.findResult { purchase ->
            System.out.println("Order Reference: ${purchase.orderReference}, Part: ${purchase.part}, Manufacturer: ${purchase.manufacturer}")
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

        System.out.println(results)
        if (results.empty) {
            return null
        }

        return purchase
    }
}
