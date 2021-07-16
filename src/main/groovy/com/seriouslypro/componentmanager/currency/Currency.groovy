package com.seriouslypro.componentmanager.currency

enum Currency {
    USD('$'),
    EUR('€'),
    GBP('£')

    String symbol

    Currency(String symbol) {
        this.symbol = symbol
    }

    boolean matches(String candidate) {
        boolean symbolMatched = symbol == candidate
        if (symbolMatched) {
            return true
        }
        boolean nameMatched = this.toString().toLowerCase() == candidate.toLowerCase()
        if (nameMatched) {
            return true
        }

        return false
    }

    static <E extends Currency> E fromSymbol(Class<E> e, String column) {

        def enumSet = EnumSet.allOf(e)
        return enumSet.find { it ->
            it.matches(column)
        }
    }
}