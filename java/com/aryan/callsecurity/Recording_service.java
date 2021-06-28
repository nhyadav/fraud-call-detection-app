package com.aryan.callsecurity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.aryan.callsecurity.Retrofit.IUploadAPI;
import com.aryan.callsecurity.Retrofit.RetrofitClient;
import com.aryan.callsecurity.Utils.IUploadcallback;
import com.aryan.callsecurity.Utils.ProgressRequestBody;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Recording_service extends Service {
    private Handler handler = new Handler();
    String selectedFileUri;
    ProgressDialog dialog;
    MediaRecorder recorder;
    IUploadAPI mService;
    File audiofile;
    String name, phonenumber;
    String audio_format;
    public String Audio_Type;
    int audioSource;
    Context context;
    Timer timer;
    Boolean offhook = false, ringging = false;
    Toast toast;
    Boolean isoffhook = false;
    private Boolean record_started = false;
    public boolean record_done = false;
    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    private CallBr br_call;
    private int percent;

private IUploadAPI getAPIUpload(){
    return RetrofitClient.getClient().create(IUploadAPI.class);
}
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("service", "destroy");
        super.onDestroy();
    }

    @Override
    public void onCreate() {
    mService = getAPIUpload();
//        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //return super.onStartCommand(intent, flags, startId);
//        final String terminate = (String)intent.getExtras().get("terminate");
//        intent.getStringExtra("terminate");
//        Log.d("tag","service started");
//        TelephonyManager telephoney = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
//        CustomPhoneStateListener customPhoneStateListener = new CustomPhoneStateListener();
//        telephoney.listen(customPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
//        context = getApplicationContext();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);
        this.br_call = new CallBr();
        this.registerReceiver(this.br_call, filter);

//        if(teminate != null){
//            stopSelf();
//        }
        return START_NOT_STICKY;
    }

    public class CallBr extends BroadcastReceiver implements IUploadcallback {
        Bundle bundle;
        String state;
        String incall, outcall;
        public boolean wasRinging = false;


        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(ACTION_IN)) {
                if ((bundle = intent.getExtras()) != null) {
                    state = bundle.getString(TelephonyManager.EXTRA_STATE);
                    if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        incall = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        wasRinging = true;
                        Toast.makeText(context, "IN:" + incall, Toast.LENGTH_LONG).show();
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        if (wasRinging == true) {
                            record_done = false;
                            Toast.makeText(context, "ANSWERED", Toast.LENGTH_LONG).show();
                            String out = new SimpleDateFormat("dd-MM-yyyy hh-mm-ss").format(new Date());
                            File sampledir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS), "/callrecording");
                            if (!sampledir.exists()) {
                                sampledir.mkdirs();
                            }
                            String file_name = "Record";
                            try {
                                audiofile = File.createTempFile(file_name + out, ".amr", sampledir);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                            recorder = new MediaRecorder();
//                            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
                            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                            recorder.setOutputFile(audiofile.getAbsolutePath());

                            try {
                                recorder.prepare();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            recorder.start();
                            record_started = true;
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    recorder.stop();
                                    recorder.release();
                                    record_started = false;
                                    record_done = true;
                                    Toast.makeText(context, "recording done", Toast.LENGTH_SHORT).show();
//                                    Intent sendintent = new Intent(Recording_service.this,FileSendtoServer.class);
//                                    sendintent.putExtra("record_done",record_done);
//                                    selectedFileUri = audiofile.getAbsolutePath();
//                                    sendintent.putExtra("audiofile",selectedFileUri);
//                                    startService(sendintent);
//                                    uploadfileserver();
                                }
                            }, 10000);


                        }
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        wasRinging = false;
                        Toast.makeText(context, "REJECT || DISCONNECT", Toast.LENGTH_LONG).show();
                        if (record_started) {
                            recorder.stop();
                            recorder.release();
                            record_started = false;
                        }
                    }
                }
            } else if (intent.getAction().equals(ACTION_OUT)) {
                if ((bundle = intent.getExtras()) != null) {
                    outcall = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    Toast.makeText(context, "OUT:" + outcall, Toast.LENGTH_LONG).show();
//                    if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))
//                    {
//                            Toast.makeText(context,"ANSWERED",Toast.LENGTH_LONG).show();
//                            String out  = new SimpleDateFormat("dd-MM-yyyy hh-mm-ss").format(new Date());
//                            File sampledir = new File(Environment.getExternalStorageDirectory(),"/testRecording1");
//                            if(!sampledir.exists()){
//                                sampledir.mkdirs();
//                            }
//                            String file_name = "Record";
//                            try{
//                                audiofile = File.createTempFile(file_name + out,".amr",sampledir);
//
//                            } catch (IOException e)
//                            {
//                                e.printStackTrace();
//                            }
//                            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
//                            recorder = new MediaRecorder();
//                            //recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
//                            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
//                            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
//                            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//                            recorder.setOutputFile(audiofile.getAbsolutePath());
//
//                            try{
//                                recorder.prepare();
//                            }catch (IllegalStateException e)
//                            {
//                                e.printStackTrace();
//                            }
//                            catch (IOException e){
//                                e.printStackTrace();
//                            }
//                            recorder.start();
//                            record_started = true;
//
//
//                    }
//                    else if(state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
//                        wasRinging = false;
//                        Toast.makeText(context,"REJECT || DISCONNECT",Toast.LENGTH_LONG).show();
//                        if(record_started){
//                            recorder.stop();
//                            recorder.release();
//                            record_started=false;
//                        }
//                    }
                }
            }
        }

        private void uploadfileserver() {
            selectedFileUri = audiofile.getAbsolutePath();
            if (selectedFileUri != null) {
//                dialog = new ProgressDialog(Recording_service.this);
//                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//                dialog.setMessage("uploading.....");
//                dialog.setIndeterminate(false);
//                dialog.setMax(100);
//                dialog.setCancelable(false);
//                dialog.show();
                Toast.makeText(context, selectedFileUri, Toast.LENGTH_SHORT).show();
                File file = null;
                try {
//                file = new File(Common.getFilePath(this, selectedFileUri));
                    file = new File(selectedFileUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (file != null) {
                    final ProgressRequestBody requestBody = new ProgressRequestBody(file,this);
                    final MultipartBody.Part body = MultipartBody.Part.createFormData("data", file.getName(), requestBody);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mService.UploadFile(body).enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    dialog.dismiss();
                                    Toast.makeText(Recording_service.this, "File uploaded", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    dialog.dismiss();
                                    Toast.makeText(Recording_service.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();


                } else {
                    Toast.makeText(context, "Can't upload", Toast.LENGTH_SHORT).show();
                }

            }
        }

        @Override
        public void onProgressUpdate(int percent) {

        }
    }

}
