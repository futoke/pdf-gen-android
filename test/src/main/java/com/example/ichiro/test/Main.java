package com.example.ichiro.test;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main extends AppCompatActivity {

    private PdfTask pdfTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void createPdf(File file) throws FileNotFoundException, DocumentException {
        OutputStream fos = new FileOutputStream(file);
        Document doc = new Document();
        PdfWriter.getInstance(doc, fos);

        doc.open();

        doc.add(new Paragraph("Hello pdf!!"));

        doc.close();
    }

    /**
     * @param view
     */
    public void onMyButtonClick(View view)
    {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            //handle case of no SDCARD present
        } else {
            String dir = Environment.getExternalStorageDirectory() + File.separator + "pdfdemo";

            File folder = new File(dir);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(dir, "test.pdf");

            try {
                pdfTask = new PdfTask();
                pdfTask.execute(file);
            } catch (Exception e) {

            }
        }
        Toast.makeText(this, "Зачем вы нажали?", Toast.LENGTH_SHORT).show();
    }

    /**
     *
     */
    class PdfTask extends AsyncTask<File, Void, Void> {

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
                    createPdf(file);
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
