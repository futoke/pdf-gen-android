package com.example.ichiro.test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FileDialog
{

    private DialogType dialogType;

    private Context context;
    private TextView auxView;
    private EditText inputTextField;

    private List<String> subdirs;
    private FileDialogListener fileDialogListener;
    private ArrayAdapter<String> listAdapter;

    private String fileName;
    private String sdDir;
    private String dir;
    private String buttonOk;
    private String buttonCancel;

    private final String EMPTY_STRING = "";
    private final String PREVIOUS_LEVEL_DIRECTORY = "..";

    /**
     * Callback interface for selected directory.
     */
    public interface FileDialogListener
    {
        void onChosenDir(String chosenDir);
    }

    /**
     * Main class
     *
     * @param currentContext
     * @param currentDialogType
     * @param currentFileDialogListener
     */
    public FileDialog(
            Context currentContext,
            DialogType currentDialogType,
            FileDialogListener currentFileDialogListener)
    {
        context = currentContext;
        fileDialogListener = currentFileDialogListener;
        dialogType = currentDialogType;

        buttonOk = this.context.getResources().getString(R.string.button_ok);
        buttonCancel = this.context.getResources().getString(R.string.button_cancel);

        dir = EMPTY_STRING;
        subdirs = null;
        listAdapter = null;

        try {
            sdDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            sdDir = new File(sdDir).getCanonicalPath();
        } catch (IOException ioe) {
            // TODO: Add handler!
        }
    }

    /**
     * The method loads a directory chooser dialog for an initial default sdcard directory.
     */
    public void chooseFileOrDir()
    {
        // Initial directory is sdcard directory.
        if (dir.equals(EMPTY_STRING)) {
            chooseFileOrDir(sdDir);
        }
        else {
            chooseFileOrDir(dir);
        }
    }

    /**
     * The method loads a directory chooser dialog for an initial input 'dir' directory.
     *
     * @param currentDir Absolute path to the current directory.
     */
    public void chooseFileOrDir(String currentDir)
    {
        File dirFile = new File(currentDir);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            currentDir = sdDir;
        }

        try {
            currentDir = new File(currentDir).getCanonicalPath();
        } catch (IOException ioe) {
            return; // TODO: Add handler!
        }

        dir = currentDir;
        subdirs = getDirectories(currentDir);

        /**
         *
         */
        class SimpleFileDialogOnClickListener implements DialogInterface.OnClickListener
        {
            /**
             * @param dialog
             * @param item
             */
            public void onClick(DialogInterface dialog, int item)
            {
                String dirOld = dir;
                String sel = String.format(
                        "%s%s",
                        EMPTY_STRING,
                        ((AlertDialog) dialog).getListView().getAdapter().getItem(item)
                );
                if (sel.charAt(sel.length() - 1) == File.separatorChar) {
                    sel = sel.substring(0, sel.length() - 1);
                }
                // Navigate into the sub-directory.
                if (sel.equals(PREVIOUS_LEVEL_DIRECTORY)) {
                    dir = dir.substring(0, dir.lastIndexOf(File.separator));
                } else {
                    dir += File.separator + sel;
                }
                fileName = createFileName();

                // If the selection is a regular file.
                if ((new File(dir).isFile())) {
                    dir = dirOld;
                    // If you uncomment this line, you can overwrite files.
                    // fileName = sel;
                    // TODO: implement duplicate checking!!!
                }

                updateDirectory();
            }
        }

        AlertDialog.Builder dialogBuilder = createDirectoryChooserDialog(
                currentDir,
                subdirs,
                new SimpleFileDialogOnClickListener()
        );

        dialogBuilder.setPositiveButton(buttonOk, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Current directory chosen.
                // Call registered listener supplied with the chosen directory.
                if (fileDialogListener != null) {
                    if (dialogType == DialogType.FILE_OPEN || dialogType == DialogType.FILE_SAVE) {
                        fileName = inputTextField.getText() + EMPTY_STRING;
                        fileDialogListener.onChosenDir(dir + File.separator + fileName);
                    } else {
                        fileDialogListener.onChosenDir(dir);
                    }
                }
            }
        });
        dialogBuilder.setNegativeButton(buttonCancel, null);
        final AlertDialog dirsDialog = dialogBuilder.create();

        // Show directory chooser dialog.
        dirsDialog.show();
    }

    private boolean createSubDir(String newDir)
    {
        File newDirFile = new File(newDir);
        return !newDirFile.exists() && newDirFile.mkdir();
    }

    private List<String> getDirectories(String dir)
    {
        List<String> dirs = new ArrayList<>();
        try {
            File dirFile = new File(dir);

            // If directory is not the base sdcard directory add ".." for going up one directory.
            if (!this.dir.equals(sdDir)) {
                dirs.add(PREVIOUS_LEVEL_DIRECTORY);
            }
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return dirs;
            }
            for (File file : dirFile.listFiles()) {
                if ( file.isDirectory()) {
                    // Add File.separator to directory names to identify them in the list.
                    dirs.add( file.getName() + File.separator );
                } else if (
                        dialogType == DialogType.FILE_OPEN ||
                        dialogType == DialogType.FILE_SAVE) {
                    // Add file names to the list if we are doing a file save or file open operation.
                    dirs.add( file.getName() );
                }
            }
        }
        catch (Exception e)	{
            // TODO: Add handler!
        }

        Collections.sort(dirs, new Comparator<String>()
        {
            public int compare(String o1, String o2)
            {
                return o1.compareTo(o2);
            }
        });
        return dirs;
    }

    // Start dialog definition.
    private AlertDialog.Builder createDirectoryChooserDialog(
            String title,
            List<String> listItems,
            DialogInterface.OnClickListener onClickListener)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        // Create title text showing file select type.
        TextView mainView = new TextView(context);

        mainView.setLayoutParams(
                new LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT
                )
        );
        switch (dialogType) {
            case FILE_OPEN:
                mainView.setText(context.getResources().getString(R.string.file_open));
                break;
            case FILE_SAVE:
                mainView.setText(context.getResources().getString(R.string.file_save));
                break;
            case DIR_SELECT:
                mainView.setText(context.getResources().getString(R.string.dir_select));
                break;
            default:
                mainView.setText(context.getResources().getString(R.string.file_save));
                break;
        }

        // Need to make this a variable Save as, Open, Select Directory.
        mainView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        mainView.setBackgroundResource(R.color.dark_grey);
        // The method getColor is deprecated but I need to use it for compatibility purposes.
        mainView.setTextColor(context.getResources().getColor(android.R.color.white));

        // Create custom view for AlertDialog title.
        LinearLayout titleLayout1 = new LinearLayout(context);
        titleLayout1.setOrientation(LinearLayout.VERTICAL);
        titleLayout1.addView(mainView);

        if (dialogType == DialogType.DIR_SELECT || dialogType == DialogType.FILE_SAVE) {


            // Create New Folder Button.
            Button newDirButton = new Button(context);
            newDirButton.setLayoutParams(
                    new LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT
                    )
            );

            newDirButton.setText(context.getResources().getString(R.string.new_directory));
            newDirButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final EditText input = new EditText(context);
                    // Force show keyboard.
                    input.requestFocus();
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

                    // Show new folder name input dialog
                    new AlertDialog.Builder(context).
                            setTitle(context.getResources().getString(R.string.new_directory_name)).
                            setView(input).setPositiveButton(buttonOk, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Editable newDir = input.getText();
                            String newDirName = newDir.toString();
                            // Create new directory.
                            if (createSubDir(dir + File.separator + newDirName)) {
                                // Navigate into the new directory.
                                dir += File.separator + newDirName;
                                updateDirectory();
                            } else {
                                Toast.makeText(
                                        context,
                                        String.format(
                                                context.getResources().getString(R.string.failed_to_create_dir),
                                                newDirName),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                            // Force hide keyboard.
                            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        }
                    }).setNegativeButton(buttonCancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Force hide keyboard.
                            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        }
                    }).show();
                }
            });
            titleLayout1.addView(newDirButton);
        }

        // Create View with folder path and entry text box.
        LinearLayout titleLayout = new LinearLayout(context);
        titleLayout.setOrientation(LinearLayout.VERTICAL);

        auxView = new TextView(context);
        auxView.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT)
        );
        auxView.setBackgroundResource(R.color.dark_grey);
        // The method getColor is deprecated but I need to use it for compatibility purposes.
        auxView.setTextColor(context.getResources().getColor(android.R.color.white));
        auxView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        auxView.setText(title);

        titleLayout.addView(auxView);

        if (dialogType == DialogType.FILE_OPEN || dialogType == DialogType.FILE_SAVE) {
            inputTextField = new EditText(context);
            inputTextField.setText(createFileName());
            titleLayout.addView(inputTextField);
        }

        // Set Views and Finish Dialog builder.
        dialogBuilder.setView(titleLayout);
        dialogBuilder.setCustomTitle(titleLayout1);
        listAdapter = createListAdapter(listItems);
        dialogBuilder.setSingleChoiceItems(listAdapter, -1, onClickListener);
        dialogBuilder.setCancelable(false);
        return dialogBuilder;
    }

    /**
     *
     */
    private void updateDirectory()
    {
        subdirs.clear();
        subdirs.addAll(getDirectories(dir));
        auxView.setText(dir);
        listAdapter.notifyDataSetChanged();

        if (dialogType == DialogType.FILE_OPEN || dialogType == DialogType.FILE_SAVE) {
            inputTextField.setText(createFileName());
        }
    }

    /**
     * @param items
     * @return
     */
    private ArrayAdapter<String> createListAdapter(List<String> items)
    {
        return new ArrayAdapter<String>(context, android.R.layout.select_dialog_item, android.R.id.text1, items)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) {
                    // Enable list item (directory) text wrapping.
                    TextView tv = (TextView) v;
                    tv.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
                    tv.setEllipsize(null);
                }
                return v;
            }
        };
    }

    /**
     * @return
     */
    private String createFileName()
    {
        Date date;
        String currentDateTime;
        String defaultPdfName;
        String dateTimeFormat;

        date = new Date();
        dateTimeFormat = this.context.getResources().getString(R.string.date_time_format);
        currentDateTime = new SimpleDateFormat(dateTimeFormat).format(date);
        defaultPdfName = this.context.getResources().getString(R.string.default_pdf_name);

        return String.format("%s_%s.pdf", defaultPdfName, currentDateTime);
    }
} 