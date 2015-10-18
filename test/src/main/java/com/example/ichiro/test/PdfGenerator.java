package com.example.ichiro.test;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PdfGenerator {
    private PdfTask pdfTask;
    private Context context;
    private FileDialog fileSaveDialog;
    private File file;

    private static final String DEST = "result/table.pdf";

    private static final Float PAGE_HEIGHT = 595.0f;
    private static final Float TOP_MARGIN = 40.0f;
    private static final Float BOTTOM_MARGIN = 2.0f;
    private static final Float LEFT_MARGIN = 2.0f;
    private static final Float RIGHT_MARGIN = 2.0f;

    private static final Font tableNameFont = new Font(
            Font.FontFamily.HELVETICA,
            18,
            Font.NORMAL,
            BaseColor.BLUE
    );
    private static final Font headerNameFont = new Font(
            Font.FontFamily.HELVETICA,
            12,
            Font.BOLD,
            BaseColor.MAGENTA
    );
    private static final Font subheaderNameFont = new Font(
            Font.FontFamily.HELVETICA,
            10,
            Font.BOLD,
            BaseColor.MAGENTA
    );
    private static final Font textNameFont = new Font(
            Font.FontFamily.HELVETICA,
            10,
            Font.NORMAL,
            BaseColor.BLACK
    );

    BaseFont bf;
    Font f_title;
    Font f_text;

    public PdfGenerator (Context currentContext) {
        context = currentContext;
        pdfTask = new PdfTask();
    }

    public void setFont() throws DocumentException, IOException {
        try {
            bf = BaseFont.createFont("/system/fonts/DroidSansFallback.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            f_title = new Font(bf, 14);
            f_text = new Font(bf);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//    public static void main(String[] args) throws IOException,
//            DocumentException {
//        File file = new File(DEST);
//        file.getParentFile().mkdirs();
//
//    }

    private void createTitle(Document document, String title)
            throws DocumentException, IOException {


        setFont();

        Paragraph titleParagraph = new Paragraph();
        float spacingAfter = 10.0f;

        titleParagraph.setFont(f_title);
        titleParagraph.add(title);
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        // Instead of the top page margin.
        titleParagraph.setLeading(TOP_MARGIN - spacingAfter);
        titleParagraph.setSpacingAfter(spacingAfter);

        document.add(titleParagraph);
    }

    private void fillPdfFile(File file) throws IOException, DocumentException {
        Document document = new Document(
                PageSize.A4.rotate(),
                LEFT_MARGIN,
                RIGHT_MARGIN,
            /*
            Actually, top margin will define in the padding field
            of the title paragraph.
            */
                TOP_MARGIN - TOP_MARGIN,
                BOTTOM_MARGIN
        );
        OutputStream fos = new FileOutputStream(file);
        PdfWriter.getInstance(document, fos);
        document.open();

        ///////////////////////////////////////////////////////////////////

        createTitle(document, "Table name Font 4 (Я - кириллица, 我 - 中國)");

        /////////////////////////////////////////////////////////////////////

        Paragraph table = new Paragraph();
        Paragraph cellHeader;
        Paragraph cellSubheader;
        Paragraph cellContent;
        PdfPCell cell, nestedTableCell;

        // The main table fits top and bottom parts.
        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100.0f);
        mainTable.setSpacingBefore(0.0f);
        mainTable.setSpacingAfter(0.0f);

        // Calculate row height.
        float rowHeight;
        rowHeight = (PAGE_HEIGHT - TOP_MARGIN - BOTTOM_MARGIN) / 3;

        // Define the top part.
        PdfPCell topTableCell = new PdfPCell();
        topTableCell.setBorder(PdfPCell.NO_BORDER);
        topTableCell.setPadding(0);

        PdfPTable topTable = new PdfPTable(5);
        topTable.setWidthPercentage(100.0f);
        topTable.setSpacingBefore(0.0f);
        topTable.setSpacingAfter(0.0f);

        // Cell 1 (row 1).
        cellHeader = new Paragraph("Header 1 Font 1", headerNameFont);

        cellContent = new Paragraph("Text 1 Font 2", textNameFont);
        cellContent.setFirstLineIndent(20.0f);

        cell = new PdfPCell();

        cell.addElement(cellHeader);
        cell.addElement(cellContent);

        cell.setFixedHeight(rowHeight);
        cell.setBorder(Rectangle.TOP | Rectangle.RIGHT | Rectangle.LEFT);
        topTable.addCell(cell);

        // Cell 2.
        cellHeader = new Paragraph("Header 3 Font 1", headerNameFont);

        cellContent = new Paragraph("Text 3 Font 2", textNameFont);
        cellContent.setFirstLineIndent(20.0f);

        cell = new PdfPCell();

        cell.addElement(cellHeader);
        cell.addElement(cellContent);
        topTable.addCell(cell);

        // Cell 3.
        cellHeader = new Paragraph("Header 4 Font 1", headerNameFont);

        cellContent = new Paragraph("Text 4 Font 2", textNameFont);
        cellContent.setFirstLineIndent(20.0f);

        cell = new PdfPCell();

        cell.addElement(cellHeader);
        cell.addElement(cellContent);

        cell.setRowspan(2);
        topTable.addCell(cell);

        // Cell 4.
        cellHeader = new Paragraph("Header 5 Font 1", headerNameFont);

        cellContent = new Paragraph("Text 5 Font 2", textNameFont);
        cellContent.setFirstLineIndent(20.0f);

        cell = new PdfPCell();

        cell.addElement(cellHeader);
        cell.addElement(cellContent);

        topTable.addCell(cell);

        // Cell 5.
        cellHeader = new Paragraph("Header 6 Font 1", headerNameFont);

        cellContent = new Paragraph("Text 6 Font 2", textNameFont);
        cellContent.setFirstLineIndent(20.0f);

        cell = new PdfPCell();

        cell.addElement(cellHeader);
        cell.addElement(cellContent);

        cell.setBorder(Rectangle.TOP | Rectangle.RIGHT | Rectangle.LEFT);
        topTable.addCell(cell);

        // Cell 6 (row2).
        cellHeader = new Paragraph("Header 2 Font 1", headerNameFont);

        cellContent = new Paragraph("Text 2 Font 2", textNameFont);
        cellContent.setFirstLineIndent(20.0f);

        cell = new PdfPCell();

        cell.addElement(cellHeader);
        cell.addElement(cellContent);

        cell.setFixedHeight(rowHeight);
        cell.setBorder(Rectangle.BOTTOM | Rectangle.RIGHT | Rectangle.LEFT);
        topTable.addCell(cell);

        // Cell 7 fits a nested table.
        PdfPTable nestedTable = new PdfPTable(1);
        nestedTable.setWidthPercentage(100.0f);
        nestedTable.setSpacingBefore(0.0f);
        nestedTable.setSpacingAfter(0.0f);

        // Row 1 in the nested table.
        cellHeader = new Paragraph("Header 9 Font 1", headerNameFont);
        cellSubheader = new Paragraph("SubHeader 8_1 Font 3", subheaderNameFont);
        cellSubheader.setFirstLineIndent(20.0f);
        cellContent = new Paragraph("Text 8_1 Font 2", textNameFont);
        cellContent.setFirstLineIndent(30.0f);

        nestedTableCell = new PdfPCell();
        nestedTableCell.addElement(cellHeader);
        nestedTableCell.addElement(cellSubheader);
        nestedTableCell.addElement(cellContent);

        nestedTableCell.setBorder(PdfPCell.NO_BORDER);
        nestedTableCell.setFixedHeight(rowHeight / 2.0f);
        nestedTable.addCell(nestedTableCell);

        // Row 2 in the nested table.
        cellSubheader = new Paragraph("SubHeader 8_2 Font 3", subheaderNameFont);
        cellSubheader.setFirstLineIndent(20.0f);
        cellContent = new Paragraph("Text 8_2 Font 2", textNameFont);
        cellContent.setFirstLineIndent(30.0f);

        nestedTableCell = new PdfPCell();
        nestedTableCell.addElement(cellHeader);
        nestedTableCell.addElement(cellSubheader);
        nestedTableCell.addElement(cellContent);
        nestedTableCell.setBorder(PdfPCell.NO_BORDER);
        nestedTable.addCell(nestedTableCell);

        cell = new PdfPCell();
        cell.setPadding(0);
        cell.addElement(nestedTable);
        topTable.addCell(cell);

        // Cell 8.
        cellHeader = new Paragraph("Header 9 Font 1", headerNameFont);

        cellContent = new Paragraph("Text 9 Font 2", textNameFont);
        cellContent.setFirstLineIndent(20.0f);

        cell = new PdfPCell();

        cell.addElement(cellHeader);
        cell.addElement(cellContent);

        topTable.addCell(cell);

        // Cell 9.
        cellHeader = new Paragraph("Header 7 Font 1", headerNameFont);

        cellContent = new Paragraph("Text 7 Font 2", textNameFont);
        cellContent.setFirstLineIndent(20.0f);

        cell = new PdfPCell();

        cell.addElement(cellHeader);
        cell.addElement(cellContent);

        cell.setBorder(Rectangle.BOTTOM | Rectangle.RIGHT | Rectangle.LEFT);
        topTable.addCell(cell);

        topTableCell.addElement(topTable);
        mainTable.addCell(topTableCell);

        // Define the bottom part.
        PdfPCell bottomTableCell = new PdfPCell();
        bottomTableCell.setBorder(PdfPCell.NO_BORDER);
        bottomTableCell.setPadding(0);

        PdfPTable bottomTable = new PdfPTable(2);
        bottomTable.setWidthPercentage(100.0f);
        bottomTable.setSpacingBefore(0.0f);
        bottomTable.setSpacingAfter(0.0f);

        // Left cell of the bottom table.
        cellHeader = new Paragraph("Header 10 Font 1", headerNameFont);

        cellContent = new Paragraph("Text 10 Font 2", textNameFont);
        cellContent.setFirstLineIndent(20.0f);

        cell = new PdfPCell();

        cell.addElement(cellHeader);
        cell.addElement(cellContent);
        cell.setFixedHeight(rowHeight);
        bottomTable.addCell(cell);

        // Right cell of the bottom table.
        cellHeader = new Paragraph("Header 11 Font 1", headerNameFont);

        cellContent = new Paragraph("Text 11 Font 2", textNameFont);
        cellContent.setFirstLineIndent(20.0f);

        cell = new PdfPCell();

        cell.addElement(cellHeader);
        cell.addElement(cellContent);
        bottomTable.addCell(cell);

        bottomTableCell.addElement(bottomTable);
        mainTable.addCell(bottomTableCell);

        table.add(mainTable);
        document.add(table);

        document.close();
    }

    /**
     * Main method
     */
    public void execute() {
        fileSaveDialog =  new FileDialog (
                context,
                DialogType.FILE_SAVE,
                new FileDialog.FileDialogListener() {
                    @Override
                    public void onChosenDir(String chosenDir) {
                        /* The code in this function will be executed when the dialog
                           OK button is pushed */
                        try {
                            file = new File(chosenDir);
                            pdfTask.execute();
                        } catch (Exception e) {
                            // TODO
                        }
                    }
                });

        fileSaveDialog.chooseFileOrDir();
    }

//    private File createPdfFile() {
//
//        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            return null;
//        } else {
//            String dir = Environment.getExternalStorageDirectory() + File.separator + "pdfdemo";
//
//            File folder = new File(dir);
//            if (!folder.exists()) {
//                folder.mkdirs();
//            }
//            return new File(dir, "test.pdf");
//        }
//    }


//    private void fillPdfFile(File file) throws FileNotFoundException, DocumentException {
//        OutputStream fos = new FileOutputStream(file);
//        try {
//            createPdf(DEST);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Document doc = new Document();
//        PdfWriter.getInstance(doc, fos);
//
//        doc.open();
//        doc.add(new Paragraph("Hello pdf lalalalala!!"));
//        doc.close();
//    }

    private void viewPdfFile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(
            String.format(
                context.getResources().getString(R.string.msg_file_written),
                file.toString()
            )
        );
        builder.setIcon(android.R.drawable.ic_dialog_info);

        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                context.startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
       builder.show();
    }

    /**
     *
     */
    private class PdfTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(context, "", "Loading. Please wait...", true);
            dialog.show();
        }

        /**
         * @param
         * @return
         */
        @Override
        protected Void doInBackground(Void... params) {
            try {
                fillPdfFile(file);
            } catch (DocumentException de) {
                // TODO
            } catch (FileNotFoundException fnfe) {
                Log.w("ExternalStorage", String.format("Error writing %s", file), fnfe);
            } catch (IOException e) {
                e.printStackTrace(); // TODO
            }
            return null;
        }

        /**
         * @param result
         */
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            viewPdfFile();
        }
    }

}
