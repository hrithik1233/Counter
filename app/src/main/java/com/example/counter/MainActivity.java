package com.example.counter;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SecondsCounterInterface {

    Button start,stop,pause,reset;
    TextView txt;
    SecondsCounter counter;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start=findViewById(R.id.start);
        stop=findViewById(R.id.stop);
        pause=findViewById(R.id.pause);
        reset=findViewById(R.id.reset);


        txt=findViewById(R.id.textView);
        counter=new SecondsCounter();
        counter.setUpdateInterfaceCall(this,true);
        start.setOnClickListener(v -> {
           counter.start();
        });

        stop.setOnClickListener(v -> {
         counter.stop();
        });

        pause.setOnClickListener(v -> {
              if(pause.getText().toString().equals("pause")){
                  counter.pause();
                  pause.setText("resume");
              }else{
                  counter.resume();
                  pause.setText("pause");
              }
        });

        reset.setOnClickListener(v -> {
         counter.reset();
        });

    }


    @Override
    public void updateTimeFormatSecondsCounter(long hr, long min, long sec) {

    }

    @Override
    public void updateStringFormatSecondsCounter(String timmeStamp) {
            runOnUiThread(() -> txt.setText(timmeStamp));
    }


    @Override
    public void updateTotalSecondsSecondsCounter(long totalSeondsCounter) {

    }
}