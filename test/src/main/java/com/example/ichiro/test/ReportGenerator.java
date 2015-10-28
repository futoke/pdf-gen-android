package com.example.ichiro.test;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

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

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportGenerator {
    private GenTask genTask;
    private Context context;
    private FileSaveDialog fileSaveDialog;
    private File file; // The name of the generated file with path.

    private String tableName;
    private String[] cellContent;
    private String[] cellHeaders;
    private String[][] pages;

    private static final String LINE_SEPARATOR =
            System.getProperty("line.separator");
    private static final String DOCX_FILE_TEMPLATE = "template.docx";
    private static final String DOCX_TABLE_TEMPLATE = "table.xml";
    private static final String DOCX_DOCUMENT_PATH = "word/document.xml";
    private static final String EMPTY_STRING = "";
    private static final String PATH_TO_DROID_SANS =
            "/system/fonts/DroidSansFallback.ttf";
    private static final String APPLICATION_PDF = "application/pdf";
    private static final String APPLICATION_DOCX =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String CODING_UTF8 = "utf-8";

    private static final byte ENGLISH = 0;
    private static final byte RUSSIAN = 1;
    private static final byte CHINESE = 2;

    private static final Byte HEADER_1 = 0;
    private static final Byte HEADER_3 = 1;
    private static final Byte HEADER_4 = 2;
    private static final Byte HEADER_5 = 3;
    private static final Byte HEADER_6 = 4;
    private static final Byte HEADER_2 = 5;
    private static final Byte HEADER_8 = 6;
    private static final Byte HEADER_81 = 7;
    private static final Byte HEADER_82 = 8;
    private static final Byte HEADER_9 = 9;
    private static final Byte HEADER_7 = 10;
    private static final Byte HEADER_10 = 11;
    private static final Byte HEADER_11 = 12;

    private static final Byte CONTENT_1 = 0;
    private static final Byte CONTENT_3 = 1;
    private static final Byte CONTENT_4 = 2;
    private static final Byte CONTENT_5 = 3;
    private static final Byte CONTENT_6 = 4;
    private static final Byte CONTENT_2 = 5;
    private static final Byte CONTENT_81 = 6;
    private static final Byte CONTENT_82 = 7;
    private static final Byte CONTENT_9 = 8;
    private static final Byte CONTENT_7 = 9;
    private static final Byte CONTENT_10 = 10;
    private static final Byte CONTENT_11 = 11;

    // Play with this constants to change capacity of the cells.
    private static final Integer ENGLISH_CHUNK_SIZES[] = {
            300, // Content 1.
            300, // Content 3.
            730, // Content 4.
            300, // Content 5.
            300, // Content 6.
            300, // Content 2.
            200, // Content 81.
            200, // Content 82.
            300, // Content 9.
            300, // Content 7.
            700, // Content 10.
            700  // Content 11.
    };
    private static final Integer RUSSIAN_CHUNK_SIZES[] = {
            280, // Content 1.
            250, // Content 3.
            630, // Content 4.
            250, // Content 5.
            280, // Content 6.
            280, // Content 2.
            180, // Content 81.
            180, // Content 82.
            280, // Content 9.
            280, // Content 7.
            560, // Content 10.
            560  // Content 11.
    };
    private static final Integer CHINESE_CHUNK_SIZES[] = {
            130, // Content 1.
            130, // Content 3.
            300, // Content 4.
            130, // Content 5.
            130, // Content 6.
            130, // Content 2.
            80, // Content 81.
            80, // Content 82.
            130, // Content 9.
            130, // Content 7.
            300, // Content 10.
            300  // Content 11.
    };
    private static final Integer COPY_BUFFER_SIZE = 1024;

    private static final Float PAGE_HEIGHT = 595.0f;
    private static final Float TOP_MARGIN = 40.0f;
    private static final Float BOTTOM_MARGIN = 2.0f;
    private static final Float LEFT_MARGIN = 2.0f;
    private static final Float RIGHT_MARGIN = 2.0f;
    private static final Float CELL_FIRST_LINE_INDENT = 20.0f;
    private static final Float ADDITION_INDENT = 10.0f;
    private static final Float SPACE_AFTER = 10.0f;

    private static BaseFont droidSans;

    private static Font tableNameFont;
    private static Font headerFont;
    private static Font subHeaderFont;
    private static Font textFont;
    private static Font textFontSmall;

    /**
     * The constructor of the class.
     *
     * @param context The Activity context.
     * @param tableName The table name. Place in the header.
     * @param cellContent The array of the cells' content.
     * @param cellHeaders The array of the cells' headers.
     */
    public ReportGenerator(
            Context context,
            String tableName,
            String[] cellContent,
            String[] cellHeaders)
    {
        this.context = context;
        this.tableName = tableName;
        this.cellContent = cellContent;
        this.cellHeaders = cellHeaders;

        genTask = new GenTask();
    }

    /**
     * The function detects language of a string.
     *
     * @param str The input string.
     * @return Language as a constant from the list {RUSSIAN, ENGLISH, CHINESE}.
     */
    private byte
    detectLang(String str)
    {
        Integer[] freqLangList;
        Integer freqLang;
        Byte mostCommonLang;
        Character.UnicodeBlock ub;

        mostCommonLang = ENGLISH;
        freqLangList = new Integer[]{0, 0, 0};
        freqLang = 0;

        for (char c : str.toCharArray() ) {
            if (Character.isLetter(c)) {
                ub = Character.UnicodeBlock.of(c);
                if (ub == Character.UnicodeBlock.BASIC_LATIN) {
                    freqLangList[ENGLISH] += 1;
                } else if (ub == Character.UnicodeBlock.CYRILLIC) {
                    freqLangList[RUSSIAN] += 1;
                } else {
                    freqLangList[CHINESE] += 1;
                }
            }
        }
        for (Byte i = 0; i < 3; i++) {
            if (freqLangList[i] > freqLang) {
                freqLang = freqLangList[i];
                mostCommonLang = i;
            }
        }
        return mostCommonLang;
    }

    /**
     * The method divides long content into chunks.
     */
    private void
    divIntoPages()
    {
        Integer cols;
        Integer rows;
        Integer cnt;
        Byte lang;
        String buffer;
        List<String> dividedCell;
        List<List<String>> cellChunks;

        rows = 0;
        cols = cellContent.length;
        cellChunks = new ArrayList<>();


        // Combine all strings to detect language.
        lang = detectLang(StringUtils.join(cellContent, EMPTY_STRING));


        for (int i = 0; i < cols; i++) {
            switch (lang) {
                case ENGLISH:
                    buffer = WordUtils.wrap(
                            cellContent[i],
                            ENGLISH_CHUNK_SIZES[i]
                    );
                    break;
                case RUSSIAN:
                    buffer = WordUtils.wrap(
                            cellContent[i],
                            RUSSIAN_CHUNK_SIZES[i]
                    );
                    break;
                case CHINESE:
                    buffer = "";
                    cnt = 0;
                    for (char c : cellContent[i].toCharArray()) {
                        buffer += c;
                        if (cnt.equals(CHINESE_CHUNK_SIZES[i])) {
                            cnt = 0;
                            buffer += LINE_SEPARATOR;
                        } else {
                            cnt++;
                        }
                    }
//                    buffer = WordUtils.wrap(
//                            cellContent[i],
//                            CHINESE_CHUNK_SIZES[i],
//                            LINE_SEPARATOR,
//                            true
//                    );
                    break;
                default:
                    buffer = WordUtils.wrap(
                            cellContent[i],
                            ENGLISH_CHUNK_SIZES[i]
                    );
            }
            dividedCell = new ArrayList<>(
                    Arrays.asList(
                            buffer.split(LINE_SEPARATOR)
                    )
            );
            cellChunks.add(dividedCell);

            if (rows < dividedCell.size()) {
                rows = dividedCell.size();
            }
        }
        pages = new String[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                try {
                    pages[i][j] = cellChunks.get(j).get(i);
                } catch (IndexOutOfBoundsException iobe) {
                    pages[i][j] = EMPTY_STRING;
                }

            }
        }
    }

    /**
     * The method creates the font styles.
     */
    private void
    createFonts()
    {
        try {
            droidSans = BaseFont.createFont(
                    PATH_TO_DROID_SANS,
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        tableNameFont = new Font(droidSans, 18, Font.NORMAL, BaseColor.BLUE);
        headerFont = new Font(droidSans, 12, Font.BOLD, BaseColor.MAGENTA);
        subHeaderFont = new Font(droidSans, 10, Font.BOLD, BaseColor.MAGENTA);
        textFont = new Font(droidSans, 10, Font.NORMAL, BaseColor.BLACK);
        textFontSmall = new Font(droidSans, 7, Font.NORMAL, BaseColor.BLACK);
    }

    /**
     * The method creates the title of the table.
     *
     * @param document The object of the pdf document class.
     * @param title The title of the page.
     * @throws DocumentException
     */
    private void
    createTitle(Document document, String title)
            throws DocumentException
    {
        Paragraph titleParagraph = new Paragraph();

        titleParagraph.setFont(tableNameFont);
        titleParagraph.add(title);
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        // Instead of the top page margin.
        titleParagraph.setLeading(TOP_MARGIN - SPACE_AFTER);
        titleParagraph.setSpacingAfter(SPACE_AFTER);

        document.add(titleParagraph);
    }

    /**
     * The method creates a cell of the table (object).
     *
     * @param header The header of the cell.
     * @param content The content of the cell.
     * @return The instance of the cell class.
     */
    private PdfPCell
    createCell(String header, String content)
    {
        PdfPCell cell;
        Paragraph cellContent;

        cell = new PdfPCell();
        cellContent = new Paragraph(content, textFont);

        cellContent.setFirstLineIndent(CELL_FIRST_LINE_INDENT);

        cell.addElement(new Paragraph(header, headerFont));
        cell.addElement(cellContent);

        return cell;
    }

    /**
     * The method creates the nested table (Header 8).
     *
     * @param header Header 8.
     * @param subHeader_1 Header 8.1.
     * @param subHeader_2 Header 8.2.
     * @param content_1 Content 8.1.
     * @param content_2 Content 8.2.
     * @return The instance of the cell class.
     */
    private PdfPCell
    createNestedTable(
            String header,
            String subHeader_1,
            String subHeader_2,
            String content_1,
            String content_2)
    {
        PdfPTable nestedTable = new PdfPTable(1);
        nestedTable.setWidthPercentage(100.0f);
        nestedTable.setSpacingBefore(0.0f);
        nestedTable.setSpacingAfter(0.0f);

        // Row 1 in the nested table.
        Paragraph cellHeader = new Paragraph(header, headerFont);
        Paragraph cellSubHeader = new Paragraph(subHeader_1, subHeaderFont);
        cellSubHeader.setFirstLineIndent(CELL_FIRST_LINE_INDENT);
        Paragraph cellContent = new Paragraph(content_1, textFontSmall);
        cellContent.setFirstLineIndent(
                CELL_FIRST_LINE_INDENT + ADDITION_INDENT
        );

        PdfPCell nestedTableCell = new PdfPCell();
        nestedTableCell.addElement(cellHeader);
        nestedTableCell.addElement(cellSubHeader);
        nestedTableCell.addElement(cellContent);

        nestedTableCell.setBorder(PdfPCell.NO_BORDER);
        nestedTableCell.setFixedHeight(
                (PAGE_HEIGHT - TOP_MARGIN - BOTTOM_MARGIN) / 6
        );
        nestedTable.addCell(nestedTableCell);

        // Row 2 in the nested table.
        cellSubHeader = new Paragraph(subHeader_2, subHeaderFont);
        cellSubHeader.setFirstLineIndent(CELL_FIRST_LINE_INDENT);
        cellContent = new Paragraph(content_2, textFontSmall);
        cellContent.setFirstLineIndent(30.0f);

        nestedTableCell = new PdfPCell();
        nestedTableCell.addElement(cellHeader);
        nestedTableCell.addElement(cellSubHeader);
        nestedTableCell.addElement(cellContent);
        nestedTableCell.setBorder(PdfPCell.NO_BORDER);
        nestedTable.addCell(nestedTableCell);

        PdfPCell cell = new PdfPCell();
        cell.setPadding(0);
        cell.addElement(nestedTable);

        return cell;
    }

    /**
     * The method creates a main table.
     *
     * @param document The instance of the Document class.
     * @param cellContentChunks The chunks of content for the all cells.
     * @throws DocumentException
     */
    private void
    createTable(Document document, String[] cellContentChunks)
            throws DocumentException
    {
        Paragraph table = new Paragraph();
        float rowHeight;
        PdfPCell cell;

        // The main table fits top and bottom parts.
        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100.0f);
        mainTable.setSpacingBefore(0.0f);
        mainTable.setSpacingAfter(0.0f);

        // Calculate row height.
        rowHeight = (PAGE_HEIGHT - TOP_MARGIN - BOTTOM_MARGIN) / 3;

        // Define the top part of the table.
        PdfPCell topTableCell = new PdfPCell();
        topTableCell.setBorder(PdfPCell.NO_BORDER);
        topTableCell.setPadding(0);

        PdfPTable topTable = new PdfPTable(5);
        topTable.setWidthPercentage(100.0f);
        topTable.setSpacingBefore(0.0f);
        topTable.setSpacingAfter(0.0f);

        // Cell 1 (row 1).
        cell = createCell(cellHeaders[HEADER_1], cellContentChunks[CONTENT_1]);
        cell.setFixedHeight(rowHeight);
        cell.setBorder(Rectangle.TOP | Rectangle.RIGHT | Rectangle.LEFT);
        topTable.addCell(cell);

        // Cell 2.
        cell = createCell(cellHeaders[HEADER_3], cellContentChunks[CONTENT_3]);
        topTable.addCell(cell);

        // Cell 3.
        cell = createCell(cellHeaders[HEADER_4], cellContentChunks[CONTENT_4]);
        cell.setRowspan(2);
        topTable.addCell(cell);

        // Cell 4.
        cell = createCell(cellHeaders[HEADER_5], cellContentChunks[CONTENT_5]);
        topTable.addCell(cell);

        // Cell 5.
        cell = createCell(cellHeaders[HEADER_6], cellContentChunks[CONTENT_6]);
        cell.setBorder(Rectangle.TOP | Rectangle.RIGHT | Rectangle.LEFT);
        topTable.addCell(cell);

        // Cell 6 (row2).
        cell = createCell(cellHeaders[HEADER_2], cellContentChunks[CONTENT_2]);
        cell.setFixedHeight(rowHeight);
        cell.setBorder(Rectangle.BOTTOM | Rectangle.RIGHT | Rectangle.LEFT);
        topTable.addCell(cell);

        // Cell 7 fits a nested table.
        cell = createNestedTable(
                cellHeaders[HEADER_8],
                cellHeaders[HEADER_81],
                cellHeaders[HEADER_82],
                cellContentChunks[CONTENT_81],
                cellContentChunks[CONTENT_82]
        );
        topTable.addCell(cell);

        // Cell 8.
        cell = createCell(cellHeaders[HEADER_9], cellContentChunks[CONTENT_9]);
        topTable.addCell(cell);

        // Cell 9.
        cell = createCell(cellHeaders[HEADER_7], cellContentChunks[CONTENT_7]);
        cell.setBorder(Rectangle.BOTTOM | Rectangle.RIGHT | Rectangle.LEFT);
        topTable.addCell(cell);

        // Add the top table into the main table.
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
        cell = createCell(cellHeaders[HEADER_10], cellContentChunks[CONTENT_10]);
        cell.setFixedHeight(rowHeight);
        bottomTable.addCell(cell);

        // Right cell of the bottom table.
        cell = createCell(cellHeaders[HEADER_11], cellContentChunks[CONTENT_11]);
        bottomTable.addCell(cell);

        // Add the bottom table into the main table.
        bottomTableCell.addElement(bottomTable);
        mainTable.addCell(bottomTableCell);

        table.add(mainTable);

        document.add(table);
    }


    /**
     * The method gets the table template from assets.
     *
     * @return The xml code of the table.
     */
    private
    String getDocxTable ()
    {
        InputStream in = null;
        AssetManager assetManager = context.getAssets();
        try {
            in = assetManager.open(DOCX_TABLE_TEMPLATE);
            return IOUtils.toString(in, CODING_UTF8);
        } catch (IOException e) {
            Log.e(
                    "Assets",
                    String.format(
                            "Failed to copy asset file: %s",
                            DOCX_TABLE_TEMPLATE
                    ),
                    e
            );
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    Log.e("IO", "General input/output error", ioe);
                }
            }
        }
        return null;
    }


    /**
     * The method gets the template of the DOCX file from assets.
     *
     * @param destFilePath The full file path in assets.
     */
    private void
    copyDocxTemplateFromAssets(File destFilePath)
    {
        AssetManager assetManager = context.getAssets();
        InputStream in = null;
        OutputStream out = null;

        try {
            in = assetManager.open(DOCX_FILE_TEMPLATE);
            out = new FileOutputStream(destFilePath);
            copyFile(in, out);
        } catch(IOException e) {
            Log.e(
                    "Assets",
                    String.format(
                            "Failed to copy asset file: %s",
                            destFilePath
                    ),
                    e
            );
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    Log.e("IO", "Cannot close input stream", ioe);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioe) {
                    Log.e("IO", "Cannot close output stream", ioe);
                }
            }
        }
    }

    /**
     * The method copies the input stream of the file to the output stream.
     *
     * @param in The input file stream.
     * @param out The output file stream.
     * @throws IOException
     */
    private void
    copyFile(InputStream in, OutputStream out)
            throws IOException
    {
        int read;
        byte[] buffer = new byte[COPY_BUFFER_SIZE];

        while((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /**
     * The method fills all fields in the PDF file.
     *
     * @throws IOException
     * @throws DocumentException
     */
    private void
    fillPdfFile()
            throws IOException, DocumentException
    {
        createFonts();

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

        divIntoPages();

        for (int i = 0; i < pages.length; i++) {
            document.newPage();
            createTitle(document, tableName);
            createTable(document, pages[i]);
        }
        document.close();
    }

    /**
     * The method fills all fields in the PDF file.
     *
     * Необольшое пояснение как работать с этой минибиблиотекой. В assets лежат
     * два файла: template.docx и table.xml. Первый -- это общий шаблон файла,
     * второй -- шаблон таблицы. Чтобы что-то поменять в шаблоне необходимо
     * распаковать template.docx любым архиватором, затем зайти в директорию
     * word и открыть документ document.xml, в нём найти джокер %s и заменить
     * его на содержимое файла table.xml. После этого запаковать все обратно и,
     * вуаля, можно менять шаблон, например форматирование или расположение
     * элементов. Также можно работать с подстановочными полями. Они обозначаются
     * следующим образом "$номер_поля%s", где номер поля соотвествует индексу
     * массива в выражении String.format(tableXML, (Object[])pageContent);
     * Нумерация полей идет с единицы, количество полей должно сопадать с
     * размерностью передаваемого массива, размер массива не ограничен.
     * Изменив шаблон, нужно проделать все операции в обратном порядке, т.е.
     * опять распаковать его, достать нужный файл, найти в нем тег tbl,
     * переместить его содержимое в файл table.xml, а вместо него вбить %s.
     * В завершении всего опять запаковать все в template.docx.
     */
    private void
    fillDocxFile()
    {
        String documentXML;
        String tableXML;
        InputStream is;

        String tables = EMPTY_STRING;
        String[] pageContent;
        ZipInputStream zis = null;
        
        copyDocxTemplateFromAssets(file);

        try {
            ZipFile zipFile = new ZipFile(file);
            zis = zipFile.getInputStream(
                    zipFile.getFileHeader(DOCX_DOCUMENT_PATH)
            );
            documentXML = IOUtils.toString(zis, CODING_UTF8);
            tableXML = getDocxTable();

            divIntoPages();

            for (int i = 0; i < pages.length; i++) {
                pageContent = ArrayUtils.addAll(cellHeaders, pages[i]);
                pageContent = ArrayUtils.add(pageContent, tableName);
                if (tableXML != null) {
                    tables += String.format(tableXML, (Object[])pageContent);
                } else {
                    throw new Exception();
                }
            }
            documentXML = String.format(documentXML, tables);

            zipFile.removeFile(DOCX_DOCUMENT_PATH);

            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setFileNameInZip(DOCX_DOCUMENT_PATH);
            parameters.setSourceExternalStream(true);

            is = IOUtils.toInputStream(documentXML);
            zipFile.addStream(is, parameters);
        } catch (ZipException ze) {
            Log.e("Zip", "An error occurred while working with zip file", ze);
        } catch (IOException ioe) {
            Log.e("IO", "General input/output error", ioe);
        } catch (Exception e) {
            Log.e("Unknown", "Unknown error", e);
        } finally {
            try {
                if (zis != null) {
                    zis.close();
                } else {
                    Log.e("Zip", "Zip stream was not open");
                }
            } catch (IOException e) {
                Log.e("Unknown", "Unknown error", e);
            }
        }
    }

    /**
     * Main method.
     */
    public void
    execute()
    {
        fileSaveDialog = new FileSaveDialog(
            context,
            new FileSaveDialog.FileDialogListener() {
                @Override
                public void
                onChosenDir(String chosenDir)
                {
                    /* The code in this function will be
                    executed when the dialog
                    OK button is pushed */
                    try {
                        file = new File(chosenDir);
                        genTask.execute();
                    } catch (Exception e) {
                        Log.e("Unknown", "Unknown error", e);
                    }
                }
            });
        fileSaveDialog.chooseFileOrDir();
    }
    /**
     * The method creates 'Open in' dialog.
     */
    private void
    viewPdfFile()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(
            String.format(
                context.getResources().getString(R.string.msg_file_written),
                file.toString()
            )
        );
        builder.setIcon(android.R.drawable.ic_dialog_info);

        builder.setPositiveButton(
                R.string.button_yes,
                new DialogInterface.OnClickListener()
        {
            public void
            onClick(DialogInterface dialog, int id)
            {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                switch (fileSaveDialog.fileType){
                    case FileSaveDialog.PDF_FILE_TYPE:
                        intent.setDataAndType(
                                Uri.fromFile(file),
                                APPLICATION_PDF
                        );
                        break;
                    case FileSaveDialog.DOCX_FILE_TYPE:
                        intent.setDataAndType(
                                Uri.fromFile(file),
                                APPLICATION_DOCX
                        );
                        break;
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                context.startActivity(intent);
            }
        });
        builder.setNegativeButton(
                R.string.button_no,
                new DialogInterface.OnClickListener()
        {
            public void
            onClick(DialogInterface dialog, int id)
            {
            }
        });
       builder.show();
    }

    /**
     * Threading class
     */
    private class GenTask
            extends AsyncTask<Void, Void, Void>
    {
        private ProgressDialog dialog;
        private Exception exceptionToBeThrown;

        @Override
        protected void
        onPreExecute()
        {
            super.onPreExecute();
            dialog = ProgressDialog.show(
                    context,
                    EMPTY_STRING,
                    context.getResources().getString(
                            R.string.msg_file_generating
                    ),
                    true
            );
            dialog.show();
        }

        @Override
        protected Void
        doInBackground(Void... params)
        {
            try {
                switch (fileSaveDialog.fileType){
                    case FileSaveDialog.PDF_FILE_TYPE:
                        fillPdfFile();
                        break;
                    case FileSaveDialog.DOCX_FILE_TYPE:
                        fillDocxFile();
                        break;
                }
            } catch (DocumentException de) {
                Log.e(
                        "Document",
                        "An error occurred while generating PDF document",
                        de
                );
                exceptionToBeThrown = de;
            } catch (FileNotFoundException fnfe) {
                exceptionToBeThrown = fnfe;
                Log.e(
                        "Storage",
                        String.format(
                                "Error writing %s", file
                        ), fnfe
                );
            } catch (IOException e) {
                exceptionToBeThrown = e;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void
        onPostExecute(Void result)
        {
            super.onPostExecute(result);
            dialog.dismiss();
            if (exceptionToBeThrown == null) {
                viewPdfFile();
            } else {
                Toast.makeText(
                        context,
                        context.getResources().getString(
                                R.string.err_generating_failure
                        ),
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
}