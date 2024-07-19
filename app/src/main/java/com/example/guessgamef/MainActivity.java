package com.example.guessgamef;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    TextView wrongRightText, countText;
    Button startButton;
//    TableLayout guess;

    Random r = new Random();
    int x;
    byte wrongs;
    boolean startGame;

    HashSet<TextView> walet = new HashSet<>();

    MediaPlayer player;
    TextToSpeech tts; // == text to speach


    @Override
    public void onBackPressed() {
        if (player != null)
            player.stop();
        super.onBackPressed();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wrongRightText = findViewById(R.id.wrongRightText);
        countText = findViewById(R.id.countText);
        startButton = findViewById(R.id.startButton);
        tts = new TextToSpeech(this,this);
    }

    public void start(View view) {
        if (player != null)
            player.stop();
        startGame = true;
        wrongs = 0;
        wrongRightText.setText("");
        countText.setText("");

        x = r.nextInt(9)+1;
        Toast.makeText(this,""+x,  Toast.LENGTH_SHORT).show();
        for (TextView v : walet) {
            v.setEnabled(true);
        }
        walet.clear();
    }

    public void guess(View view) {
        if(!startGame){
            Toast.makeText(this, "click at start",  Toast.LENGTH_SHORT).show();
            return;
        }

        TextView v = (TextView) view;
        v.setEnabled(false);
        walet.add(v);
        tts.speak(v.getText().toString(),TextToSpeech.QUEUE_FLUSH,null,null);
        int guess = Integer.parseInt(v.getText().toString());

        if(guess == x){
            wrongRightText.setText("Grate job! You Guessed the Number");
            countText.setText("👏🏻");
            player = MediaPlayer.create(this, R.raw.correct);
            player.start();
            player.setVolume(0.5f,0.5f);
            startGame = false;
        } else {
            wrongRightText.setText("Wrong. Try Again");
            wrongs++;
            player = MediaPlayer.create(this, R.raw.wrong);
            player.start();
            countText.setText(String.valueOf(wrongs));
        }

        if(wrongs == 3){
            wrongRightText.setText("Game Over");
            Toast.makeText(this, "Game over",  Toast.LENGTH_SHORT).show();
            player = MediaPlayer.create(this, R.raw.game_over);
            player.start();
            return;
        }
    }

    @Override
    public void onInit(int status) {

    }
}