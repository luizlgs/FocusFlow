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

public class ProjectsActivity extends AppCompatActivity {
    private ImageButton new_project_button;
    private Button create_project_button;
    private ImageButton back_from_new_project_button;
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
        setContentView(R.layout.activity_projects);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        try{
            SharedPreferences preferences = getSharedPreferences("BasicUserData", ProjectsActivity.this.MODE_PRIVATE);
            String user_projects = preferences.getString("projects", "ProjectsNotFound");
            JSONArray projects_json = new JSONArray(user_projects);

            LinearLayout projectLayout = findViewById(R.id.projectlayout);

            for(int i=0; i<projects_json.length(); i++){
                final int index = i;
                Button newButton = (Button) getLayoutInflater().inflate(R.layout.botao_modelo, projectLayout, false);

                newButton.setId(View.generateViewId());

                newButton.setText(projects_json.getJSONObject(i).getString("title"));
                if(projects_json.getJSONObject(i).getString("project_state").equals("t"))
                    newButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#804CAF50")));
                else
                    newButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#80F44336")));


                projectLayout.addView(newButton);

                newButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("current_project", projects_json.getJSONObject(index).toString());
                            editor.apply();
                            recreate_ = false;
                            Intent intent = new Intent(ProjectsActivity.this, ProjectInfoActivity.class);
                            startActivity(intent);
                        } catch (Exception e){
                            Log.e("Erro", "nao foi possivel acessar o json de projetos");
                        }
                    }
                });

            }

        } catch (Exception e) {
            Log.e("Erro", "falha ao acesar os dados do json de projetos");
        }

        create_project_button = findViewById(R.id.create_project_button);
        create_project_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText project_title_field = findViewById(R.id.project_title_field);
                EditText project_description_field = findViewById(R.id.project_description_field);
                EditText project_delivery_date_field = findViewById(R.id.project_delivery_date_field);
                EditText project_members_field = findViewById(R.id.project_members_field);

                SharedPreferences preferences = getSharedPreferences("BasicUserData", MODE_PRIVATE);
                String ProjectsBefore = preferences.getString("projects", "[]");

                CreateObjects new_project = new CreateObjects(ProjectsActivity.this);
                int userId = preferences.getInt("user_id", -1);
                String user_name = preferences.getString("user_name", "NameNotFound");
                new_project.createProject(project_title_field.getText().toString().trim(),
                                          project_description_field.getText().toString().trim(),
                                          project_delivery_date_field.getText().toString().trim(),
                                          project_members_field.getText().toString().trim(),
                                          String.valueOf(userId),
                                          user_name);

                String ProjectsAfter = preferences.getString("projects", "[]");

                if (ProjectsBefore.equals(ProjectsAfter)) {
                    TextView invalidData = findViewById(R.id.invalid_data);
                    invalidData.setVisibility(View.VISIBLE);
                    return;
                }

                findViewById(R.id.new_project_scrollview).setVisibility(View.INVISIBLE);
                findViewById(R.id.main_scrollview_layout).setVisibility(View.VISIBLE);

                project_title_field.setText("");
                project_description_field.setText("");
                project_delivery_date_field.setText("");
                project_members_field.setText("");

                recreate();
            }
        });

        new_project_button = findViewById(R.id.new_project_button);
        new_project_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.new_project_scrollview).setVisibility(View.VISIBLE);
                findViewById(R.id.main_scrollview_layout).setVisibility(View.INVISIBLE);
            }
        });

        back_from_new_project_button = findViewById(R.id.back_from_new_project_button);
        back_from_new_project_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.new_project_scrollview).setVisibility(View.INVISIBLE);
                findViewById(R.id.main_scrollview_layout).setVisibility(View.VISIBLE);
            }
        });

        back_to_initial = findViewById(R.id.back_to_initial);
        back_to_initial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectsActivity.this, InitialScreenActivity.class);
                startActivity(intent);
            }
        });

    }
}