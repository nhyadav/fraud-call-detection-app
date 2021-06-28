package com.aryan.callsecurity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.aryan.callsecurity.Retrofit.IUploadAPI;
import com.aryan.callsecurity.Retrofit.RetrofitClient;
import com.aryan.callsecurity.Utils.Common;
import com.aryan.callsecurity.Utils.IUploadcallback;
import com.aryan.callsecurity.Utils.ProgressRequestBody;
import com.google.android.material.navigation.NavigationView;
import com.aryan.callsecurity.Recording_service;


import java.io.File;
import java.net.URISyntaxException;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, IUploadcallback {
    // initialize variable
    private static final int PICK_FILE_REQUEST = 1000;
    private static final String CHANNEL_ID = "callnotification100";
    IUploadAPI mService;
    Button btnupload, btnsend;
    ImageView imageView;
    Uri selectedFileUri;
    ProgressDialog dialog;


    private IUploadAPI getAPIUpload() {
        return RetrofitClient.getClient().create(IUploadAPI.class);

    }

    static final int REQUEST_CODE = 123;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        //..........phone service
        /*-------Button----------------*/
        Button button = findViewById(R.id.start_operation);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.PROCESS_OUTGOING_CALLS)
                        + ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                        + ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        + ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)
                        + ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

                        != PackageManager.PERMISSION_GRANTED) {
                    // when permission is not granted.
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.PROCESS_OUTGOING_CALLS)
                            || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                            || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_PHONE_STATE)) {
                        // create alert dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Please grant these permission");
                        builder.setMessage("All the above permission is mandatory for this application.");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_PHONE_STATE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.RECORD_AUDIO
                                }, REQUEST_CODE);
                            }
                        });
                        builder.setNegativeButton("Cancel", null);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                Manifest.permission.PROCESS_OUTGOING_CALLS,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.RECORD_AUDIO
                        }, REQUEST_CODE);
                    }
                } else {
                    // when permission are already granted.
                    operation_on();
                    //Toast.makeText(MainActivity.this, "permission is granted, operation will be perform.", Toast.LENGTH_SHORT).show();
                }
            }
        });
// for uploading file.
        mService = getAPIUpload();
        btnupload = findViewById(R.id.upload);
        btnsend = findViewById(R.id.sendtoserv);
        //***automatics loading..

        btnupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                chooseFile();
//                uploadFile();
//                Toast.makeText(MainActivity.this, "error...", Toast.LENGTH_SHORT).show();
            }
        });

        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
