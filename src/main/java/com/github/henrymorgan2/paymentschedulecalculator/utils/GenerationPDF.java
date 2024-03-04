package com.github.henrymorgan2.paymentschedulecalculator.utils;

import com.github.henrymorgan2.paymentschedulecalculator.dto.EntryPaymentShedule;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.lowagie.text.DocumentException;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.util.List;
import java.util.stream.Stream;

@Component
public class GenerationPDF{

    public byte[] generatePdfFromHtml(List<EntryPaymentShedule> list) throws FileNotFoundException, com.itextpdf.text.DocumentException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

//        String outputFolder = System.getProperty("user.home") + File.separator + "thymeleaf.pdf";
//        OutputStream outputStream = new FileOutputStream(outputFolder);
        Document document = new Document();
//        PdfWriter.getInstance(document, outputStream);
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

        Stream.of("Day of payment", "Payment amount", "Interest amount", "Loan body", "Balance owed")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });
    }

    private void addRows(PdfPTable table, List<EntryPaymentShedule> list) {

        for (EntryPaymentShedule entryPaymentShedule: list) {
            table.addCell(entryPaymentShedule.getWorkingDayOfPayment());
            table.addCell(String.valueOf(entryPaymentShedule.getMonthlyPaymentAmount()));
            table.addCell(String.valueOf(entryPaymentShedule.getInterestAmountPerMonth()));
            table.addCell(String.valueOf(entryPaymentShedule.getLoanBody()));
            table.addCell(String.valueOf(entryPaymentShedule.getBalanceOwed()));
        }

    }

    public String parseThymeleafTemplate(List<EntryPaymentShedule> list) {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setTemplateMode(TemplateMode.HTML);
//        templateResolver.setForceTemplateMode(true);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

//        HashMap<String, String> mapHeader = new HashMap<>();
//        EntryPaymentShedule entryPaymentShedule = new EntryPaymentShedule("Дата платежа", "Платеж", "Проценты", "Тело кредита", "Сумма основного долга");
//        EntryPaymentShedule entryPaymentShedule = new EntryPaymentShedule(1,2,3,4,5);

//        mapHeader.put("workingDayOfPayment", "Дата платежа");
//        mapHeader.put("monthlyPaymentAmount", "Платеж");
//        mapHeader.put("interestAmountPerMonth", "Проценты");
//        mapHeader.put("loanBody", "Тело кредита");
//        mapHeader.put("balanceOwed", "Сумма основного долга");


//        List<HashMap> hashMapList = new ArrayList<>();
//        HashMap<String, String> hashMap = new HashMap<>();
//        hashMap.put("workingDayOfPayment", "Petrov");
//        hashMap.put("monthlyPaymentAmount", "Ivan");
//        hashMap.put("interestAmountPerMonth", "Ivan2");
//        hashMap.put("loanBody", "Ivan3");
//        hashMap.put("balanceOwed", "Ivan4");
//        hashMapList.add(hashMap);

//        List<EntryPaymentShedule> list = new ArrayList<>();
//        list.add(entryPaymentShedule);


        Context context = new Context();
        context.setVariable("list", list);
//        context.setVariable("list", hashMapList);

        return templateEngine.process("templates/paymentSchedule", context);
    }

    public void generatePdfFromHtmlOld(String html) throws DocumentException, IOException {
        String outputFolder = System.getProperty("user.home") + File.separator + "thymeleaf.pdf";
        OutputStream outputStream = new FileOutputStream(outputFolder);

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(outputStream);

        outputStream.close();
    }

}
