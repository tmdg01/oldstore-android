package ru.bulbad0za.oldstore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Environment;
import android.app.ProgressDialog;
import android.preference.PreferenceManager;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppDetailActivity extends Activity {
    private ImageView appIcon;
    private TextView appName, appShortDescription, appDescription, appVersion, appMinAndroid;
    private TextView downloadButton;
    private String serverUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);

        appIcon = (ImageView) findViewById(R.id.app_icon);
        appName = (TextView) findViewById(R.id.app_name);
        appShortDescription = (TextView) findViewById(R.id.app_short_description);
        appDescription = (TextView) findViewById(R.id.app_description);
        appVersion = (TextView) findViewById(R.id.app_version);
        appMinAndroid = (TextView) findViewById(R.id.app_min_android);
        downloadButton = (TextView) findViewById(R.id.download_button);

        SharedPreferences preferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        serverUrl = preferences.getString("server_url", "http://old-apps.xyz");

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("app")) {
            final App app = (App) intent.getSerializableExtra("app");

            if (app != null) {
                appName.setText(app.getName());
                appShortDescription.setText(app.getShortDescription());
                appDescription.setText(app.getDescription());
                appVersion.setText("Версия: " + app.getApplicationVersion());
                appMinAndroid.setText("Мин. Android: " + app.getMinAndroidVersion());

                new ImageLoader.LoadImageTask(appIcon).execute(serverUrl + "/images/" + app.getIcon());

                downloadButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String downloadUrl = serverUrl + "/apks/" + app.getFile();
                        downloadAndInstallApp(downloadUrl);
                    }
                });
            }
        }
    }

    private void downloadAndInstallApp(final String downloadUrl) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Загрузка приложения...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.show();

        new AsyncTask<Void, Long, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(downloadUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    long fileLength = connection.getContentLength();

                    String fileName = "app.apk";
                    File outputFile;

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppDetailActivity.this);
                    String downloadLocation = prefs.getString("download_location", "external");

                    if (downloadLocation.equals("external") && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        File externalDir = Environment.getExternalStorageDirectory();
                        outputFile = new File(externalDir, fileName);
                    } else {
                        outputFile = new File(getFilesDir(), fileName);
                    }

                    FileOutputStream fos = new FileOutputStream(outputFile);
                    InputStream is = connection.getInputStream();

                    byte[] buffer = new byte[4096];
                    long total = 0;
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        total += len;
                        publishProgress(total, fileLength);
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    is.close();

                    return outputFile.getAbsolutePath();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onProgressUpdate(Long... values) {
                long total = values[0];
                long fileLength = values[1];
                int progress = (int) (total * 100 / fileLength);
                progressDialog.setProgress(progress);
                String message = String.format("Загружено %.2f МБ из %.2f МБ", total / 1048576.0, fileLength / 1048576.0);
                progressDialog.setMessage(message);
            }

            @Override
            protected void onPostExecute(final String filePath) {
                progressDialog.dismiss();
                if (filePath != null) {
                    installApp(filePath);
                } else {
                    Toast.makeText(AppDetailActivity.this, "Ошибка при скачивании", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void installApp(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Файл установки не найден", Toast.LENGTH_SHORT).show();
        }
    }

    private void showInstallPrompt(final String filePath) {
        new AlertDialog.Builder(this)
                .setTitle("Установка приложения")
                .setMessage("Приложение успешно скачано. Хотите установить его сейчас?")
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        installApp(filePath);
                    }
                })
                .setNegativeButton("Нет", null)
                .show();
    }

}