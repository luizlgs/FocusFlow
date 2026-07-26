package com.example.focusflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focusflow.api.CreateObjects;
import com.example.focusflow.api.DeleteObjects;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Locale;

public class PomodoroSessionActivity extends AppCompatActivity {
    private ImageButton new_pomodorosession_button;
    private ImageButton back_from_new_pomodorosession_button;
    private ImageButton back_to_initial;
    private ImageButton delete_pomodoro_button;
    private ImageButton back_from_delete_pomodorosession_button;
    private Button create_pomodorosession_button;
    private Button delete_pomodorosession_button;
    private EditText session_delete_id_field;
    private EditText session_title_field;
    private EditText session_description_field;
    private EditText session_blocks_field;
    private EditText session_short_pause_field;
    private EditText session_big_pause_field;
    private ScrollView delete_pomodorosession_scrollview;
    private TextView delete_pomodoro_error;

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

                if(pomodorosessions_json.getJSONObject(i).getString("end_time").isEmpty()){
                    newButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#80F44336")));
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
                    newButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#804CAF50")));
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
                session_title_field = findViewById(R.id.session_title_field);
                session_description_field = findViewById(R.id.session_description_field);
                session_blocks_field = findViewById(R.id.session_blocks_field);
                session_short_pause_field = findViewById(R.id.session_short_pause_field);
                session_big_pause_field = findViewById(R.id.session_big_pause_field);

                SharedPreferences preferences = getSharedPreferences("BasicUserData", MODE_PRIVATE);
                int userId = preferences.getInt("user_id", -1);

                String pomodorosBefore = preferences.getString("pomodorosessions", "[]");

                String sessionShortPause = session_short_pause_field.getText().toString().trim();
                String sessionBigPause = session_big_pause_field.getText().toString().trim();
                String sessionBlocks = session_blocks_field.getText().toString().trim();

                int shortPauseMinutes;
                int bigPauseMinutes;
                int blocksMinutes;

                try {
                    shortPauseMinutes = Integer.parseInt(sessionShortPause);
                    bigPauseMinutes = Integer.parseInt(sessionBigPause);
                    blocksMinutes = Integer.parseInt(sessionBlocks);
                } catch (NumberFormatException e) {
                    TextView invalidData = findViewById(R.id.invalid_data);
                    invalidData.setText("Dados inválidos");
                    invalidData.setVisibility(View.VISIBLE);
                    return;
                }

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

        delete_pomodoro_button = findViewById(R.id.delete_pomodoro_button);
        session_delete_id_field = findViewById(R.id.session_delete_id_field);
        delete_pomodorosession_scrollview = findViewById(R.id.delete_pomodorosession_scrollview);

        back_from_delete_pomodorosession_button = findViewById(R.id.back_from_delete_pomodorosession_button);
        delete_pomodorosession_button = findViewById(R.id.delete_pomodorosession_button);

        delete_pomodoro_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.main_pomodorosessions_scrollview).setVisibility(View.INVISIBLE);
                delete_pomodorosession_scrollview.setVisibility(View.VISIBLE);
            }
        });

        back_from_delete_pomodorosession_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete_pomodorosession_scrollview.setVisibility(View.INVISIBLE);
                findViewById(R.id.main_pomodorosessions_scrollview).setVisibility(View.VISIBLE);
            }
        });

        delete_pomodorosession_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete_pomodoro_error = findViewById(R.id.delete_pomodoro_error);
                delete_pomodoro_error.setVisibility(View.INVISIBLE);

                String typed_id_str = session_delete_id_field.getText().toString().trim();

                if (typed_id_str.isEmpty()) {
                    delete_pomodoro_error.setText("Digite o ID da sessão");
                    delete_pomodoro_error.setVisibility(View.VISIBLE);
                    return;
                }

                int typed_id;
                try {
                    typed_id = Integer.parseInt(typed_id_str);
                } catch (NumberFormatException e) {
                    delete_pomodoro_error.setText("ID inválido");
                    delete_pomodoro_error.setVisibility(View.VISIBLE);
                    return;
                }

                SharedPreferences preferences = getSharedPreferences("BasicUserData", MODE_PRIVATE);
                String pomodoro_sessions = preferences.getString("pomodorosessions", "[]");

                try {
                    JSONArray sessions_json = new JSONArray(pomodoro_sessions);
                    boolean id_exists = false;

                    for (int i = 0; i < sessions_json.length(); i++) {
                        if (sessions_json.getJSONObject(i).getInt("id") == typed_id) {
                            id_exists = true;
                            break;
                        }
                    }

                    if (!id_exists) {
                        delete_pomodoro_error.setText("Nenhuma sessão encontrada com esse ID");
                        delete_pomodoro_error.setVisibility(View.VISIBLE);
                        return;
                    }

                    String sessionsBefore = preferences.getString("pomodorosessions", "[]");

                    DeleteObjects deleteObjects = new DeleteObjects(PomodoroSessionActivity.this);
                    deleteObjects.deletePomodoroSession(String.valueOf(typed_id));

                    String sessionsAfter = preferences.getString("pomodorosessions", "[]");
                    if (sessionsBefore.equals(sessionsAfter)) {
                        delete_pomodoro_error.setText("Não foi possível apagar a sessão");
                        delete_pomodoro_error.setVisibility(View.VISIBLE);
                        return;
                    }

                    session_delete_id_field.setText("");
                    delete_pomodorosession_scrollview.setVisibility(View.INVISIBLE);
                    findViewById(R.id.main_pomodorosessions_scrollview).setVisibility(View.VISIBLE);
                    recreate();

                } catch (JSONException e) {
                    delete_pomodoro_error.setText("Erro ao verificar sessões");
                    delete_pomodoro_error.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}