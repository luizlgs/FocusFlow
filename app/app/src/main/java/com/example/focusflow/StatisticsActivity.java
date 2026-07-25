package com.example.focusflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {
    private ImageButton back_to_initial;

    private boolean is6months_ago(String date, String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            long task_milis = sdf.parse(date + " " + time).getTime();   // millis da task

            Calendar cutoff = Calendar.getInstance();
            cutoff.add(Calendar.MONTH, -6);            // agora menos 6 meses (respeita os meses reais)
            long cutoff_milis = cutoff.getTimeInMillis();

            long now = System.currentTimeMillis();
            return task_milis >= cutoff_milis && task_milis <= now;   // entre 6 meses atrás e agora
        } catch (Exception e) {
            return false;
        }
    }

    private void plotByDay(ArrayList<String> dates, int chartId, String label) {
        long dayMillis = 24L * 60 * 60 * 1000;

        // corte de 3 meses e total de dias no período
        Calendar cut = Calendar.getInstance();
        cut.add(Calendar.MONTH, -6);
        long cutoff = cut.getTimeInMillis();
        long now = System.currentTimeMillis();
        int totalDays = (int) ((now - cutoff) / dayMillis) + 1;

        // conta por dia (dia 0 = ~3 meses atrás, totalDays-1 = hoje)
        int[] perDay = new int[totalDays];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (String d : dates) {
            try {
                long ms = sdf.parse(d).getTime();
                int dayIndex = (int) ((ms - cutoff) / dayMillis);
                if (dayIndex < 0) dayIndex = 0;
                if (dayIndex >= totalDays) dayIndex = totalDays - 1;
                perDay[dayIndex]++;
            } catch (Exception e) { /* data inválida, ignora */ }
        }

        // pontos: x = dia, y = quantidade
        List<Entry> entries = new ArrayList<>();
        for (int day = 0; day < totalDays; day++) {
            entries.add(new Entry(day, perDay[day]));
        }

        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(2f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float value) {
                if (value == 0) return "";
                return String.valueOf((int) value);
            }
        });

        LineChart chart = findViewById(chartId);
        chart.setData(new LineData(dataSet));
        chart.getDescription().setEnabled(false);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setAxisMinimum(0f);
        x.setAxisMaximum(totalDays - 1);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float value) {
                if (value == 0) return "";
                return String.valueOf((int) value);
            }
        });
        chart.getAxisRight().setEnabled(false);

        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setVisibleXRangeMaximum(7f);     // ~1 semana por vez
        chart.moveViewToX(totalDays - 1);      // começa nos dias mais recentes

        chart.invalidate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences preferences = getSharedPreferences("BasicUserData", StatisticsActivity.this.MODE_PRIVATE);
        String pomodoro_sessions = preferences.getString("pomodorosessions", "SessionsNotFound");
        String tasks = preferences.getString("tasks", "TasksNotFound");
        String projects = preferences.getString("projects", "ProjectsNotFound");
        try {
            JSONArray pomodoro_sessions_json = new JSONArray(pomodoro_sessions);
            JSONArray projects_json = new JSONArray(projects);
            JSONArray tasks_json = new JSONArray(tasks);

            ArrayList<String> session_dates = new ArrayList<>();
            for (int i = 0; i < pomodoro_sessions_json.length(); i++) {
                String end_time = pomodoro_sessions_json.getJSONObject(i).optString("end_time", "");
                String date = pomodoro_sessions_json.getJSONObject(i).optString("date", "");
                if (end_time.isEmpty()) continue;   // sessão não encerrada, pula

                if (is6months_ago(date, end_time.split("\\.")[0]))
                    session_dates.add(date);        // guarda a data de conclusão
            }

            ArrayList<String> project_dates = new ArrayList<>();
            for (int i = 0; i < projects_json.length(); i++) {
                String completion_date = projects_json.getJSONObject(i).optString("completion_date", "");
                if (completion_date.isEmpty()) continue;   // projeto não concluído, pula

                //não salvo a hora em que um projeto é concluido ja que projetos são processos mais demorados
                if (is6months_ago(completion_date, "00:00:00"))
                    project_dates.add(completion_date);
            }

            ArrayList<String> task_dates = new ArrayList<>();
            for(int i=0; i<tasks_json.length(); i++){
                String current_task_completion_time = tasks_json.getJSONObject(i).optString("completion_time", "");
                String current_task_completion_date = tasks_json.getJSONObject(i).optString("completion_date", "");
                if(current_task_completion_time.isEmpty()) continue;

                if(is6months_ago(current_task_completion_date, current_task_completion_time.split("\\.")[0]))
                    task_dates.add(current_task_completion_date);
            }

            plotByDay(task_dates, R.id.tasksChart, "Tarefas concluídas");
            plotByDay(session_dates, R.id.sessionsChart, "Sessões concluídas");
            plotByDay(project_dates, R.id.projectsChart, "Projetos concluídos");


        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        back_to_initial = findViewById(R.id.back_to_initial);
        back_to_initial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StatisticsActivity.this, InitialScreenActivity.class);
                startActivity(intent);
            }
        });


    }
}