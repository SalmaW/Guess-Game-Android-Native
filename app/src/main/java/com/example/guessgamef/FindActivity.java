package com.example.guessgamef;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.ShakeDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class FindActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, ShakeDetector.ShakeListener {
    TextView wrongRightText, countText, findX;
    Button startButton;
    ImageView soundIcon;
    TableLayout guess;

    List<Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
    int index;

    Random r = new Random();
    int x;
    byte wrongs;
    boolean startGame, soundOn = true;

    HashSet<TextView> wallet = new HashSet<>();

    MediaPlayer player;
    TextToSpeech tts; // == text to speech
    SharedPreferences pref;

    Intent aa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_find);
        aa = new Intent(this, GameMenu.class);

        wrongRightText = findViewById(R.id.wrongRightText);
        countText = findViewById(R.id.countText);
        findX = findViewById(R.id.guessX);
        startButton = findViewById(R.id.startButton);
        soundIcon = findViewById(R.id.soundIcon);
        guess = findViewById(R.id.guess);//TableLayout

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onBackPressed() {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("sound", soundOn);
        editor.apply();

        tts.stop();
        tts.shutdown();

        Sensey.getInstance().stopShakeDetection(this);
        Sensey.getInstance().stop();

        if (player != null) {
            player.stop();
            player.release();
        }

        setResult(RESULT_OK, aa);
        finish();
        super.onBackPressed();
    }

    public void start(View view) {
        gameStarted();
    }

    private void gameStarted() {
        shuffleNumbers();
        if (player != null)
            player.stop();
        startGame = true;
        wrongs = 0;
        wrongRightText.setText("");
        countText.setText("");

        x = r.nextInt(9) + 1;
        findX.setText("Find: " + x);
        if (soundOn) {
            tts.speak(findX.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);
        }
//        Toast.makeText(this, "" + x, Toast.LENGTH_SHORT).show();
        for (TextView v : wallet) {
            v.setEnabled(true);
            v.setText("?");
        }
        wallet.clear();

    }

    private void shuffleNumbers() {
        // Shuffle the numbers
        Collections.shuffle(numbers);
        // Assign shuffled numbers to TextViews and set initial state
        index = 0;
        for (int i = 0; i < guess.getChildCount(); i++) {
            View child = guess.getChildAt(i);
            if (child instanceof TableRow) {
                TableRow row = (TableRow) child;

                for (int j = 0; j < row.getChildCount(); j++) {
                    View view = row.getChildAt(j);
                    if (view instanceof TextView) {
                        TextView textView = (TextView) view;
                        textView.setText("?");
                        textView.setTag(numbers.get(index++)); // Store the actual number in the Tag
                    }
                }
            }
        }
    }

    public void guess(View view) {
        if (!startGame) {
            Toast.makeText(this, "Click at start", Toast.LENGTH_SHORT).show();
            YoYo.with(Techniques.Tada).duration(500).playOn(startButton);
            return;
        }

        TextView v = (TextView) view;
        YoYo.with(Techniques.Shake).duration(200).playOn(v);
        v.setEnabled(false);
        wallet.add(v);
        if (soundOn) {
            tts.speak(v.getTag().toString(), TextToSpeech.QUEUE_FLUSH, null, null);
        }

        int guess = (int) v.getTag(); // Retrieve the actual number from the Tag
        v.setText(String.valueOf(guess)); // Show the actual number

        if (guess == x) {
            wrongRightText.setText("Great job! You Guessed the Number");
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
        // Initialization code for TextToSpeech
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
        // Handle shake detection
    }

    @Override
    public void onShakeStopped() {
        gameStarted();
    }
}
