package com.example.focusflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HistoryActivity extends AppCompatActivity {
    private TableLayout tabela;
    private ImageButton back_to_initial;

    //funcoes so quicksort para os JSONObjects das sessoes pomodoro, projects e tasks
    private JSONArray quicksortbyid(JSONArray arr){
        try {
            quicksort(arr, 0, arr.length() - 1);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return arr;
    }

    private void quicksort(JSONArray arr, int low, int high) throws JSONException {
        if (low < high) {
            int p = partition(arr, low, high);
            quicksort(arr, low, p - 1);
            quicksort(arr, p + 1, high);
        }
    }

    private int partition(JSONArray arr, int low, int high) throws JSONException {
        // pega o elemento do meio como pivô (evita o pior caso em array já ordenada)
        int mid = low + (high - low) / 2;
        swap(arr, mid, high);

        int pivot = getId(arr, high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (getId(arr, j) >= pivot) {   // >= => ordem DECRESCENTE (maior id primeiro)
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, high);
        return i + 1;
    }

    private int getId(JSONArray arr, int i) throws JSONException {
        return Integer.parseInt(arr.getJSONObject(i).getString("id"));
    }

    private void swap(JSONArray arr, int i, int j) throws JSONException {
        Object tmp = arr.get(i);
        arr.put(i, arr.get(j));
        arr.put(j, tmp);
    }


    private TextView makeCell(String text, int weight) {
        TextView attribute = new TextView(this);
        attribute.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight));
        attribute.setText(text);
        attribute.setTextSize(12);
        attribute.setMaxLines(2);
        attribute.setPadding(10, 10, 10, 10);
        attribute.setGravity(Gravity.CENTER);
        return attribute;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences preferences = getSharedPreferences("BasicUserData", HistoryActivity.this.MODE_PRIVATE);
        String pomodoro_sessions = preferences.getString("pomodorosessions", "PomodoroSessionsNotFound");
        String projects = preferences.getString("projects", "PomodoroSessionsNotFound");
        String tasks = preferences.getString("tasks", "PomodoroSessionsNotFound");

        try {

            JSONArray all = new JSONArray();

            JSONArray pomodoro_sessions_json = new JSONArray(pomodoro_sessions);
            JSONArray projects_json = new JSONArray(projects);
            JSONArray tasks_json = new JSONArray(tasks);

            for(int i=0; i<pomodoro_sessions_json.length(); i++) all.put(pomodoro_sessions_json.getJSONObject(i));
            for(int i=0; i<projects_json.length(); i++) all.put(projects_json.getJSONObject(i));
            for(int i=0; i<tasks_json.length(); i++) all.put(tasks_json.getJSONObject(i));

            JSONArray ordered = quicksortbyid(all);

            tabela = findViewById(R.id.minha_tabela);
            for(int i=ordered.length()-1; i>=0; i--){
                JSONObject obj = ordered.getJSONObject(i);

                String tipo, nome, fim, estado;
                nome = obj.optString("title", "");

                if (obj.has("blocks")) {// sessão pomodoro
                    tipo = "Pomodoro";
                    fim = obj.optString("start_time", "") + "\n" + obj.optString("end_time", ""); //mostra o end_time, se tiver
                    estado = fim.isEmpty() ? "Em andamento" : "Finalizada";
                } else if (obj.has("project_state")) {// projeto
                    tipo = "Projeto";
                    fim = obj.optString("start_date", "") + "\n" + obj.optString("completion_date", ""); //mostra o completion_date, se tiver
                    estado = obj.optString("project_state", "f").equals("t") ? "Finalizado" : "Em andamento";
                } else {// tarefa
                    tipo = "Tarefa";
                    fim = obj.optString("task_date", "") + "\n" + obj.optString("completion_date", ""); //mostra o completion_date, se tiver
                    estado = obj.optString("task_state", "f").equals("t") ? "Finalizada" : "À fazer";
                }

                TableRow row = new TableRow(this);
                row.addView(makeCell(tipo, 2));
                row.addView(makeCell(nome, 3));
                row.addView(makeCell(fim, 2));
                row.addView(makeCell(estado, 3));

                tabela.addView(row);
            }


        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        back_to_initial = findViewById(R.id.back_to_initial);
        back_to_initial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoryActivity.this, InitialScreenActivity.class);
                startActivity(intent);
            }
        });
    }
}