package com.example.focusflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focusflow.api.CreateObjects;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Locale;

public class PomodoroSessionActivity extends AppCompatActivity {
    private ImageButton new_pomodorosession_button;
    private ImageButton back_from_new_pomodorosession_button;
    private Button create_pomodorosession_button;
    private ImageButton back_to_initial;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        recreate();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pomodoro_session);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        try {
            SharedPreferences preferences = getSharedPreferences("BasicUserData", PomodoroSessionActivity.this.MODE_PRIVATE);
            String user_pomodorosessions = preferences.getString("pomodorosessions", "PomodoroSessionsNotFound");
            JSONArray pomodorosessions_json = new JSONArray(user_pomodorosessions);

            LinearLayout pomodorosessionslayout = findViewById(R.id.pomodorosessionslayout);


            for (int i = 0; i < pomodorosessions_json.length(); i++) {
                final int index = i;
                Button newButton = (Button) getLayoutInflater().inflate(R.layout.botao_modelo, pomodorosessionslayout, false);

                newButton.setId(View.generateViewId());
                newButton.setText(pomodorosessions_json.getJSONObject(i).getString("title"));
                pomodorosessionslayout.addView(newButton);

                Log.e("Erroasasa",pomodorosessions_json.getJSONObject(i).getString("end_time"));


                if(pomodorosessions_json.getJSONObject(i).getString("end_time").isEmpty()){
                    newButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("current_session", pomodorosessions_json.getJSONObject(index).toString());
                                editor.apply();
                                Intent intent = new Intent(PomodoroSessionActivity.this, PomodoroSessionTimerActivity.class);
                                startActivity(intent);
                            } catch (JSONException e) {
                                Log.e("Erroasasa", "falha ao abrir a sessao no timer");
                            }
                        }
                    });
                }
                else {

                    newButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("current_session", pomodorosessions_json.getJSONObject(index).toString());
                                editor.apply();
                                Intent intent = new Intent(PomodoroSessionActivity.this, PomodoroSessionInfoActivity.class);
                                startActivity(intent);
                            } catch (Exception e) {
                                Log.e("Erro", "nao foi possivel acessar o json de projetos");
                            }
                        }
                    });
                }
            }

        } catch (Exception e) {
            Log.e("Erro", "falha ao acessar os dados do json de sessoes pomodoro");
        }

        new_pomodorosession_button = findViewById(R.id.new_pomodoro_button);
        new_pomodorosession_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.new_pomodorosession_scrollview).setVisibility(View.VISIBLE);
                findViewById(R.id.main_pomodorosessions_scrollview).setVisibility(View.INVISIBLE);
            }
        });

        back_from_new_pomodorosession_button = findViewById(R.id.back_from_new_pomodorosession_button);
        back_from_new_pomodorosession_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.new_pomodorosession_scrollview).setVisibility(View.INVISIBLE);
                findViewById(R.id.main_pomodorosessions_scrollview).setVisibility(View.VISIBLE);
            }
        });

        back_to_initial = findViewById(R.id.back_to_initial);
        back_to_initial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PomodoroSessionActivity.this, InitialScreenActivity.class);
                startActivity(intent);
            }
        });

        create_pomodorosession_button = findViewById(R.id.create_pomodorosession_button);
        create_pomodorosession_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText session_title_field = findViewById(R.id.session_title_field);
                EditText session_description_field = findViewById(R.id.session_description_field);
                EditText session_blocks_field = findViewById(R.id.session_blocks_field);
                EditText session_short_pause_field = findViewById(R.id.session_short_pause_field);
                EditText session_big_pause_field = findViewById(R.id.session_big_pause_field);

                SharedPreferences preferences = getSharedPreferences("BasicUserData", MODE_PRIVATE);
                int userId = preferences.getInt("user_id", -1);

                String pomodorosBefore = preferences.getString("pomodorosessions", "[]");

                String sessionShortPause = session_short_pause_field.getText().toString().trim();
                String sessionBigPause = session_big_pause_field.getText().toString().trim();
                String sessionBlocks = session_blocks_field.getText().toString().trim();

                int shortPauseMinutes = Integer.parseInt(sessionShortPause);
                int bigPauseMinutes = Integer.parseInt(sessionBigPause);
                int blocksMinutes = Integer.parseInt(sessionBlocks);

                String sessionShortPauseFormatted = String.format(Locale.getDefault(), "%02d:%02d:00", shortPauseMinutes / 60, shortPauseMinutes % 60);
                String sessionBigPauseFormatted = String.format(Locale.getDefault(), "%02d:%02d:00", bigPauseMinutes / 60, bigPauseMinutes % 60);
                String sessionBlocksFormatted = String.format(Locale.getDefault(), "%02d:%02d:00", blocksMinutes / 60, blocksMinutes % 60);

                CreateObjects new_pomodoro = new CreateObjects(PomodoroSessionActivity.this);
                new_pomodoro.createPomodoroSession(
                        session_title_field.getText().toString().trim(),
                        session_description_field.getText().toString().trim(),
                        sessionShortPauseFormatted,
                        sessionBigPauseFormatted,
                        sessionBlocksFormatted,
                        String.valueOf(userId));

                String pomodorosAfter = preferences.getString("pomodorosessions", "[]");
                if (pomodorosBefore.equals(pomodorosAfter)) {
                    TextView invalidData = findViewById(R.id.invalid_data);
                    invalidData.setVisibility(View.VISIBLE);
                    return;
                }

                Intent intent = new Intent(PomodoroSessionActivity.this, PomodoroSessionTimerActivity.class);
                startActivity(intent);

                findViewById(R.id.new_pomodorosession_scrollview).setVisibility(View.INVISIBLE);
                findViewById(R.id.main_pomodorosessions_scrollview).setVisibility(View.VISIBLE);
                session_title_field.setText("");
                session_description_field.setText("");
                session_blocks_field.setText("");
                session_short_pause_field.setText("");
                session_big_pause_field.setText("");
                recreate();
            }
        });

    }
}