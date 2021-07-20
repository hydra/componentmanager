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

Workflow
========

The workflow is currently:
1) export your past purchases (orders) from supported suppliers (e.g. LCSC) into supported input formats (e.g. CSV) 
2) run PurchaseCombiner to update the purchase history.

Then:

1) Export BOM from EDA tool (DipTrace)
2) create Name + Value to Name + Value part substitutions file for the project (design specific substitutions)
3) create Name + Value to Order-code & Manufacturer file for each component to be used. (an EDA to order-code mapping)
4) run BOMCost to calculate the cost for the BOM.
5) repeat as required.

Limitations
===========
PurchaseCombiner
 * Always start with an empty sheet, currently it doesn't handle duplicates and just inserts rows.
 - Only supports LCSC.  Mouser, Farnell, Digikey are planned.
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