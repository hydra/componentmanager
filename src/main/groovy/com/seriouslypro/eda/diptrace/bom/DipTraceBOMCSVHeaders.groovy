package com.seriouslypro.eda.diptrace.bom

import com.seriouslypro.csv.CSVColumn

enum DipTraceBOMCSVHeaders implements CSVColumn<DipTraceBOMCSVHeaders> {
    REFDES_LIST(["RefDes"]),
    VALUE(["Value"]),
    NAME(["Name"]),
    QUANTITY(["Quantity"])

    DipTraceBOMCSVHeaders(List<String> aliases = []) {
        this.aliases = aliases
    }
}
