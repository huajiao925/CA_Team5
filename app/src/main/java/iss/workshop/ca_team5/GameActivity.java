package iss.workshop.ca_team5;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Locale;
import android.widget.TextView;

import java.util.Random;

public class GameActivity extends AppCompatActivity {

    GridView gridView;
    ArrayList<GridItem> gameImage = new ArrayList<>();

    int[] position ={0,1,2,3,4,5,0,1,2,3,4,5};
    Bitmap[] shuffledImages;
    int[] shuffledPos;
    boolean[] isFlipped ={false,false,false,false,false,false,false,false,false,false,false,false};

    int click = 0;
    int currentPos = -1;
    int prevPos = -1;
    int count = 0;

    //timer test-code
    private int timerSec = 0;
    private int endTime = 0;
    private boolean running;
    private boolean wasRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        loadSelectedImage(); //retrieve save selected images
        getGameImages(); //duplicate the gameImages

        //shuffled images
        shuffledPos = shuffle(position);
        shuffledImages = shuffleImages();

        if (savedInstanceState != null) {
            timerSec = savedInstanceState
                    .getInt("seconds");
            running = savedInstanceState
                    .getBoolean("running");
            wasRunning = savedInstanceState
                    .getBoolean("wasRunning");
        }
        runTimer();

        gridView = findViewById(R.id.grid_view);
        //hidden images
        Bitmap hidden1 = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.hidden1);
        Bitmap[] hidden = {hidden1, hidden1, hidden1,hidden1,hidden1,
                hidden1,hidden1,hidden1,hidden1,hidden1,hidden1,hidden1};

        GridAdapter adapter = new GridAdapter(this, hidden);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GridAdapter adapter = (GridAdapter)gridView.getAdapter();
                running = true;
                if(!isFlipped[i] && click == 0) {
                    isFlipped[i] = true;
                    click++;
                    prevPos = i;
                    adapter.flipImage(i, shuffledImages[i]);
                }
                if(!isFlipped[i] && click == 1){
                    if(!isMatched(prevPos,i)){
                        click++;
                        currentPos = i;
                        adapter.flipImage(i, shuffledImages[i]);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                GridAdapter adapter = (GridAdapter) gridView.getAdapter();
                                adapter.flipBack(currentPos, prevPos);
                                prevPos = -1;
                                currentPos = -1;
                                click = 0;
                            }
                        }, 500);
                        isFlipped[i] = false;
                        isFlipped[prevPos] = false;
                        //add sound
                        Toast.makeText(getApplicationContext(), "Not Match", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        adapter.flipImage(i, shuffledImages[i]);
                        isFlipped[i] =true;
                        // add sound
                        Toast.makeText(getApplicationContext(), "Match", Toast.LENGTH_SHORT).show();
                        count++;
                        System.out.println(count);
                        prevPos = -1;
                        currentPos = -1;
                        click = 0;
                        //
                        //THE GAME ENDS HERE!
                        if (count == 6) {running = false;
                            endTime = timerSec;
                            showEndDialog(endTime);
                        }

                        final TextView countView
                        = (TextView)findViewById(
                        R.id.countMatches);
                        if (count > 0) {countView.setText(count + " out of 6 matched");}
                    }
                }
            }
        });
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        wasRunning = running;
        running = false;
    }

    // If the activity is resumed,
    // start the stopwatch
    // again if it was running previously.
    @Override
    protected void onResume()
    {
        super.onResume();
        if (wasRunning) {
            running = true;
        }
    }
    @Override
    public void onSaveInstanceState(
            Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState
                .putInt("seconds", timerSec);
        savedInstanceState
                .putBoolean("running", running);
        savedInstanceState
                .putBoolean("wasRunning", wasRunning);
    }

    //shuffle position
    public int[] shuffle(int[] position){
        Random random = new Random();
        int[] shuffledPos = new int[position.length];
        int n = position.length;
        for (int i=0 ;i < n ;i++){
            int r = random.nextInt(n-i);
            int temp = position[r];
            position[r] = position[i];
            position[i] = temp;
        }
        for(int i=0; i < n ;i++){
            shuffledPos[i] = position[i];
        }
        return shuffledPos;
    }

    //shuffle images based on shuffled position
    public Bitmap[] shuffleImages(){
        int n = gameImage.size();
        Bitmap[] shuffledImages = new Bitmap[n];
        for(int i = 0; i<n;i++){
            shuffledImages[i]=gameImage.get(shuffledPos[i]).getImage();
        }
        return shuffledImages;
    }

    //checked if images matched
    public boolean isMatched(int prevPos, int currentPos){
        return (shuffledPos[prevPos] == shuffledPos[currentPos]);
    }


    private void runTimer()
    {
        final TextView timeView
            = (TextView)findViewById(
            R.id.timer);

        final Handler handler
                = new Handler();

        handler.post(new Runnable() {
            @Override

            public void run()
            {
                int hours = timerSec / 3600;
                int minutes = (timerSec % 3600) / 60;
                int secs = timerSec % 60;

                String time
                        = String
                        .format(Locale.getDefault(),
                                "%d:%02d",
                                minutes, secs);

                timeView.setText(time);

                if (running) {
                    timerSec++;
                }

                handler.postDelayed(this, 1000);
            }
        });
    }
    private void showEndDialog(int endTime) {

        int minutes = (endTime % 3600) / 60;
        int secs = endTime % 60;
        String time
                = String
                .format(Locale.getDefault(),
                        "%d:%02d",
                        minutes, secs);
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);

        builder.setPositiveButton("Play Again!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                dialog.dismiss();

            }
        });

        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.game_end_dialog, null);

        TextView messageView = (TextView)dialoglayout.findViewById(R.id.timetaken);
        if (minutes!=0){messageView.setText("You took " + minutes + " minutes and " + secs + " seconds!");}
        else{messageView.setText("Amazing! You only took "  + secs + " seconds!");}

        builder.setView(dialoglayout);
        builder.show();
    }

    //load game images from safe file
    public void loadSelectedImage(){
        for(int i = 0; i < 6; i++) {
            String name = "image" + i;
            FileInputStream fileInputStream;
            Bitmap bitmap = null;
            try {
                fileInputStream = getApplicationContext().openFileInput(name);
                bitmap = BitmapFactory.decodeStream(fileInputStream);
                gameImage.add(new GridItem(bitmap));
                fileInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //to duplicate game images
    protected void getGameImages(){
        for(int i=0; i < 6; i++){
           gameImage.add(gameImage.get(i));
        }
    }

}