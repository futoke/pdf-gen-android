package com.example.ichiro.test;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

enum DialogType {FILE_OPEN, FILE_SAVE, DIR_SELECT}

public class FileSaveDialog
{
    private DialogType dialogType;
    private Context context;
    private TextView dirNameView;
    private EditText inputFileName;
    private List<String> subDirs;
    private FileDialogListener fileDialogListener;
    private ArrayAdapter<String> listAdapter;

    private String fullFileName;
    private String sdDir;
    private String dir;
    private String buttonOk;
    private String buttonCancel;

    public Integer fileType;

    private final String[] fileTypes = {".pdf", ".docx"};
    private final String EMPTY_STRING = "";
    private final String PREVIOUS_LEVEL_DIRECTORY = "..";

    public static final int PDF_FILE_TYPE = 0;
    public static final int DOCX_FILE_TYPE = 1;

    /**
     * Callback interface for selected directory.
     */
    public interface FileDialogListener
    {
        void
        onChosenDir(String chosenDir);
    }

    /**
     * Main class
     *
     * @param currentContext            Activity context.
     * @param currentFileDialogListener Callback for file dialog.
     */
    public FileSaveDialog(
            Context currentContext,
            FileDialogListener currentFileDialogListener)
    {
        context = currentContext;
        fileDialogListener = currentFileDialogListener;
        dialogType = DialogType.FILE_SAVE;

        buttonOk = this.context.getResources().getString(R.string.button_ok);
        buttonCancel =
                this.context.getResources().getString(R.string.button_cancel);

        dir = EMPTY_STRING;
        subDirs = null;
        listAdapter = null;

        try {
            sdDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            sdDir = new File(sdDir).getCanonicalPath();
        } catch (IOException ioe) {
            Log.e("Storage", "External storage unavailable.", ioe);
        }
    }

    /**
     * The method loads a directory chooser dialog for an initial default
     * sdcard directory.
     */
    public void
    chooseFileOrDir()
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
     * @param editText The focused text field when virtual keyboard will be
     *                 hide.
     */
    private void
    hideKeyboard(EditText editText)
    {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE
        );
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    /**
     * @param editText The focused text field when virtual keyboard will be
     *                 show.
     */
    private void showKeyboard(EditText editText) {
        editText.setSelectAllOnFocus(true);
        editText.requestFocus();
    }

    /**
     * @param editText The text field with a new directory name.
     */
    private void
    createDir(EditText editText)
    {
        String newDirName = editText.getText().toString();
        // Create new directory.
        if (createSubDir(dir + File.separator + newDirName)) {
            // Navigate into the new directory.
            dir += File.separator + newDirName;
            updateDirectory();
        } else {
            Toast.makeText(
                    context,
                    String.format(
                            context.getResources().getString(
                                    R.string.err_failed_to_create_dir
                            ),
                            newDirName),
                    Toast.LENGTH_SHORT
            ).show();
        }
        hideKeyboard(editText);
    }

    /**
     * Current directory chosen.
     * Call registered listener supplied with the chosen directory.
     */
    private void
    createFile() {
        String fileName;

        if (fileDialogListener != null) {
            if (dialogType == DialogType.FILE_OPEN ||
                dialogType == DialogType.FILE_SAVE)
            {
                fileName = inputFileName.getText().toString();
                if (!fileName.equals("")) {
                    fullFileName = String.format(
                            "%s%s%s",
                            fileName,
                            fileTypes[fileType],
                            EMPTY_STRING
                    );

                    final String filePath = dir + File.separator + fullFileName;
                    if (!new File(filePath).exists()) {
                        fileDialogListener.onChosenDir(filePath);
                    } else {
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(context);
                        builder.setMessage(
                                String.format(
                                        context.getResources().getString(
                                                R.string.msg_file_exists
                                        ),
                                        fullFileName
                                )
                        );
                        builder.setIcon(android.R.drawable.ic_dialog_info);

                        builder.setPositiveButton(
                                R.string.button_ok,
                                new DialogInterface.OnClickListener()
                        {
                            public void
                            onClick(DialogInterface dialog, int id)
                            {
                                fileDialogListener.onChosenDir(filePath);
                            }
                        });
                        builder.setNegativeButton(
                                R.string.button_cancel,
                                new DialogInterface.OnClickListener()
                        {
                            public void
                            onClick(DialogInterface dialog, int id)
                            {
                                chooseFileOrDir();
                                inputFileName.setText(EMPTY_STRING);
                            }
                        });
                        builder.show();
                    }

                } else {
                    Toast.makeText(
                            context,
                            context.getResources().getString(
                                    R.string.err_file_name_is_empty
                            ),
                            Toast.LENGTH_SHORT
                    ).show();
                    chooseFileOrDir();
                    inputFileName.setText(EMPTY_STRING);
                    showKeyboard(inputFileName);
                }
            } else {
                fileDialogListener.onChosenDir(dir);
            }
        }
    }

