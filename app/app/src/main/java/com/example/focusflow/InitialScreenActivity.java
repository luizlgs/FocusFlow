package com.example.focusflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
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
import java.util.List;
import java.util.Locale;

public class InitialScreenActivity extends AppCompatActivity {
    private ImageButton sandwich_button;
    private Button profile_button;
    private Button projects_button;
    private Button tasks_button;
    private Button historybutton;
    private Button pomodorosessionsbutton;
    private Button statistics_button;

    boolean side_bar_is_on = false;
    private BarChart chart;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        recreate();
    }

    private boolean is24h_ago(String date, String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            long task_milis = sdf.parse(date + " " + time).getTime();// millis da task
            long now = System.currentTimeMillis();// millis agora
            long diff = now - task_milis;
            long milisseconds_in_a_day = 24 * 60 * 60 * 1000;
            return diff >= 0 && diff <= milisseconds_in_a_day;
        } catch (Exception e) {
            return false;
        }
    }

    private void close_side_bar(){
        ConstraintLayout parentLayout = findViewById(R.id.main);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(parentLayout);

        TransitionManager.beginDelayedTransition(parentLayout);

        constraintSet.clear(R.id.sandwich_button, ConstraintSet.START);

        constraintSet.connect(
                R.id.sandwich_button, ConstraintSet.END,
                ConstraintSet.PARENT_ID, ConstraintSet.END,
                45
        );
        constraintSet.applyTo(parentLayout);

        findViewById(R.id.sidebar_menu).setVisibility(View.INVISIBLE);
        side_bar_is_on = false;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_initial_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //createProjectsChart(); //criação do grafico dos projetos
        //createTasksChart(); //criação do grafico das tarefas

        // carrega as informacoes basicas do usuario salvos no login
        SharedPreferences preferences = getSharedPreferences("BasicUserData", InitialScreenActivity.this.MODE_PRIVATE);

        String user_name = preferences.getString("user_name", "UserNotFound");

        TextView hello_text = findViewById(R.id.hellotext);
        hello_text.setText("Hello, "+user_name.split(" ")[0]+"!");

        ConstraintLayout parentLayout = findViewById(R.id.main);
        sandwich_button = findViewById(R.id.sandwich_button);

        sandwich_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!side_bar_is_on){
                    // 1. Cria o modificador de posições e copia o estado atual da tela
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(parentLayout);

                    // 2. Ativa a animação de transição suave automática
                    TransitionManager.beginDelayedTransition(parentLayout);

                    // 4. Quebra a amarração da direita (que prendia o botão na quina da tela)
                    constraintSet.clear(R.id.sandwich_button, ConstraintSet.END);

                    // 5. Cria a nova amarração: Gruda o lado esquerdo do botão no lado esquerdo do menu
                    constraintSet.connect(
                            R.id.sandwich_button, ConstraintSet.START,
                            R.id.sidebar_menu, ConstraintSet.START,
                            (-120) // Margem de afastamento em pixels para o botão não colar na quina interna do menu
                    );

                    // 6. Aplica as modificações fisicamente na tela
                    constraintSet.applyTo(parentLayout);

                    findViewById(R.id.sidebar_menu).setVisibility(View.VISIBLE);

                    side_bar_is_on = true;
                }
                else{
                    close_side_bar();
                }
            }
        });

        profile_button = findViewById(R.id.profilebutton);
        profile_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InitialScreenActivity.this, ProfileActivity.class);
                startActivity(intent);
                close_side_bar();
            }
        });

        projects_button = findViewById(R.id.projectsbutton);
        projects_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InitialScreenActivity.this, ProjectsActivity.class);
                startActivity(intent);
                close_side_bar();

                findViewById(R.id.sidebar_menu).setVisibility(View.INVISIBLE);
            }
        });

        tasks_button = findViewById(R.id.tasksbutton);
        tasks_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InitialScreenActivity.this, TasksActivity.class);
                startActivity(intent);
                close_side_bar();

                findViewById(R.id.sidebar_menu).setVisibility(View.INVISIBLE);
            }
        });

        pomodorosessionsbutton = findViewById(R.id.pomodorosessionsbutton);
        pomodorosessionsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InitialScreenActivity.this, PomodoroSessionActivity.class);
                startActivity(intent);
            }
        });

        historybutton = findViewById(R.id.historybutton);
        historybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InitialScreenActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        statistics_button = findViewById(R.id.statistics_button);
        statistics_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InitialScreenActivity.this, StatisticsActivity.class);
                startActivity(intent);
            }
        });


        //montagem do grafico de dashboard
        String tasks = preferences.getString("tasks", "TasksNotFound");
        try {
            JSONArray tasks_json = new JSONArray(tasks);
            //salvando todos os horarios
            ArrayList<String> times = new ArrayList<>();
            for(int i=0; i<tasks_json.length(); i++){
                String current_task_completion_time = tasks_json.getJSONObject(i).optString("completion_time", "");
                String current_task_completion_date = tasks_json.getJSONObject(i).optString("completion_date", "");
                if(current_task_completion_time.isEmpty()) continue;

                if(is24h_ago(current_task_completion_date, current_task_completion_time.split("\\.")[0]))
                    times.add(current_task_completion_time);
            }


            //montagem do grafico das tasks

            // 6 blocos por hora => blocos de 10 min => 144 no total
            int slots = 24 * 3;
            int[] perSlot = new int[slots];
            for (String t : times) {
                String[] p = t.split(":");
                int hour = Integer.parseInt(p[0]);
                int minute = Integer.parseInt(p[1]);
                int slot = (hour * 60 + minute) / 20;   // qual bloco de 20 min
                perSlot[slot]++;
            }

            // pontos: x em horas (cada bloco = 1/6 de hora), y = quantidade
            List<Entry> entries = new ArrayList<>();
            for (int s = 0; s < slots; s++) {
                float xHora = s / 3f;                    // 0, 0.166, 0.333 ... => 6 por hora
                entries.add(new Entry(xHora, perSlot[s]));
            }

            LineDataSet dataSet = new LineDataSet(entries, "Tarefas concluídas");
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(2f);
            dataSet.setValueTextSize(10f);
            dataSet.setValueFormatter(new ValueFormatter() {
                @Override public String getFormattedValue(float value) { return String.valueOf((int) value); }
            });

            LineChart chart = findViewById(R.id.tasksChart);
            chart.setData(new LineData(dataSet));
            chart.getDescription().setEnabled(false);

            // eixo X em horas (rótulos de 1 em 1, mas 6 pontos entre cada)
            XAxis x = chart.getXAxis();
            x.setPosition(XAxis.XAxisPosition.BOTTOM);
            x.setGranularity(1f);
            x.setAxisMinimum(0f);
            x.setAxisMaximum(24f);

            // eixo Y só inteiros
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

            // zoom / rolagem no X
            chart.setTouchEnabled(true);
            chart.setDragEnabled(true);
            chart.setScaleEnabled(true);
            chart.setPinchZoom(true);
            chart.setVisibleXRangeMaximum(6f);   // 6 horas por vez (= 36 pontos)
            chart.moveViewToX(0f);

            dataSet.setValueFormatter(new ValueFormatter() {
                @Override public String getFormattedValue(float value) {
                    if (value == 0) return "";
                    return String.valueOf((int) value);
                }
            });

            chart.invalidate();


        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


    }
}