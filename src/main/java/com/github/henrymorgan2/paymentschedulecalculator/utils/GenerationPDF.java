package com.github.henrymorgan2.paymentschedulecalculator.utils;

import com.github.henrymorgan2.paymentschedulecalculator.dto.EntryPaymentShedule;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Component
public class GenerationPDF {

    static List<String> namesHeaderColumns = List.of("Day of payment", "Payment amount", "Interest amount", "Loan body", "Balance owed");

    public byte[] generatePdfFromList(List<EntryPaymentShedule> list) throws com.itextpdf.text.DocumentException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Document document = new Document();

        PdfWriter.getInstance(document, byteArrayOutputStream);
        document.open();

        PdfPTable table = new PdfPTable(5);
        addTableHeader(table);
        addRows(table, list);

        document.add(table);
        document.close();

        return byteArrayOutputStream.toByteArray();

    }

    private void addTableHeader(PdfPTable table) {

        namesHeaderColumns.stream()
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });
    }

    private void addRows(PdfPTable table, List<EntryPaymentShedule> list) {

        for (EntryPaymentShedule entryPaymentShedule : list) {
            table.addCell(entryPaymentShedule.getWorkingDayOfPayment().toString());
            table.addCell(String.valueOf(entryPaymentShedule.getMonthlyPaymentAmount()));
            table.addCell(String.valueOf(entryPaymentShedule.getInterestAmountPerMonth()));
            table.addCell(String.valueOf(entryPaymentShedule.getLoanBody()));
            table.addCell(String.valueOf(entryPaymentShedule.getBalanceOwed()));
        }

    }

}