    /**
     * The method loads a directory chooser dialog for an initial input 'dir'
     * directory.
     *
     * @param currentDir Absolute path to the current directory.
     */
    public void
    chooseFileOrDir(String currentDir)
    {
        File dirFile = new File(currentDir);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            currentDir = sdDir;
        }
        try {
            currentDir = new File(currentDir).getCanonicalPath();
        } catch (IOException ioe) {
            Log.e(
                    "Storage",
                    String.format("The directory %s unavailable.", currentDir),
                    ioe
            );
        }

        dir = currentDir;
        subDirs = getDirectories(currentDir);

        class SimpleFileDialogOnClickListener
                implements DialogInterface.OnClickListener
        {
            public void
            onClick(DialogInterface dialog, int item)
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

                fullFileName = EMPTY_STRING;

                // If the selection is a regular file.
                if ((new File(dir).isFile())) {
                    dir = dirOld;
                }
                updateDirectory();
            }
        }
        AlertDialog.Builder builder = createDirectoryChooserDialog(
                currentDir,
                subDirs,
                new SimpleFileDialogOnClickListener()
        );
        showKeyboard(inputFileName);

        builder.setPositiveButton(
                buttonOk, new OnClickListener()
        {
            @Override
            public void
            onClick(DialogInterface dialog, int which)
            {
                createFile();
                hideKeyboard(inputFileName);
            }
        });
        builder.setNegativeButton(
                buttonCancel,
                new OnClickListener()
        {
            @Override
            public void
            onClick(DialogInterface dialog, int which)
            {
                hideKeyboard(inputFileName);
            }
        });
        final AlertDialog dirsDialog = builder.create();
        dirsDialog.show();

        inputFileName.setOnEditorActionListener(
                new TextView.OnEditorActionListener()
        {
            @Override
            public boolean
            onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    createFile();
                    hideKeyboard(inputFileName);
                    dirsDialog.dismiss();
                }
                return false;
            }
        });
    }

    /**
     * The method for creation subdirectories in the certain directory.
     * @param newDir The new directory name.
     * @return File object of the new directory.
     */
    private boolean
    createSubDir(String newDir)
    {
        File newDirFile = new File(newDir);
        return !newDirFile.exists() && newDirFile.mkdir();
    }

    /**
     * The method creates a list of the all file objects in the certain
     * directory.
     *
     * @param dir The listing directory.
     * @return The list of the directories.
     */
    private List<String>
    getDirectories(String dir)
    {
        List<String> dirs = new ArrayList<>();
        try {
            File dirFile = new File(dir);

            // If directory is not the base sdcard directory add ".." for going
            // up one directory.
            if (!this.dir.equals(sdDir)) {
                dirs.add(PREVIOUS_LEVEL_DIRECTORY);
            }
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return dirs;
            }
            for (File file : dirFile.listFiles()) {
                if ( file.isDirectory()) {
                    /*
                    Add File.separator to directory names to identify them in
                    the list.
                    */
                    dirs.add( file.getName() + File.separator );
                } else if (
                        dialogType == DialogType.FILE_OPEN ||
                        dialogType == DialogType.FILE_SAVE) {
                    /*
                    Add file names to the list if we are doing a file save or
                    file open operation.
                    */
                    dirs.add(file.getName() );
                }
            }
        }
        catch (Exception e)	{
            Log.e(
                    "Directories",
                    "An error occurred while creation the list of the directories",
                    e
            );
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
    @SuppressWarnings("deprecation")
    private AlertDialog.Builder
    createDirectoryChooserDialog(
            String title,
            List<String> listItems,
            DialogInterface.OnClickListener onClickListener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Create title text showing file select type.
        TextView dialogTitleView = new TextView(context);

        dialogTitleView.setLayoutParams(
                new LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT
                )
        );
        switch (dialogType) {
            case FILE_OPEN:
                dialogTitleView.setText(
                        context.getResources().getString(R.string.file_open)
                );
                break;
            case FILE_SAVE:
                dialogTitleView.setText(
                        context.getResources().getString(R.string.file_save)
                );
                break;
            case DIR_SELECT:
                dialogTitleView.setText(
                        context.getResources().getString(R.string.dir_select)
                );
                break;
            default:
                dialogTitleView.setText(
                        context.getResources().getString(R.string.file_save)
                );
                break;
        }

        // Need to make this a variable Save as, Open, Select Directory.
        dialogTitleView.setGravity(
                Gravity.CENTER_VERTICAL |
                Gravity.CENTER_HORIZONTAL
        );
        dialogTitleView.setBackgroundResource(R.color.dark_grey);
        // The method getColor is deprecated but I need to use it for
        // compatibility purposes.
        //noinspection deprecation
        dialogTitleView.setTextColor(
                context.getResources().getColor(android.R.color.white)
        );
        // Create custom view for AlertDialog title.
        LinearLayout titleLayout = new LinearLayout(context);
        titleLayout.setOrientation(LinearLayout.VERTICAL);
        titleLayout.addView(dialogTitleView);

        if (dialogType == DialogType.DIR_SELECT ||
            dialogType == DialogType.FILE_SAVE)
        {
            // Create New Folder Button.
            Button newDirButton = new Button(context);
            newDirButton.setLayoutParams(
                    new LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT
                    )
            );
            newDirButton.setText(
                    context.getResources().getString(R.string.new_directory)
            );
            newDirButton.setOnClickListener(
                    new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    final EditText inputDirName = new EditText(context);

                    inputDirName.setInputType(
                            InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
                    );
                    // Force show keyboard.
                    showKeyboard(inputDirName);
                    InputMethodManager imm = (InputMethodManager)
                            context.getSystemService(
                                    Context.INPUT_METHOD_SERVICE
                            );
                    imm.toggleSoftInput(
                            InputMethodManager.SHOW_FORCED,
                            InputMethodManager.HIDE_IMPLICIT_ONLY
                    );

                    final AlertDialog.Builder builder =
                            new AlertDialog.Builder(context);
                    builder.setTitle(
                            context.getResources().getString(
                                    R.string.new_directory_name
                            )
                    );
                    builder.setView(inputDirName);

                    builder.setPositiveButton(
                            buttonOk,
                            new DialogInterface.OnClickListener()
                    {
                        public void
                        onClick(DialogInterface dialog, int whichButton)
                        {
                            createDir(inputDirName);
                            showKeyboard(inputFileName);
                        }
                    });
                    builder.setNegativeButton(
                            buttonCancel,
                            new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void
                        onClick(DialogInterface dialog, int which)
                        {
                            hideKeyboard(inputDirName);
                        }
                    });
                    final AlertDialog newDirDialog = builder.create();
                    newDirDialog.show();

                    inputDirName.setOnEditorActionListener(
                            new TextView.OnEditorActionListener()
                    {
                        @Override
                        public boolean
                        onEditorAction(TextView v, int actionId, KeyEvent event)
                        {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                createDir(inputDirName);
                                newDirDialog.dismiss();
                                showKeyboard(inputFileName);
                            }
                            return false;
                        }
                    });
                }
            });
            titleLayout.addView(newDirButton);
        }

        // Create View with folder path and entry text box and file type
        // extension.
        LinearLayout createFilePathLayout = new LinearLayout(context);
        createFilePathLayout.setOrientation(LinearLayout.VERTICAL);

        dirNameView = new TextView(context);
        dirNameView.setLayoutParams(
                new LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT
                )
        );
        dirNameView.setBackgroundResource(R.color.dark_grey);
        // The method getColor is deprecated but I need to use it for
        // compatibility purposes.
        dirNameView.setTextColor(
                context.getResources().getColor(android.R.color.white)
        );
        dirNameView.setGravity(
                Gravity.CENTER_VERTICAL |
                Gravity.CENTER_HORIZONTAL
        );
        dirNameView.setText(title);

        createFilePathLayout.addView(dirNameView);

        if (dialogType == DialogType.FILE_OPEN ||
            dialogType == DialogType.FILE_SAVE)
        {
            // Create input for the file name.
            inputFileName = new EditText(context);
            inputFileName.setLayoutParams(
                    new TableLayout.LayoutParams(
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.MATCH_PARENT,
                            0.9f
                    )
            );
            inputFileName.setInputType(
                    InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
            );
            inputFileName.setText(createFileName());

            // Create spinner for the file extension/
            final Spinner fileTypeSpinner = new Spinner(context);
            fileTypeSpinner.setLayoutParams(
                    new LayoutParams(
                            LayoutParams.WRAP_CONTENT, // Width.
                            LayoutParams.WRAP_CONTENT  // Height.
                    )
            );
            fileTypeSpinner.setGravity(Gravity.END);
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                    context,
                    android.R.layout.simple_spinner_dropdown_item,
                    fileTypes
            );
            spinnerArrayAdapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
            );
            fileTypeSpinner.setAdapter(spinnerArrayAdapter);
            fileTypeSpinner.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void
                onItemSelected(
                        AdapterView<?> parent,
                        View view,
                        int position,
                        long id)
                {
                    fileType = position;
                }
                @Override
                public void
                onNothingSelected(AdapterView<?> arg0)
                {
                    fileType = PDF_FILE_TYPE;
                }
            });

            // Create nested layout for input and spinner.
            LinearLayout createFilePathLayout1 = new LinearLayout(context);
            createFilePathLayout1.setOrientation(LinearLayout.HORIZONTAL);

            createFilePathLayout1.addView(inputFileName);
            createFilePathLayout1.addView(fileTypeSpinner);

            // Add to parent layout.
            createFilePathLayout.addView(createFilePathLayout1);
        }

        // Set Views and Finish Dialog builder.
        builder.setView(createFilePathLayout);
        builder.setCustomTitle(titleLayout);
        listAdapter = createListAdapter(listItems);
        builder.setSingleChoiceItems(listAdapter, -1, onClickListener);
        builder.setCancelable(false);
        return builder;
    }

    /**
     * Update content of the directory view.
     */
    private void
    updateDirectory() {
        subDirs.clear();
        subDirs.addAll(getDirectories(dir));
        dirNameView.setText(dir);
        listAdapter.notifyDataSetChanged();

//        if (dialogType == DialogType.FILE_OPEN ||
//            dialogType == DialogType.FILE_SAVE)
//        {
//            inputFileName.setText(createFileName());
//        }
    }

    /**
     * Create adapter for the list of the file objects.
     *
     * @param items The items to add.
     * @return List of the added items.
     */
    private ArrayAdapter<String>
    createListAdapter(List<String> items)
    {
        return new ArrayAdapter<String>(
                context,
                android.R.layout.select_dialog_item,
                android.R.id.text1,
                items)
        {
            @Override
            public View
            getView(int position, View convertView, ViewGroup parent)
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
     * Create default name for the files based on current date and time.
     *
     * @return The file name.
     */
    @SuppressLint("SimpleDateFormat")
    private String
    createFileName()
    {
        Date date;
        String currentDateTime;
        String defaultPdfName;
        String dateTimeFormat;

        date = new Date();
        dateTimeFormat =
                this.context.getResources().getString(R.string.date_time_format);
        currentDateTime = new SimpleDateFormat(dateTimeFormat).format(date);
        defaultPdfName =
                this.context.getResources().getString(R.string.default_pdf_name);

        return String.format("%s_%s", defaultPdfName, currentDateTime);
    }
} 