package com.example.counter;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity  implements Methodcall {

    Button start;
    String audioFilePath;
    private MediaRecorder mediaRecorder;
    boolean isrecording=false;
   ExecutorService executer= Executors.newSingleThreadExecutor();

    long recordstartTime=0;
    private MediaPlayer mediaPlayer;

    RecyclerAdapter adapter;
    RecyclerView recyclerView;
    ArrayList<byte[]>list;
    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start=findViewById(R.id.pause);
        list=new ArrayList<>();

        audioFilePath = getPathTostorage();
        recyclerView=findViewById(R.id.recyclerview);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    11);
        }else{
           setupMediaRecorder(audioFilePath);
        }

      adapter=new RecyclerAdapter(list,this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fetchdata();


        start.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startrecording();
                    return true;

                case MotionEvent.ACTION_UP:
               new Handler().postDelayed(this::stoprecording,1000);
                runOnUiThread(() -> start.setText("start"));
                    return true;

                default:
                    return false;
            }


        });


    }

    private void stoprecording() {
        executer.execute(() -> {
            try {
                if(mediaRecorder!=null){

                    mediaRecorder.stop();
                    mediaRecorder.reset();
                    runOnUiThread(() -> {

                        saveRecording();
                        fetchdata();
                        audioFilePath=getPathTostorage();
                        setupMediaRecorder(audioFilePath);
                    });


                }
            }catch (Exception e){
            }

        });
    }


    private void saveRecording() {

        DatabaseHelper db=new DatabaseHelper(this);
        byte[] t= readFileToBytes(audioFilePath);
        db.insertRecording(t);
    }


    @SuppressLint("NotifyDataSetChanged")
    public void fetchdata() {
        DatabaseHelper db=new DatabaseHelper(this);
        list=db.getRecordingData();
        Toast.makeText(this, ""+list.size(), Toast.LENGTH_SHORT).show();
        adapter=new RecyclerAdapter(list,this);

        recyclerView.setAdapter(adapter);
        Toast.makeText(this, ""+adapter.getItemCount(), Toast.LENGTH_SHORT).show();
    }

    private void startrecording() {
        executer.execute(() -> {
            try {
                mediaRecorder.setOutputFile(audioFilePath);
                mediaRecorder.prepare();

                mediaRecorder.start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        start.setText("recording...");
                    }
                });

            }catch (Exception e){
                Log.i("error",e.getMessage());

            }
        });

    }

    private void playrecord(int pos) {

            try {
                File tempAudioFile = null;
                    tempAudioFile = File.createTempFile("audio", ".mp3", getCacheDir());
                    tempAudioFile.deleteOnExit();
                    File finalTempAudioFile = tempAudioFile;
                     FileOutputStream fos = new FileOutputStream(finalTempAudioFile);

                            fos.write(list.get(pos));
                            mediaPlayer=new MediaPlayer();
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(finalTempAudioFile.getAbsolutePath());
                            mediaPlayer.prepare();
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mp.start();
                                }
                            });


                mediaPlayer.setOnCompletionListener(mp -> {
                    mediaPlayer.release();
                    mediaPlayer=null;

                });

            }catch (Exception e){
                Log.i("error",e.getMessage());

            }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 11) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               setupMediaRecorder(audioFilePath);
            } else {
                // Handle permission denied
            }
        }
    }







    private void setupMediaRecorder(String outputFile) {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(outputFile);
        mediaRecorder.setAudioEncodingBitRate(128000);
        mediaRecorder.setAudioSamplingRate(44100);
    }


    private String getPathTostorage()  {
        int ran= (int) (Math.random()*1000);
        File publicFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "testMusic"+ran+".mp3");
        File tempAudioFile = null;
        try{
            String filename="temp_audio"+ran;
            tempAudioFile = File.createTempFile(filename, ".mp3", getCacheDir());
            tempAudioFile.deleteOnExit();
            return tempAudioFile.getAbsolutePath();
        }catch (Exception e){}
       Log.i("error",tempAudioFile.getAbsolutePath());
        return publicFile.getAbsolutePath();
    }
    public  byte[] readFileToBytes(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            int fileSize = fis.available();
            byte[] fileBytes = new byte[fileSize];
            fis.read(fileBytes);
            return fileBytes;
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Handle the error according to your needs
        }
    }

    @Override
    public void onclickcall(int pos) {
        Toast.makeText(this, "sdf"+pos, Toast.LENGTH_SHORT).show();
        playrecord( list.size()-1);

    }
}

class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyviewHolder>{

     ArrayList<byte[]>list;
     Methodcall methodcall;
     RecyclerAdapter(ArrayList<byte[]> bt,Methodcall call){

         this.list=bt;
         methodcall=call;
     }

    @NonNull
    @Override
    public RecyclerAdapter.MyviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.content,parent,false);
        return  new MyviewHolder(view,methodcall) ;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.MyviewHolder holder, int position) {
         holder.txt.setText(Integer.toString(position+1));
    }

    @Override
    public int getItemCount() {
       Log.i("error",""+list.size());
        return list.size();
    }

    public static class MyviewHolder extends RecyclerView.ViewHolder{
        TextView txt;
        ImageView img;
        public MyviewHolder(@NonNull View itemView,Methodcall methodcall) {
            super(itemView);
            txt=itemView.findViewById(R.id.textid);
            img=itemView.findViewById(R.id.playbtn);

            int pos=getAdapterPosition();
            img.setOnClickListener(v -> {

                     methodcall.onclickcall(pos);



            });

        }
    }

}
interface Methodcall{
    void onclickcall(int pos);

}