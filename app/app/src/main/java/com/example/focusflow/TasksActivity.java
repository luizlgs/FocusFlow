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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focusflow.api.CreateObjects;
import com.example.focusflow.api.DeleteObjects;

import org.json.JSONArray;

public class TasksActivity extends AppCompatActivity {
    private ImageButton new_task_button;
    private ImageButton back_from_new_task_button;
    private ImageButton back_to_initial;

    private ImageButton delete_task_icon_button;
    private ImageButton back_from_delete_task_button;

    private Button create_task_button;
    private Button delete_task_button;

    private boolean recreate_ = true;

    private LinearLayout taskslayout;

    private EditText task_title_field;
    private EditText task_description_field;
    private EditText task_date_field;
    private EditText task_priority_field;
    private EditText task_delete_id_field;

    private ScrollView delete_task_scrollview;
    private TextView delete_task_error;


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

            taskslayout = findViewById(R.id.taskslayout);

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
                task_title_field = findViewById(R.id.task_title_field);
                task_description_field = findViewById(R.id.task_description_field);
                task_date_field = findViewById(R.id.task_date_field);
                task_priority_field = findViewById(R.id.task_priority_field);

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


        delete_task_icon_button = findViewById(R.id.delete_task_icon_button);
        delete_task_scrollview = findViewById(R.id.delete_task_scrollview);
        task_delete_id_field = findViewById(R.id.task_delete_id_field);
        delete_task_button = findViewById(R.id.delete_task_button);
        delete_task_error = findViewById(R.id.delete_task_error);

        delete_task_icon_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.main_task_scrollview).setVisibility(View.INVISIBLE);
                delete_task_scrollview.setVisibility(View.VISIBLE);
            }
        });

        back_from_delete_task_button = findViewById(R.id.back_from_delete_task_button);
        back_from_delete_task_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete_task_scrollview.setVisibility(View.INVISIBLE);
                findViewById(R.id.main_task_scrollview).setVisibility(View.VISIBLE);
            }
        });

        delete_task_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete_task_error.setVisibility(View.INVISIBLE);

                String typed_id_str = task_delete_id_field.getText().toString().trim();

                if (typed_id_str.isEmpty()) {
                    delete_task_error.setText("Digite o ID da tarefa");
                    delete_task_error.setVisibility(View.VISIBLE);
                    return;
                }

                int typed_id;
                try {
                    typed_id = Integer.parseInt(typed_id_str);
                } catch (NumberFormatException e) {
                    delete_task_error.setText("ID inválido");
                    delete_task_error.setVisibility(View.VISIBLE);
                    return;
                }

                SharedPreferences preferences = getSharedPreferences("BasicUserData", MODE_PRIVATE);
                String tasks = preferences.getString("tasks", "[]");

                try {
                    JSONArray tasks_json = new JSONArray(tasks);
                    boolean id_exists = false;

                    for (int i = 0; i < tasks_json.length(); i++) {
                        if (tasks_json.getJSONObject(i).getInt("id") == typed_id) {
                            id_exists = true;
                            break;
                        }
                    }

                    if (!id_exists) {
                        delete_task_error.setText("Nenhuma tarefa encontrada com esse ID");
                        delete_task_error.setVisibility(View.VISIBLE);
                        return;
                    }

                    String tasksBefore = preferences.getString("tasks", "[]");

                    DeleteObjects deleteObjects = new DeleteObjects(TasksActivity.this);
                    deleteObjects.deleteTask(String.valueOf(typed_id));

                    String tasksAfter = preferences.getString("tasks", "[]");
                    if (tasksBefore.equals(tasksAfter)) {
                        delete_task_error.setText("Não foi possível apagar a tarefa");
                        delete_task_error.setVisibility(View.VISIBLE);
                        return;
                    }

                    task_delete_id_field.setText("");
                    delete_task_scrollview.setVisibility(View.INVISIBLE);
                    findViewById(R.id.main_task_scrollview).setVisibility(View.VISIBLE);
                    recreate();

                } catch (org.json.JSONException e) {
                    delete_task_error.setText("Erro ao verificar tarefas");
                    delete_task_error.setVisibility(View.VISIBLE);
                }
            }
        });

    }
}