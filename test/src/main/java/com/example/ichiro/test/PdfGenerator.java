package com.example.ichiro.test;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class PdfGenerator {
    private PdfTask pdfTask;

    public void execute() {
        File file;
        file = createPdfFile(); // TODO: Add if statement here!
        if (file != null) {
            try {
                pdfTask = new PdfTask();
                pdfTask.execute(file);
            } catch (Exception e) {

            }
        } else {
            // TODO: Add debug and change return statement!
        }

    }

    private File createPdfFile() {

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return null;
        } else {
            String dir = Environment.getExternalStorageDirectory() + File.separator + "pdfdemo";

            File folder = new File(dir);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            return new File(dir, "test.pdf");
        }
    }


    private void fillPdfFile(File file) throws FileNotFoundException, DocumentException {
        OutputStream fos = new FileOutputStream(file);
        Document doc = new Document();
        PdfWriter.getInstance(doc, fos);

        doc.open();
        doc.add(new Paragraph("Hello pdf lalalalala!!"));
        doc.close();
    }

    /**
     *
     */
    private class PdfTask extends AsyncTask<File, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * @param files
         * @return
         */
        @Override
        protected Void doInBackground(File... files) {
            File file = files[0];

            try {
                int filesCounter = files.length;
                if (filesCounter == 1) {
                    fillPdfFile(file);
                } else {

                }
            } catch (DocumentException de) {

            } catch (FileNotFoundException fnfe) {
                Log.w("ExternalStorage", String.format("Error writing %s", file), fnfe);
            }
            return null;
        }

        /**
         * @param result
         */
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

}
