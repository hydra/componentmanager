package com.seriouslypro.eda.diptrace.bom

import com.seriouslypro.csv.CSVInput
import com.seriouslypro.eda.BOMItem

class DipTraceBOMCSVInput extends CSVInput<BOMItem, DipTraceBOMCSVHeaders> {

    DipTraceBOMCSVInput(String reference, Reader reader) {
        super(reference, reader, new DipTraceBOMHeaderParser(), new DipTraceBOMLineParser(), ';' as char)
    }
}
