package com.example.focusflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;

import com.example.focusflow.api.ChangeObjectsStates;

public class ProjectInfoActivity extends AppCompatActivity {
    private Button members;
    int appearNumber = 0;
    private ImageButton back_from_members_button;
    private ImageButton conclude_project;
    private ImageButton back_to_projects;
    boolean concluded;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_project_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences preferences = getSharedPreferences("BasicUserData", ProjectInfoActivity.this.MODE_PRIVATE);
        String user_project = preferences.getString("current_project", "ProjectNotFound");
        try {

            JSONObject project_json = new JSONObject(user_project);

            //Instancia as informacoes do projeto na tela com uma cor diferente do tipo do atributo
            TextView projetct_title = findViewById(R.id.project_title_text);
            projetct_title.setText(project_json.getString("title"));

            TextView project_id = findViewById(R.id.project_id_text);
            String projectId = project_json.getString("id");
            SpannableString textoProjectId = new SpannableString(projectId);
            textoProjectId.setSpan(new ForegroundColorSpan(Color.parseColor("#80000000")), 0, textoProjectId.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            project_id.append(textoProjectId);

            TextView project_creator = findViewById(R.id.project_creator_text);
            String creatorName = project_json.getString("creator_name");
            SpannableString textoCreatorName = new SpannableString(creatorName);
            textoCreatorName.setSpan(new ForegroundColorSpan(Color.parseColor("#80000000")), 0, textoCreatorName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            project_creator.append(textoCreatorName);

            TextView projetct_description = findViewById(R.id.project_description_text);
            String description = project_json.getString("description");
            SpannableString textoDescription = new SpannableString(description);
            textoDescription.setSpan(new ForegroundColorSpan(Color.parseColor("#80000000")), 0, textoDescription.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            projetct_description.append(textoDescription);

            TextView projetct_start_date = findViewById(R.id.project_start_date);
            String startDate = project_json.getString("start_date");
            SpannableString textoStartDate = new SpannableString(startDate);
            textoStartDate.setSpan(new ForegroundColorSpan(Color.parseColor("#80000000")), 0, textoStartDate.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            projetct_start_date.append(textoStartDate);

            TextView projetct_delivery_date= findViewById(R.id.project_delivery_date);
            String deliveryDate = project_json.getString("delivery_date");
            SpannableString textoDeliveryDate = new SpannableString(deliveryDate);
            textoDeliveryDate.setSpan(new ForegroundColorSpan(Color.parseColor("#80000000")), 0, textoDeliveryDate.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            projetct_delivery_date.append(textoDeliveryDate);

            TextView projetct_state= findViewById(R.id.project_state);
            String projectState = project_json.getString("project_state");

            TextView project_completion_date = findViewById(R.id.project_completion_date);

            if(project_json.getString("project_state").equals("t")) {
                String completionDate = project_json.getString("completion_date");

                SpannableString textoCompletionDate = new SpannableString(completionDate);
                textoCompletionDate.setSpan(
                        new ForegroundColorSpan(Color.parseColor("#80000000")),
                        0,
                        textoCompletionDate.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );

                project_completion_date.append(textoCompletionDate);

            } else {
                project_completion_date.setVisibility(View.GONE);
            }

            if(projectState.equals("f")){
                projectState = "Em andamento";
                concluded = false;
            }
            else{
                projectState = "Finalizado";
                concluded = true;
            }
            SpannableString textoProjectState = new SpannableString(projectState);
            textoProjectState.setSpan(new ForegroundColorSpan(Color.parseColor("#80000000")), 0, textoProjectState.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            projetct_state.append(textoProjectState);

            JSONArray members_json = project_json.getJSONArray("members");
            members = findViewById(R.id.members_button);
            members.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        try {
                            if(appearNumber == 0) {
                                for (int i = 0; i < members_json.length(); i++) {
                                    final int index = i;
                                    String current_member_name = members_json.getJSONObject(index).getString("name");
                                    String current_member_email = members_json.getJSONObject(index).getString("email");

                                    LinearLayout projectLayout = findViewById(R.id.memebers_layout);

                                    TextView current_member_text = new TextView(ProjectInfoActivity.this);
                                    current_member_text.setId(View.generateViewId());
                                    current_member_text.setText("Nome: " + current_member_name + "\nEmail:" + current_member_email);

                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                    );

                                    current_member_text.setTextColor(getResources().getColor(android.R.color.black));
                                    params.setMargins(0, 8, 0, 16);
                                    current_member_text.setLayoutParams(params);
                                    projectLayout.addView(current_member_text);

                                }
                                appearNumber++;
                            }

                            ScrollView members_layout = findViewById(R.id.memebers_scrollview);
                            members_layout.setVisibility(View.VISIBLE);
                            findViewById(R.id.main_project_layout).setVisibility(View.INVISIBLE);


                        } catch (Exception e) {
                            Log.e("Erro", "nao foi possivel acessar o json de membros do projeto");
                        }

                }
            });

            back_from_members_button = findViewById(R.id.back_from_members_button);
            back_from_members_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findViewById(R.id.memebers_scrollview).setVisibility(View.INVISIBLE);
                    findViewById(R.id.main_project_layout).setVisibility(View.VISIBLE);
                }
            });

        } catch(Exception e) {
            Log.e("Erro", "nao foi possivel acessar o json de projetos");
        }

        conclude_project =  findViewById(R.id.conclude_project);
        if(concluded)
            conclude_project.setImageResource(R.drawable.botao_nconcluir);
        else
            conclude_project.setImageResource(R.drawable.botao_concluir);

        conclude_project = findViewById(R.id.conclude_project);
        conclude_project.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeObjectsStates complete_project = new ChangeObjectsStates(ProjectInfoActivity.this);
                try{
                    JSONObject project_json = new JSONObject(user_project);
                    complete_project.endProject(
                            project_json.getString("id"),
                            () -> runOnUiThread(() -> recreate())
                    );


                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        back_to_projects = findViewById(R.id.back_to_projects);
        back_to_projects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectInfoActivity.this, ProjectsActivity.class);
                startActivity(intent);
            }
        });

    }
}