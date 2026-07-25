package com.example.focusflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import com.example.focusflow.api.ChangeObjectsStates;

public class TasksInfoActivity extends AppCompatActivity {
    private ImageButton conclude_task;
    private ImageButton back_to_tasks;

    boolean concluded;

    private TextView task_title;
    private TextView task_description;
    private TextView task_date;
    private TextView task_state;
    private TextView task_completion_date;
    private TextView task_completion_time;
    private TextView task_priority;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tasks_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences preferences = getSharedPreferences("BasicUserData", TasksInfoActivity.this.MODE_PRIVATE);
        String user_task = preferences.getString("current_task", "ProjectNotFound");
        try{

            JSONObject user_task_json = new JSONObject(user_task);

            task_title = findViewById(R.id.task_title);
            task_title.setText(user_task_json.getString("title"));

            task_description = findViewById(R.id.task_description);
            String description = user_task_json.getString("description");
            SpannableString textoDescription = new SpannableString(description);
            textoDescription.setSpan(new ForegroundColorSpan(Color.parseColor("#80000000")), 0, textoDescription.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            task_description.append(textoDescription);

            task_date = findViewById(R.id.task_date);
            String taskDate = user_task_json.getString("task_date");
            SpannableString textoTaskDate = new SpannableString(taskDate);
            textoTaskDate.setSpan(new ForegroundColorSpan(Color.parseColor("#80000000")), 0, textoTaskDate.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            task_date.append(textoTaskDate);

            task_state = findViewById(R.id.task_state);
            String taskState = user_task_json.getString("task_state");

            task_completion_date = findViewById(R.id.task_completion_date);
            task_completion_time = findViewById(R.id.task_completion_time);

            if(user_task_json.getString("task_state").equals("t")) {

                String completionDate = user_task_json.getString("completion_date");
                String completionTime = user_task_json.getString("completion_time");
                completionTime = completionTime.substring(0, completionTime.lastIndexOf(":")); //remove os milissegundos

                SpannableString textoCompletionDate = new SpannableString(completionDate);
                SpannableString textoCompletionTime = new SpannableString(completionTime);

                textoCompletionDate.setSpan(
                        new ForegroundColorSpan(Color.parseColor("#80000000")),
                        0,
                        textoCompletionDate.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );

                task_completion_date.append(textoCompletionDate);

                textoCompletionTime.setSpan(
                        new ForegroundColorSpan(Color.parseColor("#80000000")),
                        0,
                        textoCompletionTime.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );

                task_completion_time.append(textoCompletionTime);

            } else {
                task_completion_date.setVisibility(View.GONE);
                task_completion_time.setVisibility(View.GONE);
            }

            if(taskState.equals("f")){
                taskState = "À fazer";
                concluded = false;
            }
            else{
                taskState = "Finalizado";
                concluded = true;
            }
            SpannableString textoTaskState = new SpannableString(taskState);
            textoTaskState.setSpan(new ForegroundColorSpan(Color.parseColor("#80000000")), 0, textoTaskState.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            task_state.append(textoTaskState);

            task_priority = findViewById(R.id.task_priority);
            String priority = user_task_json.getString("priority");
            SpannableString textoPriority = new SpannableString(priority);
            textoPriority.setSpan(new ForegroundColorSpan(Color.parseColor("#80000000")), 0, textoPriority.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            task_priority.append(textoPriority);


        }catch (Exception e){
            Log.e("Erro", "nao foi possivel acessar o json de projetos");
        }


        conclude_task = findViewById(R.id.conclude_task);
        if(concluded)
            conclude_task.setImageResource(R.drawable.botao_nconcluir);
        else
            conclude_task.setImageResource(R.drawable.botao_concluir);


        conclude_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    JSONObject task_json = new JSONObject(user_task);

                    ChangeObjectsStates changeTask = new ChangeObjectsStates(
                            TasksInfoActivity.this
                    );
                    changeTask.endTask(
                            task_json.getString("id"),
                            () -> runOnUiThread(() -> recreate())
                    );
                } catch (Exception e){
                    throw new RuntimeException(e);
                }
            }
        });

        back_to_tasks = findViewById(R.id.back_to_tasks);
        back_to_tasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TasksInfoActivity.this, TasksActivity.class);
                startActivity(intent);
            }
        });
    }
}