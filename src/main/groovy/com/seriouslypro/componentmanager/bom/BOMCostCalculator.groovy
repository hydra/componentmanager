package com.seriouslypro.componentmanager.bom

import com.seriouslypro.componentmanager.currency.Currency
import com.seriouslypro.csv.CSVInput
import com.seriouslypro.eda.diptrace.bom.DipTraceBOMCSVInput

class BOMCostCalculator {
    String bomFileName
    String purchasesFileName
    Currency currency

    BOMCostResult calculate() {

        Reader bomReader = new FileReader(bomFileName)
        CSVInput csvInput = new DipTraceBOMCSVInput(bomFileName, bomReader)

        BOMCostResult result = new BOMCostResult()
        result.currency = currency


        return result
    }
}
