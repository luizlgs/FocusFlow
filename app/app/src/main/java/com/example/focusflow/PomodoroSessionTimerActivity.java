package com.example.focusflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focusflow.api.ChangeObjectsStates;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class PomodoroSessionTimerActivity extends AppCompatActivity {
    private ImageButton back_to_sessions;
    private Button start_session_button;
    private Button stop_button;
    TextView timer;

    private Thread t;

    private long phase_start_elapsed = 0;
    private int phase_duration_seconds = 0;
    private long pause_started_elapsed = 0;
    private int remaining;

    private int total_focus = 0;
    private int small_pauses = 0;
    private int big_pauses = 0;
    private boolean is_pause = true;

    private Handler timerHandler;
    private Runnable timerRunnable;

    private boolean timer_stopped = false;

    private JSONObject session_json;
    private boolean session_is_on = true;

    private SharedPreferences preferences;
    private String user_session;
    private String current_time;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        recreate();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (session_is_on && current_time != null && session_json != null && session_json.has("id")) {
            ChangeObjectsStates standBy = new ChangeObjectsStates(PomodoroSessionTimerActivity.this);
            try {
                standBy.standByPomodoro(session_json.getString("id"), total_focus, current_time, small_pauses, big_pauses, is_pause);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void end_session(JSONObject session_json){
        session_is_on = false;
        timerHandler.removeCallbacks(timerRunnable);
        try {
            ChangeObjectsStates end_pomodoro_session = new ChangeObjectsStates(PomodoroSessionTimerActivity.this);
            end_pomodoro_session.endPomodoroSession(session_json.getString("id"), total_focus, current_time, () -> runOnUiThread(() -> {}));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        //tira a sessao atual da current_session
        SharedPreferences preferences = getSharedPreferences("BasicUserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("current_session", "{}");
        editor.apply();

        findViewById(R.id.session_ended_text).setVisibility(View.VISIBLE);
        Intent intent = new Intent(PomodoroSessionTimerActivity.this, PomodoroSessionActivity.class);
        startActivity(intent);
    }

    private int timeToSeconds(String time){
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0])*3600 + Integer.parseInt(parts[1])*60 + Integer.parseInt(parts[2]);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pomodoro_session_timer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        preferences = getSharedPreferences("BasicUserData", PomodoroSessionTimerActivity.this.MODE_PRIVATE);
        user_session = preferences.getString("current_session", "SessionNotFound");


        try {
            session_json = new JSONObject(user_session);

            TextView timer_session_title = findViewById(R.id.timer_session_title);
            timer_session_title.setText(session_json.getString("title"));

            String horas_string_blocks = session_json.getString("blocks").substring(0, 2);
            String minutos_string_blocks = session_json.getString("blocks").substring(3, 5);
            String segundos_string_blocks = session_json.getString("blocks").substring(6, 8);
            String time_blocks = horas_string_blocks+":"+minutos_string_blocks+":"+segundos_string_blocks;

            String horas_string_small = session_json.getString("short_pause").substring(0, 2);
            String minutos_string_small = session_json.getString("short_pause").substring(3, 5);
            String segundos_string_small = session_json.getString("short_pause").substring(6);
            String time_small = horas_string_small+":"+ minutos_string_small+":"+segundos_string_small;

            String horas_string_big = session_json.getString("big_pause").substring(0, 2);
            String minutos_string_big = session_json.getString("big_pause").substring(3, 5);
            String segundos_string_big = session_json.getString("big_pause").substring(6, 8);
            String time_big = horas_string_big+":"+minutos_string_big+":"+segundos_string_big;

            //restaura o timer antigo se for uma sessão que ainda nao terminou
            timer = findViewById(R.id.timer_text);
            String timer_existent = session_json.optString("timer", "");
            if (!timer_existent.isEmpty()) {
                remaining = timeToSeconds(timer_existent);   // retoma do valor salvo
            } else {
                remaining = timeToSeconds(time_blocks);   // sessão nova, começa do bloco
            }

            //restaura o foco antigo se for uma sessão que ainda nao terminou
            String focus_existent = session_json.optString("total_focus", "");
            if (!focus_existent.isEmpty()) {
                total_focus = timeToSeconds(focus_existent);
            }

            //restaura a fase do ciclo (bloco/pausa e contadores)
            String small_pauses_existent = session_json.optString("small_pauses", "");
            if (!small_pauses_existent.isEmpty()) {
                small_pauses = Integer.parseInt(small_pauses_existent);
            }

            //restaura a pause loonga
            String big_pauses_existent = session_json.optString("big_pauses", "");
            if (!big_pauses_existent.isEmpty()) {
                big_pauses = Integer.parseInt(big_pauses_existent);
            }

            //restaura o estado da sessao
            String is_pause_existent = session_json.optString("is_pause", "");
            if (!is_pause_existent.isEmpty()) {
                is_pause = is_pause_existent.equals("t") || is_pause_existent.equals("true");
            }

            timer.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", remaining/3600, (remaining%3600)/60, remaining%60));

            start_session_button = findViewById(R.id.start_session_button);
            start_session_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (start_session_button.getText().toString().equals("Iniciar sessão")) {
                        timerHandler = new Handler(); //controlador/agendador do runnable
                        start_session_button.setText("Encerrar sessão");
                        Button meuBotao = findViewById(R.id.start_session_button);
                        meuBotao.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#99CC0000")));

                        phase_start_elapsed = android.os.SystemClock.elapsedRealtime();
                        phase_duration_seconds = timeToSeconds(timer.getText().toString()); //tempo do bloco de foco
                        current_time = timer.getText().toString();
                        timerRunnable = new Runnable() {

                            @Override
                            public void run() {
                                long now = android.os.SystemClock.elapsedRealtime();
                                int elapsed_in_phase = (int) ((now - phase_start_elapsed) / 1000); //tempo passado desde o inicio da fase atual
                                remaining = phase_duration_seconds - elapsed_in_phase; // duração da fase - o que ja passou desde o inicio da fase

                                if(is_pause){
                                    total_focus++;
                                }

                                if (remaining <= 0) {
                                    if (!is_pause) {
                                        current_time = time_blocks;
                                        is_pause = true;
                                    } else {
                                        if (small_pauses == 3) {
                                            small_pauses = 0;
                                            big_pauses++;
                                            current_time = time_big;
                                        } else {
                                            small_pauses++;
                                            current_time = time_small;
                                        }
                                        is_pause = false;
                                    }
                                    phase_start_elapsed = now; //inicio da proxima fase
                                    phase_duration_seconds = timeToSeconds(current_time); //quantidade de tempo que durará o timer atual (do blocks, do small ou big pause)
                                    remaining = phase_duration_seconds;

                                }
                                //salva o remaining no current_time
                                current_time = String.format(Locale.getDefault(), "%02d:%02d:%02d", remaining/3600, (remaining%3600)/60, remaining%60);
                                timer.setText(current_time);
                                timerHandler.postDelayed(this, 1000);
                            }
                        };
                        timerHandler.postDelayed(timerRunnable, 1000); //espera um segundo e depois começa o timer

                    } else {
                        end_session(session_json);
                    }
                }
            });

            stop_button = findViewById(R.id.stop_button);
            stop_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (timerHandler == null) return;
                    if(timer_stopped) {
                        stop_button.setText("Parar");
                        long pausedDuration = android.os.SystemClock.elapsedRealtime() - pause_started_elapsed;
                        phase_start_elapsed += pausedDuration;
                        timerHandler.postDelayed(timerRunnable, 1000);
                        timer_stopped = false;
                    }

                    else {
                        stop_button.setText("Continuar");
                        pause_started_elapsed = android.os.SystemClock.elapsedRealtime();
                        timerHandler.removeCallbacks(timerRunnable);
                        timer_stopped = true;
                    }
                }
            });


        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        back_to_sessions = findViewById(R.id.back_to_sessions);
        back_to_sessions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PomodoroSessionTimerActivity.this, PomodoroSessionActivity.class);
                startActivity(intent);
            }
        });
    }
}