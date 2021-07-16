package com.seriouslypro.componentmanager.purchasecombiner

import com.google.api.client.auth.oauth2.Credential
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.seriouslypro.googlesheets.CredentialFactory
import com.seriouslypro.googlesheets.GoogleSheetsTransportFactory
import com.seriouslypro.googlesheets.SheetFinder
import com.seriouslypro.googlesheets.SheetNotFoundException
import com.seriouslypro.googlesheets.SheetsBuilder
import com.seriouslypro.googlesheets.TransportFactory
import groovy.io.FileType

class PurchaseCombinerGoogleSheetsUpdater {

    public static final String SHEET_TITLE_PURCHASE_HISTORY = 'PurchaseHistory'

    String sheetId
    String credentialsFileName
    String sourceDirectory

    CredentialFactory credentialFactory = new CredentialFactory()
    TransportFactory transportFactory = new GoogleSheetsTransportFactory()

    SheetsBuilder sheetsBuilder = new SheetsBuilder()
    SheetFinder sheetFinder = new SheetFinder()


    def update() {
        def transport = transportFactory.build()

        Credential credentials = credentialFactory.getCredential(transport, credentialsFileName)

        Sheets service = sheetsBuilder.build(transport, credentials)

        Spreadsheet spreadsheet = service.spreadsheets().get(sheetId).execute()

        SpreadsheetProperties spreadsheetProperties = spreadsheet.getProperties()

        String sheetTitle = spreadsheetProperties.getTitle()

        Sheet purchaseHistorySheet = sheetFinder.findByTitle(spreadsheet, SHEET_TITLE_PURCHASE_HISTORY)
        if (purchaseHistorySheet == null) {
            throw new SheetNotFoundException(SHEET_TITLE_PURCHASE_HISTORY)
        }

        new File(sourceDirectory).eachFileMatch(FileType.FILES, ~/.*\.csv/) { sourceFile ->
            PurchaseCSVProcessor purchaseCSVProcessor = new PurchaseCSVProcessor(
                service: service,
                spreadsheet: spreadsheet,
                sheet: purchaseHistorySheet
            )
            purchaseCSVProcessor.process(sourceFile)
        }
    }
}
