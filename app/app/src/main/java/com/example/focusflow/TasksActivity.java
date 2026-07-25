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
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focusflow.api.CreateObjects;

import org.json.JSONArray;

public class TasksActivity extends AppCompatActivity {
    private ImageButton new_task_button;
    private ImageButton back_from_new_task_button;
    private Button create_task_button;
    private ImageButton back_to_initial;
    private boolean recreate_ = true;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        recreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!recreate_) {
            recreate();
            recreate_ = true;
        }
        recreate_ = false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tasks);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        try {
            SharedPreferences preferences = getSharedPreferences("BasicUserData", TasksActivity.this.MODE_PRIVATE);
            String user_tasks = preferences.getString("tasks", "TasksNotFound");
            JSONArray tasks_json = new JSONArray(user_tasks);

            LinearLayout taskslayout = findViewById(R.id.taskslayout);

            for(int i=0; i<tasks_json.length(); i++) {
                final int index = i;
                Button newButton = (Button) getLayoutInflater().inflate(R.layout.botao_modelo, taskslayout, false);

                newButton.setId(View.generateViewId());

                newButton.setText(tasks_json.getJSONObject(i).getString("title"));
                if(tasks_json.getJSONObject(i).getString("task_state").equals("t"))
                    newButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#804CAF50")));
                else
                    newButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#80F44336")));

                taskslayout.addView(newButton);

                newButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("current_task", tasks_json.getJSONObject(index).toString());
                            editor.apply();
                            recreate_ = false;
                            Intent intent = new Intent(TasksActivity.this, TasksInfoActivity.class);
                            startActivity(intent);
                        } catch (Exception e){
                            Log.e("Erro", "nao foi possivel acessar o json de projetos");
                        }
                    }
                });
            }


        }catch (Exception e) {
            Log.e("Erro", "falha ao acesar os dados do json de projetos");
        }


        new_task_button = findViewById(R.id.new_task_button);
        new_task_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.new_task_scrollview).setVisibility(View.VISIBLE);
                findViewById(R.id.main_task_scrollview).setVisibility(View.INVISIBLE);
            }
        });

        back_from_new_task_button = findViewById(R.id.back_from_new_task_button);
        back_from_new_task_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.new_task_scrollview).setVisibility(View.INVISIBLE);
                findViewById(R.id.main_task_scrollview).setVisibility(View.VISIBLE);
            }
        });

        create_task_button = findViewById(R.id.create_task_button);
        create_task_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText task_title_field = findViewById(R.id.task_title_field);
                EditText task_description_field = findViewById(R.id.task_description_field);
                EditText task_date_field = findViewById(R.id.task_date_field);
                EditText task_priority_field = findViewById(R.id.task_priority_field);

                SharedPreferences preferences = getSharedPreferences("BasicUserData", MODE_PRIVATE);
                int userId = preferences.getInt("user_id", -1);

                String tasksBefore = preferences.getString("tasks", "[]");

                CreateObjects new_task = new CreateObjects(TasksActivity.this);
                new_task.createTask(
                        task_title_field.getText().toString().trim(),
                        task_description_field.getText().toString().trim(),
                        task_date_field.getText().toString().trim(),
                        String.valueOf(userId),
                        task_priority_field.getText().toString().trim()
                );

                String tasksAfter = preferences.getString("tasks", "[]");

                if (tasksBefore.equals(tasksAfter)) {
                    TextView invalidData = findViewById(R.id.invalid_data);
                    invalidData.setVisibility(View.VISIBLE);
                    return;
                }

                findViewById(R.id.new_task_scrollview).setVisibility(View.INVISIBLE);
                findViewById(R.id.main_task_scrollview).setVisibility(View.VISIBLE);
                task_title_field.setText("");
                task_description_field.setText("");
                task_date_field.setText("");
                task_priority_field.setText("");
                recreate();

            }
        });

        back_to_initial = findViewById(R.id.back_to_initial);
        back_to_initial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TasksActivity.this, InitialScreenActivity.class);
                startActivity(intent);
            }
        });
    }
}