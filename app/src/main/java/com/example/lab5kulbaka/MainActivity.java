package com.example.lab5kulbaka;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    int mId;
    TextInputEditText mTextInputEditText;
    Button mLookBtn;
    Button mDeleteBtn;
    Button mDownloadBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextInputEditText = (TextInputEditText) findViewById(R.id.input);
        mLookBtn = (Button) findViewById(R.id.look);
        mDeleteBtn = (Button) findViewById(R.id.delete);
        mDownloadBtn = (Button) findViewById(R.id.download);
        if (ContextCompat.checkSelfPermission(this,
                                              Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                                                                     Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                                                  new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                  1);
            }
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        SharedPreferences settings = getSharedPreferences("JOURNALS_APP", Context.MODE_PRIVATE);
        if (!settings.contains("NEED_MESSAGE") || settings.getBoolean("NEED_MESSAGE", true)) {
            PopupClass popUpClass = new PopupClass();
            popUpClass.showPopupWindow(findViewById(R.id.root));
        }
    }

    public void onLoadClick(View view) {
        if (mTextInputEditText.getText() != null && !mTextInputEditText.getText().toString().isEmpty()) {
            try {
                mId = Integer.parseInt(mTextInputEditText.getText().toString());
                new RequestTask(this).execute();
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onShowClick(View view) {
        File file = new File(Environment
                                     .getExternalStorageDirectory()
                                     + "/journals/" + mId + ".pdf");
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setDataAndType(Uri.fromFile(file), "application/pdf");
        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        Intent intent = Intent.createChooser(target, "Open File");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getText(R.string.need_pdf_reader), Toast.LENGTH_LONG).show();
        }
    }

    public void onDeleteClick(View view) {
        File fdelete = new File(Environment
                                        .getExternalStorageDirectory()
                                        + "/journals/" + mId + ".pdf");
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Toast.makeText(this, getText(R.string.file_delete), Toast.LENGTH_LONG).show();
            }
        }
        mLookBtn.setEnabled(false);
        mDeleteBtn.setEnabled(false);
    }

    class RequestTask extends AsyncTask<String, String, String> {

        Activity mContext;

        public RequestTask(Activity context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(mContext, getString(R.string.start_load), Toast.LENGTH_LONG).show();
            mDeleteBtn.setEnabled(false);
            mLookBtn.setEnabled(false);
            mDownloadBtn.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... uri) {
            try {
                URL url = new URL("https://ntv.ifmo.ru/file/journal/" + mId + ".pdf");
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.connect();
                File folder = new File(Environment.getExternalStorageDirectory(), "/journals");
                folder.mkdir();
                File pdfFile = new File(folder, mId + ".pdf");
                try {
                    pdfFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                OutputStream output = new FileOutputStream(pdfFile);
                InputStream input = new BufferedInputStream(url.openStream(),
                                                            8192);
                byte[] data = new byte[1024];
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
                connection.disconnect();
                return "Success";
            } catch (FileNotFoundException e) {
                return "No File!";
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Error";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mDownloadBtn.setEnabled(true);
            if (result.equals("Success")) {
                Toast.makeText(mContext, getString(R.string.finish_load), Toast.LENGTH_LONG).show();
                mLookBtn.setEnabled(true);
                mDeleteBtn.setEnabled(true);
            } else if (result.equals("No File!")) {
                Toast.makeText(mContext, getString(R.string.no_file), Toast.LENGTH_LONG).show();
            }
        }
    }

}