//                Toast.makeText(MainActivity.this, "error...", Toast.LENGTH_SHORT).show();
            }
        });

        // navigation bar
        /*------------- hook---------------------*/
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        /*-------tool bar-------*/
        setSupportActionBar(toolbar);


        /*-----------navigation driver menu------*/
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if ((grantResults.length > 0) && (grantResults[0] + grantResults[1] + grantResults[2] + grantResults[3] + grantResults[4] == PackageManager.PERMISSION_GRANTED)) {
                // permission is granted
                Toast.makeText(getApplicationContext(), "permission Granted..", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "permission not granted..", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                break;
            case R.id.nav_setting:
                Intent intent = new Intent(MainActivity.this, setting.class);
                startActivity(intent);
                break;

            case R.id.nav_help:
                Intent intent1 = new Intent(MainActivity.this, help.class);
                startActivity(intent1);
                break;
            case R.id.privacy:
                Intent intent2 = new Intent(MainActivity.this, privacy.class);
                startActivity(intent2);
                break;
            case R.id.rate_us:
                Toast.makeText(this, "please rate-us, to appreciate.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.share:
                Toast.makeText(this, "share for other security.", Toast.LENGTH_SHORT).show();
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "here is the shere content body.";
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "subject here");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "share via"));
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // navigation bar
    // start button
    public void operation_on() {
        // start of service
        Intent intent4 = new Intent(this, main_running.class);
        startActivity(intent4);
        // notification
        Intent intenttabactionfornotification = new Intent(this, main_running.class);
        intenttabactionfornotification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intenttabactionfornotification, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.insurance2)
                .setContentTitle("CallSecurity")
                .setContentText("Don't worry we are with you.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationmanager = NotificationManagerCompat.from(this);
        notificationmanager.notify(1, builder.build());
        //end notification

        Intent intent3 = new Intent(MainActivity.this, Recording_service.class);
        startService(intent3);
        Toast.makeText(this, "Fraud call detection service has started.", Toast.LENGTH_LONG).show();
    }


    // menu bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.feedback) {
            //action
            Toast.makeText(this, "this feedback is opened.", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.about_us) {
            // action
            Toast.makeText(this, "this is about us opened", Toast.LENGTH_SHORT).show();
            return true;
        }
        return true;
    }  //menu
    //file upload

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_FILE_REQUEST) {
                if (data != null) {
                    selectedFileUri = data.getData();
                    if (selectedFileUri != null && !selectedFileUri.getPath().isEmpty())
//                        imageView.setImageURI(selectedFileUri);
                        Toast.makeText(this, selectedFileUri.toString(), Toast.LENGTH_SHORT).show();
//                        Toast.makeText(this, "file selected.", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this, "file not found.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void uploadFile() {
    
        if (selectedFileUri != null) {
            dialog = new ProgressDialog(this);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMessage("uploading.....");
            dialog.setIndeterminate(false);
            dialog.setMax(100);
            dialog.setCancelable(false);
            dialog.show();
            File file = null;
            try {
                file = new File(Common.getFilePath(this, selectedFileUri));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            if (file != null) {
//                final ProgressRequestBody requestBody = new ProgressRequestBody(file,this);
                final ProgressRequestBody requestBody = new ProgressRequestBody(file, this);
                final MultipartBody.Part body = MultipartBody.Part.createFormData("data", file.getName(), requestBody);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mService.UploadFile(body).enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                dialog.dismiss();
                                if((response.body()).equals("fraud")) {
//
                                Intent fullScreenIntent = new Intent(MainActivity.this,ImportanceActivity.class);
                                PendingIntent fullscreenPending = PendingIntent.getActivity(MainActivity.this,0,fullScreenIntent,PendingIntent.FLAG_UPDATE_CURRENT);

                                NotificationCompat.Builder builder= new NotificationCompat.Builder(MainActivity.this,CHANNEL_ID)
                                .setSmallIcon(R.drawable.insurance2)
                                .setContentTitle("alert")
                                .setContentText("be careful this is fraud call.")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setFullScreenIntent(fullscreenPending,true);

                                NotificationManagerCompat notificationmanager = NotificationManagerCompat.from(MainActivity.this);
                                notificationmanager.notify(1, builder.build());
                                Toast.makeText(MainActivity.this, response.body().toString(), Toast.LENGTH_LONG).show();
                                }
                                else if(response.body().equals("normal"))
                                {
                                    Intent fullScreenIntent = new Intent(MainActivity.this,ImportanceActivity.class);
                                    PendingIntent fullscreenPending = PendingIntent.getActivity(MainActivity.this,0,fullScreenIntent,PendingIntent.FLAG_UPDATE_CURRENT);

                                    NotificationCompat.Builder builder= new NotificationCompat.Builder(MainActivity.this,CHANNEL_ID)
                                            .setSmallIcon(R.drawable.insurance2)
                                            .setContentTitle("alert")
                                            .setContentText("be continue, this is normal call.")
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            .setFullScreenIntent(fullscreenPending,true);

                                    NotificationManagerCompat notificationmanager = NotificationManagerCompat.from(MainActivity.this);
                                    notificationmanager.notify(1, builder.build());
                                    Toast.makeText(MainActivity.this, "normal", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();


            } else {
                Toast.makeText(this, "Can't upload", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    public void onProgressUpdate(int percent) {

    }


}
