package com.seriouslypro.eda.diptrace.bom

import com.seriouslypro.csv.CSVInputContext
import com.seriouslypro.csv.CSVLineParserBase
import com.seriouslypro.eda.BOMItem

class DipTraceBOMLineParser extends CSVLineParserBase<BOMItem, DipTraceBOMCSVHeaders> {

    @Override
    BOMItem parse(CSVInputContext context, String[] rowValues) {
        BOMItem bomItem = new BOMItem(
            refdesList: rowValues[columnIndex(context, DipTraceBOMCSVHeaders.REFDES_LIST)].split(',').collect { refdes -> refdes.trim()},
            name: rowValues[columnIndex(context, DipTraceBOMCSVHeaders.NAME)],
            value: rowValues[columnIndex(context, DipTraceBOMCSVHeaders.VALUE)],
            quantity: rowValues[columnIndex(context, DipTraceBOMCSVHeaders.QUANTITY)] as Integer,
        )
        return bomItem
    }
}
