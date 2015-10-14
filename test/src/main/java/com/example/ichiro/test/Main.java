package com.example.ichiro.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class Main extends AppCompatActivity {
    PdfGenerator pdfGenerator;

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


    /**
     * @param view
     */
    public void onMyButtonClick(View view)
    {
        final String[] m_chosen = new String[1];

        FileDialog fileSaveDialog =  new FileDialog (
            Main.this,
            DialogType.FILE_SAVE,
            new FileDialog.FileDialogListener() {
                @Override
                public void onChosenDir(String chosenDir) {
                    // The code in this function will be executed when the dialog OK button is pushed
                    m_chosen[0] = chosenDir;
                    Toast.makeText(
                        Main.this,
                        "Chosen FileOpenDialog File: " + m_chosen[0],
                        Toast.LENGTH_LONG
                    ).show();
                }
        });

        fileSaveDialog.chooseFileOrDir();

        pdfGenerator = new PdfGenerator();
        pdfGenerator.execute();
        Toast.makeText(this, "Зачем вы нажали?", Toast.LENGTH_SHORT).show();
    }
}

