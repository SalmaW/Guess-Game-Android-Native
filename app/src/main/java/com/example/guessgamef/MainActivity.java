package com.example.guessgamef;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.ShakeDetector;

import java.util.HashSet;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, ShakeDetector.ShakeListener {
    TextView wrongRightText, countText, guessX;
    Button startButton;
    ImageView soundIcon;
//    TableLayout guess;

    Random r = new Random();
    int x;
    byte wrongs;
    boolean startGame, soundOn = true;

    HashSet<TextView> walet = new HashSet<>();

    MediaPlayer player;
    TextToSpeech tts; // == text to speech
    SharedPreferences pref;

    Intent a;

//    @Override
//    protected void onStop() {
//        SharedPreferences.Editor editor = pref.edit();
//        editor.putBoolean("sound", soundOn);
//        editor.apply();
//
//        tts.stop();
//        tts.shutdown();
//
//        Sensey.getInstance().stopShakeDetection(this);
//        Sensey.getInstance().stop();
//
//        player.stop();
//        player.release();
//
//        super.onStop();
//    }

    @Override
    public void onBackPressed() {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("sound", soundOn);
        editor.apply();

        tts.stop();
        tts.shutdown();

        Sensey.getInstance().stopShakeDetection(this);
        Sensey.getInstance().stop();

        player.stop();
        player.release();

        setResult(RESULT_OK, a);
        finish();
        super.onBackPressed();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        a = new Intent(this, GameMenu.class);

        wrongRightText = findViewById(R.id.wrongRightText);
        countText = findViewById(R.id.countText);
        guessX = findViewById(R.id.guessX);
        startButton = findViewById(R.id.startButton);
        soundIcon = findViewById(R.id.soundIcon);
        tts = new TextToSpeech(this, this);
        pref = getSharedPreferences("settings", MODE_PRIVATE);
        soundOn = pref.getBoolean("sound", true);
        if (soundOn) {
            soundIcon.setImageResource(R.drawable.sound_on);
        } else {
            soundIcon.setImageResource(R.drawable.sound_off);
        }
        Sensey.getInstance().init(this);
        Sensey.getInstance().startShakeDetection(this);
    }

    public void start(View view) {
        gameStarted();
    }

    private void gameStarted() {
        if (player != null)
            player.stop();
        startGame = true;
        wrongs = 0;
        wrongRightText.setText("");
        countText.setText("");

        x = r.nextInt(9) + 1;
//        Toast.makeText(this, "" + x, Toast.LENGTH_SHORT).show();
        for (TextView v : walet) {
            v.setEnabled(true);
        }
        walet.clear();
    }

    public void guess(View view) {
        if (!startGame) {
            Toast.makeText(this, "click at start", Toast.LENGTH_SHORT).show();
            YoYo.with(Techniques.Tada).duration(500).playOn(startButton);
            return;
        }

        TextView v = (TextView) view;
        YoYo.with(Techniques.Shake).duration(200).playOn(v);
        v.setEnabled(false);
        walet.add(v);
        if (soundOn) {
            tts.speak(v.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);
        }
        int guess = Integer.parseInt(v.getText().toString());

        if (guess == x) {
            wrongRightText.setText("Grate job! You Guessed the Number");
            guessX.setText("The Number is: " + x);
            countText.setText("👏🏻");
            player = MediaPlayer.create(this, R.raw.correct);
            player.start();
            player.setVolume(0.5f, 0.5f);
            startGame = false;
        } else {
            wrongRightText.setText("Wrong. Try Again");
            wrongs++;
            player = MediaPlayer.create(this, R.raw.wrong);
            player.start();
            countText.setText(String.valueOf(wrongs));
        }

        if (wrongs == 3) {
            wrongRightText.setText("Game Over");
            Toast.makeText(this, "Game over", Toast.LENGTH_SHORT).show();
            player = MediaPlayer.create(this, R.raw.game_over);
            player.start();
            startGame = false;
        }


    }

    @Override
    public void onInit(int status) {

    }

    public void changeSound(View view) {
        YoYo.with(Techniques.Swing).duration(300).playOn(soundIcon);
        if (soundOn) {
            soundIcon.setImageResource(R.drawable.sound_off);
            Toast.makeText(this, "Reading Out Loud Sound OFF", Toast.LENGTH_SHORT).show();
            soundOn = false;
        } else {
            soundIcon.setImageResource(R.drawable.sound_on);
            Toast.makeText(this, "Reading Out Loud Sound ON", Toast.LENGTH_SHORT).show();
            soundOn = true;
        }
    }

    @Override
    public void onShakeDetected() {

    }

    @Override
    public void onShakeStopped() {
        gameStarted();
    }
}