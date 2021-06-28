package com.aryan.callsecurity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.aryan.callsecurity.Retrofit.IUploadAPI;
import com.aryan.callsecurity.Retrofit.RetrofitClient;
import com.aryan.callsecurity.Utils.IUploadcallback;
import com.aryan.callsecurity.Utils.ProgressRequestBody;

import java.io.File;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileSendtoServer extends Service implements IUploadcallback {
    private static boolean record_done;
    private static String audiofile;
    IUploadAPI mService;

    private IUploadAPI getAPIUpload(){
        return RetrofitClient.getClient().create(IUploadAPI.class);
    }

    @Override
    public void onCreate() {
        mService = getAPIUpload();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
        record_done = intent.getExtras().getBoolean("record_done");
        audiofile = intent.getExtras().getString("audiofile");

        if(record_done)
        {
            if(audiofile != null)
            {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setMessage("uploading......");
                dialog.setIndeterminate(false);
                dialog.setMax(100);
                dialog.setCancelable(false);
                dialog.show();
                File file = null;
                try{
                    file = new File(audiofile);

                }catch (Exception e){
                    e.printStackTrace();
                }
                if(file != null)
                {
                    final ProgressRequestBody requestbody = new ProgressRequestBody(file,this);
                    final MultipartBody.Part body = MultipartBody.Part.createFormData("data",file.getName(),requestbody);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                          mService.UploadFile(body).enqueue(new Callback<String>() {
                              @Override
                              public void onResponse(Call<String> call, Response<String> response) {
                                  dialog.dismiss();
                                  Toast.makeText(FileSendtoServer.this, "file uploaded", Toast.LENGTH_SHORT).show();
                              }

                              @Override
                              public void onFailure(Call<String> call, Throwable t) {
                                  dialog.dismiss();
                                  Toast.makeText(FileSendtoServer.this, t.getMessage(), Toast.LENGTH_SHORT).show();

                              }
                          });
                        }
                    }).start();
                }
                else{
                    Toast.makeText(this, "can't upload", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onProgressUpdate(int percent) {

    }
}
