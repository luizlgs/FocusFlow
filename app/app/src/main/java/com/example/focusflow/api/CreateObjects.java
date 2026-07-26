package com.example.focusflow.api;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateObjects {

    private Context context;

    public CreateObjects(Context context) {
        this.context = context;

    }

    public void createTask(String title, String description, String task_date, String user_id, String priority){
        JSONObject new_task = new JSONObject();
        try{
            new_task.put("title", title);
            new_task.put("description", description);
            new_task.put("task_date", task_date);
            new_task.put("creator_id", user_id);
            new_task.put("priority", priority);

            DataRequests create_task = new DataRequests(context);
            boolean task_creation = create_task.sendData(new_task, "new_task", new DataRequests.OnDataReceived() {
                @Override
                public void onSuccess(JSONObject json) {
                    SharedPreferences preferences = context.getSharedPreferences("BasicUserData", context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    String tasksString = preferences.getString("tasks", "[]");

                    try {
                        JSONArray tasksArray = new JSONArray(tasksString);
                        JSONObject taskJson = new JSONObject();

                        // Altera o atributo
                        taskJson.put("id", json.getString("id"));
                        taskJson.put("title", title);
                        taskJson.put("description", description);
                        taskJson.put("task_date", task_date);
                        taskJson.put("user_id", user_id);
                        taskJson.put("priority", priority);
                        taskJson.put("task_state", "f");

                        tasksArray.put(taskJson);

                        editor.putString("tasks", tasksArray.toString());
                        editor.apply();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onError(String error) {
                    Log.e("CreateTask", "Erro ao criar tarefa: " + error);

                }
            });


        } catch (Exception e){
            Log.e("Erro", "erro ao adicionar atributos no novo json de task");
        }

    }

    public void createProject(String title, String description, String delivery_date, String members,String creator_name, String creator_id) {
        JSONObject new_project = new JSONObject();

        Date date = new Date();
        SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formated_date = formate.format(date);

        try {
            //dados do projeto que serão enviados pro servidor
            new_project.put("title", title);
            new_project.put("description", description);
            new_project.put("start_date", formated_date);
            new_project.put("delivery_date", delivery_date);
            new_project.put("members", members);
            new_project.put("creator_id", creator_id);

            DataRequests create_project = new DataRequests(context);
            boolean project_creation = create_project.sendData(new_project, "new_project", new DataRequests.OnDataReceived() {
                @Override
                public void onSuccess(JSONObject json) {
                    Log.d("CREATE_PROJECT", json.toString());
                    SharedPreferences preferences = context.getSharedPreferences("BasicUserData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    String projectsString = preferences.getString("projects", "[]");

                    try {
                        JSONArray projectsArray = new JSONArray(projectsString);
                        JSONObject projectJson = new JSONObject();

                        projectJson.put("id", json.getInt("id"));
                        projectJson.put("title", title);
                        projectJson.put("description", description);
                        projectJson.put("start_date", formated_date);
                        projectJson.put("delivery_date", delivery_date);
                        projectJson.put("creator_id", creator_id);
                        projectJson.put("creator_name", creator_name);
                        projectJson.put("project_state", "f"); //projeto inicializado como "em andamento"

                        projectJson.put("members", json.getJSONArray("members")); //membros recebidos do servidor por um array de json com chaves nome e email

                        projectsArray.put(projectJson);

                        editor.putString("projects", projectsArray.toString());
                        editor.apply();
                        Log.d("SAVED_PROJECTS", projectsArray.toString());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e("CreateProject", "Erro ao criar projeto: " + error);
                }
            });

        } catch (Exception e) {
            Log.e("Erro", "Erro ao adicionar atributos no novo JSON de projeto");
        }
    }

    public void createPomodoroSession(String session_title, String session_description, String session_shortpause, String session_bigpause, String session_blocks, String creator_id){
        JSONObject new_pomodoro = new JSONObject();

        try{
            new_pomodoro.put("title", session_title);
            new_pomodoro.put("description", session_description);
            new_pomodoro.put("short_pause", session_shortpause);
            new_pomodoro.put("big_pause", session_bigpause);
            new_pomodoro.put("blocks", session_blocks);
            new_pomodoro.put("creator_id", creator_id);

            DataRequests create_pomodoro = new DataRequests(context);
            create_pomodoro.sendData(new_pomodoro, "new_pomodoro", new DataRequests.OnDataReceived() {
                @Override
                public void onSuccess(JSONObject json) {
                    SharedPreferences preferences = context.getSharedPreferences("BasicUserData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    String sessionsString = preferences.getString("pomodorosessions", "[]");

                    try {
                        JSONArray sessionsArray = new JSONArray(sessionsString);
                        JSONObject sessionJson = new JSONObject();

                        sessionJson.put("id", json.getInt("id"));
                        sessionJson.put("title", session_title);
                        sessionJson.put("description", session_description);
                        sessionJson.put("blocks", session_blocks);
                        sessionJson.put("short_pause", session_shortpause);
                        sessionJson.put("big_pause", session_bigpause);
                        sessionJson.put("user_id", creator_id);
                        sessionJson.put("date", json.getString("date"));
                        sessionJson.put("start_time", json.getString("start_time"));
                        sessionJson.put("total_focus", json.getString("total_focus"));
                        sessionJson.put("timer", "");
                        sessionJson.put("end_time", "");

                        sessionsArray.put(sessionJson);

                        editor.putString("pomodorosessions", sessionsArray.toString());
                        editor.putString("current_session", sessionJson.toString());
                        editor.apply();
                        Log.d("SAVED_POMODOROSESSIONS", sessionsArray.getJSONObject(sessionsArray.length()-1).toString());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onError(String error) {
                    Log.e("CreatePomodoroSession", "Erro ao criar a sessao: " + error);
                }
            });

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
