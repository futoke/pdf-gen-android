package com.example.ichiro.test;

import android.os.Bundle;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import android.view.View.OnClickListener;
import android.widget.Button;


public class
        MainActivity
        extends AppCompatActivity
        implements OnClickListener
{
    public String tableName;
    public String[] cellHeaders;
    public String[] cellContent;

    @Override
    protected void
    onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button but1 = (Button)findViewById(R.id.button1);
        Button but2 = (Button)findViewById(R.id.button2);
        Button but3 = (Button)findViewById(R.id.button3);

        but1.setOnClickListener(this);
        but2.setOnClickListener(this);
        but3.setOnClickListener(this);
    }

    @Override
    public boolean
    onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean
    onOptionsItemSelected(MenuItem item)
    {
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

    protected void
    doExport(Context context,
             String tableName,
             String[] cellContent,
             String[] cellHeaders)
    {
        ReportGenerator reportGenerator;
        reportGenerator = new ReportGenerator(
                context,
                tableName,
                cellContent,
                cellHeaders
        );
        reportGenerator.execute();
    }

    @Override
    public void
    onClick(View v)
    {
        switch (v.getId()) {
            case R.id.button1: // Russian text
                Toast.makeText(
                        getApplicationContext(),
                        "Russian PDF export started",
                        Toast.LENGTH_SHORT
                ).show();
                tableName = "Название таблицы Фон 4";
                // massive of Cell headers in Russian
                cellHeaders = new String[] {
                        "Заголовок 1 Фон 1",
                        "Заголовок 3 Фон 1",
                        "Заголовок 4 Фон 1",
                        "Заголовок 5 Фон 1",
                        "Заголовок 6 Фон 1",
                        "Заголовок 2 Фон 1",
                        "Заголовок 8 Фон 1",
                        "Заголовок 8_1 Фон 3",
                        "Заголовок 8_2 Фон 3",
                        "Заголовок 9 Фон 1",
                        "Заголовок 7 Фон 1",
                        "Заголовок 10 Фон 1",
                        "Заголовок 11 Фон 1"
                };
                cellContent = new String[] {
                        getResources().getString(R.string.Text1Font2Ru),
                        getResources().getString(R.string.Text3Font2Ru),
                        getResources().getString(R.string.Text4Font2Ru),
                        getResources().getString(R.string.Text5Font2Ru),
                        getResources().getString(R.string.Text6Font2Ru),
                        getResources().getString(R.string.Text2Font2Ru),
                        getResources().getString(R.string.Text8_1Font2Ru),
                        getResources().getString(R.string.Text8_2Font2Ru),
                        getResources().getString(R.string.Text9Font2Ru),
                        getResources().getString(R.string.Text7Font2Ru),
                        getResources().getString(R.string.Text10Font2Ru),
                        getResources().getString(R.string.Text11Font2Ru)
                };

                //*** PUT YOUR PDF export method for Russian text
                doExport(MainActivity.this, tableName, cellContent, cellHeaders);

                break;
            case R.id.button2: // English text
                Toast.makeText(
                        getApplicationContext(),
                        "English PDF export started",
                        Toast.LENGTH_SHORT
                ).show();
                tableName = "Table name Font 4";
                // massive of Cell headers in English
                cellHeaders = new String[] {
                        "Header 1 Font 1",
                        "Header 3 Font 1",
                        "Header 4 Font 1",
                        "Header 5 Font 1",
                        "Header 6 Font 1",
                        "Header 2 Font 1",
                        "Header 8 Font 1",
                        "Header 8_1 Font 3",
                        "Header 8_2 Font 3",
                        "Header 9 Font 1",
                        "Header 7 Font 1",
                        "Header 10 Font 1",
                        "Header 11 Font 1"
                };
                cellContent = new String[] {
                        getResources().getString(R.string.Text1Font2En),
                        getResources().getString(R.string.Text3Font2En),
                        getResources().getString(R.string.Text4Font2En),
                        getResources().getString(R.string.Text5Font2En),
                        getResources().getString(R.string.Text6Font2En),
                        getResources().getString(R.string.Text2Font2En),
                        getResources().getString(R.string.Text8_1Font2En),
                        getResources().getString(R.string.Text8_2Font2En),
                        getResources().getString(R.string.Text9Font2En),
                        getResources().getString(R.string.Text7Font2En),
                        getResources().getString(R.string.Text10Font2En),
                        getResources().getString(R.string.Text11Font2En)
                };
                //*** PUT YOUR PDF export method for English text
                doExport(MainActivity.this, tableName, cellContent, cellHeaders);
                break;

            case R.id.button3: //
                Toast.makeText(
                        getApplicationContext(),
                        "Chinese PDF export started",
                        Toast.LENGTH_SHORT
                ).show();
                tableName = "表名称字体 4";
                // The array of the cell headers in Chinese.
                cellHeaders = new String[] {"标题 1 字体 1",
                        "标题 3 字体 1",
                        "标题 4 字体 1",
                        "标题 5 字体 1",
                        "标题 6 字体 1",
                        "标题 2 字体 1",
                        "标题 8 字体 1",
                        "标题 8_1 字体 3",
                        "标题 8_2 字体 3",
                        "标题 9 字体 1",
                        "标题 7 字体 1",
                        "标题 10 字体 1",
                        "标题 11 字体 1"
                };
                cellContent = new String[] {
                        getResources().getString(R.string.Text1Font2Cn),
                        getResources().getString(R.string.Text3Font2Cn),
                        getResources().getString(R.string.Text4Font2Cn),
                        getResources().getString(R.string.Text5Font2Cn),
                        getResources().getString(R.string.Text6Font2Cn),
                        getResources().getString(R.string.Text2Font2Cn),
                        getResources().getString(R.string.Text8_1Font2Cn),
                        getResources().getString(R.string.Text8_2Font2Cn),
                        getResources().getString(R.string.Text9Font2Cn),
                        getResources().getString(R.string.Text7Font2Cn),
                        getResources().getString(R.string.Text10Font2Cn),
                        getResources().getString(R.string.Text11Font2Cn)
                };
                //***PUT YOUR PDF export method for Chinese text
                doExport(MainActivity.this, tableName, cellContent, cellHeaders);
                break;
        }
    }
}

