package com.example.ichiro.test;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

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
    private Context context;
    private FileDialog fileSaveDialog;
    private File file;

    public PdfGenerator (Context currentContext) {
        context = currentContext;
        pdfTask = new PdfTask();
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


    private void fillPdfFile(File file) throws FileNotFoundException, DocumentException {
        OutputStream fos = new FileOutputStream(file);
        Document doc = new Document();
        PdfWriter.getInstance(doc, fos);

        doc.open();
        doc.add(new Paragraph("Hello pdf lalalalala!!"));
        doc.close();
    }

    private void viewPdf() {
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
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
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
            viewPdf();
        }
    }

}
