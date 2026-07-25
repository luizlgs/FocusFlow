package com.example.focusflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

public class PomodoroSessionInfoActivity extends AppCompatActivity {
    private ImageButton back_to_sessions;
    private TextView session_title;
    private TextView session_id;
    private TextView session_description;
    private TextView session_blocks;
    private TextView session_short_pause;
    private TextView session_big_pause;
    private TextView session_start_time;
    private TextView session_end_time;
    private TextView total_focus;
    private TextView session_date;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pomodoro_session_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences preferences = getSharedPreferences("BasicUserData", PomodoroSessionInfoActivity.this.MODE_PRIVATE);
        String user_session = preferences.getString("current_session", "SessionNotFound");

        try {
            JSONObject session_json = new JSONObject(user_session);

            session_title = findViewById(R.id.session_title_text);
            session_title.setText(session_json.getString("title"));

            session_id = findViewById(R.id.session_id_text);
            String sessionId = session_json.getString("id");
            SpannableString textoSessionId = new SpannableString(sessionId);
            textoSessionId.setSpan(new ForegroundColorSpan(Color.parseColor("#80000000")), 0, textoSessionId.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            session_id.append(textoSessionId);

            session_description = findViewById(R.id.session_description_text);
            String sessionDescription = session_json.getString("description");
            SpannableString textoSessionDescription = new SpannableString(sessionDescription);
            textoSessionDescription.setSpan(new ForegroundColorSpan(Color.parseColor("#80000000")), 0, textoSessionDescription.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            session_description.append(textoSessionDescription);

            session_blocks = findViewById(R.id.session_blocks_text);
            String sessionBlocks = session_json.getString("blocks");
            String sessionBlocksFull = sessionBlocks;
            SpannableString textoSessionBlocks = new SpannableString(sessionBlocks);
            textoSessionBlocks.setSpan(new ForegroundColorSpan(Color.parseColor("#80000000")), 0, textoSessionBlocks.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            session_blocks.append(textoSessionBlocks);

            session_short_pause = findViewById(R.id.session_short_pause_text);
            String sessionShortPause = session_json.getString("short_pause");
            String sessionShortPauseFull = sessionShortPause;
            SpannableString textoSessionShortPause = new SpannableString(sessionShortPause);
            textoSessionShortPause.setSpan(new ForegroundColorSpan(Color.parseColor("#80000000")), 0, textoSessionShortPause.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            session_short_pause.append(textoSessionShortPause);

            session_big_pause = findViewById(R.id.session_big_pause_text);
            String sessionBigPause = session_json.getString("big_pause");
            String sessionBigPauseFull = sessionBigPause;
            SpannableString textoSessionBigPause = new SpannableString(sessionBigPause);
            textoSessionBigPause.setSpan(new ForegroundColorSpan(Color.parseColor("#80000000")), 0, textoSessionBigPause.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            session_big_pause.append(textoSessionBigPause);

            session_start_time = findViewById(R.id.session_start_time_text);
            String sessionStartTime = session_json.getString("start_time");
            String sessionStartTimeFull = sessionStartTime;
            sessionStartTime = sessionStartTime.substring(0, sessionStartTime.lastIndexOf(":"));
            SpannableString textoSessionStartTime = new SpannableString(sessionStartTime);
            textoSessionStartTime.setSpan(new ForegroundColorSpan(Color.parseColor("#80000000")), 0, textoSessionStartTime.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            session_start_time.append(textoSessionStartTime);

            session_end_time = findViewById(R.id.session_end_time_text);
            String sessionEndTime = session_json.getString("end_time");
            String sessionEndTimeFull = sessionEndTime;
            sessionEndTime = sessionEndTime.substring(0, sessionEndTime.lastIndexOf(":"));
            SpannableString textoSessionEndTime = new SpannableString(sessionEndTime);
            textoSessionEndTime.setSpan(new ForegroundColorSpan(Color.parseColor("#80000000")), 0, textoSessionEndTime.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            session_end_time.append(textoSessionEndTime);

            total_focus = findViewById(R.id.total_focus);
            String focus = session_json.getString("total_focus");
            SpannableString textoTotalFocus = new SpannableString(focus);
            textoTotalFocus.setSpan(new ForegroundColorSpan(Color.parseColor("#80000000")), 0, textoTotalFocus.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            total_focus.append(textoTotalFocus);

            session_date = findViewById(R.id.session_date_text);
            String sessionDate = session_json.getString("date");
            System.out.println(sessionDate);
            if(!sessionDate.equals("")) {
                SpannableString textoSessionDate = new SpannableString(sessionDate);
                textoSessionDate.setSpan(new ForegroundColorSpan(Color.parseColor("#80000000")), 0, textoSessionDate.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                session_date.append(textoSessionDate);
                session_date.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        back_to_sessions = findViewById(R.id.back_to_sessions);
        back_to_sessions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PomodoroSessionInfoActivity.this, PomodoroSessionActivity.class);
                startActivity(intent);
            }
        });
    }
}