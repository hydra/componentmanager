ComponentManager
================

by Dominic Clifton. (C) 2018-2021

Utilities to manage costing/pricing for PCB assembly.

Background
==========

When designing PCBs for you often need to know the price or cost of a design, a price when it's a new design and you
don't have component stock, or cost if you've already purchased components.

Often you will buy more components than you need because they have minimum order quantities or come in media that holds
a fixed amount.  When this happens you seek to use these components before either changing to a different component
or simply ordering more.

EDA tools, like DipTrace often don't use a specific part because in many cased multiple suppliers can provide parts.
For example, you often specify the minimum specification, such as 100nF 6.3V 20% capacitor, but in reality you can often
use a higher specification part, such as a 100nF 50V 10% capacitor.

There are other constraints too, like how many feeders your pick and place machine has, product shelf-life, etc.  These
constraints often cause you to reduce the number of different components you stock, so that you can use them all before
they go bad, or so that you can use them in multiple different designs.  e.g.  why stock the lower specification part
if it's almost the same price as the higher specification part which can be used in more designs?

As a manufacturer you often have to use multiple suppliers due to pricing, availability and shipping concerns.

Pricing is further complicated by fluctuating prices and fluctuating exchange rates.

Given the above, what you end up with is a time-consuming nightmare of manually cross-referencing data from multiple
sources which cannot be easily updated.

Also, if the process of getting a price is complicated, then it makes the practice of design-to-a-price much more
difficult as you cannot easily see how adding components changes the price.  

Scope
=====

The tools in this project seeks to reduce your workload by:

* Collating purchase history from multiple suppliers so it can be easily searched, exported and reused.
* Define a simple format for globally mapping design specifications to orderable components.
* Define a simple format for part substitutions, to allow for component library re-use.
* Producing a cost, given a BOM, purchase history, mappings and substitution lists.

Supported suppliers
===================

* LCSC
* Mouser
* Farnell

Supported for other suppliers is planned.
Adding a supplier support is reasonably straightforward though as long as the supplier's website allows you to export one
or more CSV files containing: order date, supplier reference, manufacturer reference, manufacturer, cost and currency. 
The order reference also needs to be determined, usually either from the filename or from the data in the exported file. 

It's technically possible to support suppliers using their website APIs.
In practice suppliers generally can't get a simple order history export to CSV working
so YMMV if you try to use their APIs... See developer notes in the code for examples of broken-ness and pain.

Workflow
========

The workflow is currently:
1) export your past purchases (orders) from supported suppliers (e.g. LCSC) into supported input formats (e.g. CSV) 
2) run PurchaseCombiner to update a purchase history spreadsheet (e.g. on google sheets via the Google Sheets API) 
note: currently running purchase combiner multiple times will create duplicate rows in the spreadsheet but is non-destructive, 
recommended to use version control on the generated file so that it can be reverted if required.
when using google sheets export you can enable version history for the file in google sheets.
3) manually append line items to the spreadsheet for purchases from unsupported suppliers.

Then:

4) Export BOM from EDA tool (DipTrace)
5) create Name + Value to Name + Value part substitutions file for the project (design specific substitutions)

e.g.
```csv
"Name";"Value";"Name";"Value"
"CAP_0402";"2.2uF 6.3V 0402";"CAP_0402";"2.2uF 10V 0402 X5R 10%"
```
6) create Name + Value to Order-code & Manufacturer file for each component to be used. (an EDA to order-code mapping)

Regular expressions are supported in the patterns.
```csv
"Name Pattern";"Value Pattern";"Part Code";"Manufacturer"
"CAP_0402";"2.2uF 10V 0402 X5R 10%";"CL05A225KP5NSNC";"Samsung Electro-Mechanics"
"SM04B-SRSS-TB";"/.*/";"AFC10-S04QCC-00";"JUSHUO"
```

7) run BOMCost to calculate the cost for the BOM.
it will print out the costs of previously-ordered parts and sum the currencies used.
e.g.

```
CAP_0402, 2.2uF 6.3V 0402 -> CAP_0402, 2.2uF 10V 0402 X5R 10% -> Manufacturer: Samsung Electro-Mechanics, Part Code: CL05A225KP5NSNC, Supplier: LCSC, Order reference: 20200101AAAA, Order date: 2020-01-01, Unit price: 0.0034 USD
SM04B-SRSS-TB, RX -> Manufacturer: JUSHUO, Part Code: AFC10-S04QCC-00, Supplier: LCSC, Order reference: 20210101BBBB, Order date: 2021-01-01, Unit price: 0.0444 USD
RES_0402, 1K 0402 5% -> Manufacturer: TE CONNECTIVITY, Part Code: CRGCQ0402J1K0, Supplier: Farnell, Order reference: 12344321, Order date: 2019-01-01, Unit price: 0.0020 EUR
Unmatched BOM items
CAP_0402, 4.7uF 6.3V 0402 X5R 10%, [C4, C11]
	Substituted from CAP_0402, 4.7uF 6.3V 0402 10%
	0 -> Manufacturer: YAGEO, Part Code: CC0402KRX5R5BB475
SOLDER_PAD, , [SP7, SP8]
Cost: [USD:9.9999, EUR:8.8888, GBP:7.7777]
```

the resulting CSV file will contain data like this 
```csv
"REFDES", "NAME","VALUE","SUBSTITUTE_NAME","SUBSTITUTE_VALUE","MANUFACTURER","PART_CODE","SUPPLIER","ORDER_REFERENCE","ORDER_DATE","QUANTITY","UNIT_PRICE","LINE_PRICE","CURRENCY"
"C1, C2","CAP_0402","2.2uF 6.3V 0402","CAP_0402","2.2uF 10V 0402 X5R 10%","Samsung Electro-Mechanics","CL05A225KP5NSNC","LCSC","2020101AAAA","2020-01-01","3","0.0034","0.0102","USD"
```

it's also possible to then:

8) check your inventory against the selected BOM components.
9) order new/out-of-stock parts, sometimes by uploading the resulting 'bom-cost.csv' to a supplier.

Limitations
===========
PurchaseCombiner
 * Doesn't handle duplicates and just inserts rows, see workflow.
 - Digikey and Arrow support is planned.
 + It's still much quicker than doing it manually.
 
BOMCost
 - Work-in-progress.

Building
========
Requires 'pnpconvert' as a sibling to the 'componentmanager' directory so that gradle can find it.

Testing
=======

`gradlew test`

Installation
============

`gradlew installDist`

Running
=======
Only from IDE at the moment.  Run `main()` in BOMCost.groovy or PurchaseCombiner.groovy

DipTrace Export Settings
========================

Export a CSV file.
Group rows by: `Name, Value and Pattern`
Column divider: `;`


Header:
```csv
RefDes;"Value";"Name";"Part";"Quantity";"Manufacturer";"Datasheet";"Number of Pins";"Pattern"
```