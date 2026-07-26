package com.example.focusflow.api;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class ChangeObjectsStates {
    private Context context;
    public ChangeObjectsStates(Context context) {
        this.context = context;

    }

    public void endProject(String project_id, Runnable onComplete){
        JSONObject project_id_json = new JSONObject();

        try{
            project_id_json.put("id", project_id);
            DataRequests complete_project = new DataRequests(context);
            complete_project.sendData(project_id_json, "end_project", new DataRequests.OnDataReceived() {
                @Override
                public void onSuccess(JSONObject json) {
                    try{
                        System.out.println(json.getString("project_state"));
                        SharedPreferences preferences = context.getSharedPreferences("BasicUserData", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        String projectsString = preferences.getString("projects", "[]");
                        String currentProjectString = preferences.getString("current_project", "{}");

                        JSONArray projectsArray = new JSONArray(projectsString);
                        JSONObject currentProjectJson = new JSONObject(currentProjectString);

                        int projectId = json.getInt("id");

                        //começa do final do array pois o projeto sempre esta no final
                        //utilizei o for para caso haja algum erro e ele nao esteja exatamente no final
                        for (int i = projectsArray.length()-1; i >= 0; i--) {
                            JSONObject project = projectsArray.getJSONObject(i);

                            if (project.getInt("id") == projectId) {
                                project.put("project_state", json.getString("project_state"));
                                project.put("completion_date", json.getString("completion_date"));
                                break;
                            }
                        }

                        if (currentProjectJson.getInt("id") == projectId) {
                            currentProjectJson.put("project_state", json.getString("project_state"));
                            currentProjectJson.put("completion_date", json.getString("completion_date"));
                        }

                        editor.putString("projects", projectsArray.toString());
                        editor.putString("current_project", currentProjectJson.toString());
                        editor.apply();

                        onComplete.run();

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onError(String error) {

                }
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    public void endTask(String task_id, Runnable onComplete){
        JSONObject task_id_json = new JSONObject();

        try {
            task_id_json.put("id", task_id);

            DataRequests complete_task = new DataRequests(context);

            complete_task.sendData(task_id_json, "end_task", new DataRequests.OnDataReceived() {
                @Override
                public void onSuccess(JSONObject json) {
                    try {
                        System.out.println(json.toString());
                        SharedPreferences preferences = context.getSharedPreferences("BasicUserData", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        String tasksString = preferences.getString("tasks", "[]");
                        String currentTaskString = preferences.getString("current_task", "{}");

                        JSONArray tasksArray = new JSONArray(tasksString);
                        JSONObject currentTaskJson = new JSONObject(currentTaskString);


                        int taskId = json.getInt("id");
                        String newState = json.getString("task_state");

                        //começa do final do array pois a task sempre esta no final
                        //utilizei o for para caso haja algum erro e ele nao esteja exatamente no final
                        for(int i = tasksArray.length()-1; i >= 0; i--){

                            JSONObject task = tasksArray.getJSONObject(i);

                            if(task.getInt("id") == taskId){
                                task.put("task_state", newState);
                                task.put("completion_date", json.getString("completion_date"));
                                task.put("completion_time", json.getString("completion_time"));
                                break;
                            }
                        }
                        if(currentTaskJson.getInt("id") == taskId){
                            currentTaskJson.put("task_state", newState);
                            currentTaskJson.put("completion_date", json.getString("completion_date"));
                            currentTaskJson.put("completion_time", json.getString("completion_time"));
                        }
                        editor.putString("tasks", tasksArray.toString());
                        editor.putString("current_task", currentTaskJson.toString());

                        editor.apply();

                        onComplete.run();


                    } catch (JSONException e){

                        throw new RuntimeException(e);

                    }
                }
                @Override
                public void onError(String error) {
                }
            });
        } catch(JSONException e){

            throw new RuntimeException(e);
        }
    }

    public void endPomodoroSession(String session_id, int total_focus, String timer, Runnable onComplete){
        JSONObject session_id_json = new JSONObject();
        String total_focus_formatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", total_focus/3600, (total_focus%3600)/60, total_focus%60);
        try {
            session_id_json.put("id", session_id);
            session_id_json.put("total_focus", total_focus_formatted);
            session_id_json.put("timer", timer);

            DataRequests complete_session = new DataRequests(context);

            complete_session.sendData(session_id_json, "end_pomodoro_session", new DataRequests.OnDataReceived() {
                @Override
                public void onSuccess(JSONObject json) {
                    try {
                        SharedPreferences preferences = context.getSharedPreferences("BasicUserData", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        String sessionsString = preferences.getString("pomodorosessions", "[]");
                        String currentSessionString = preferences.getString("current_session", "{}");

                        JSONArray sessionsArray = new JSONArray(sessionsString);
                        JSONObject currentSessionJson = new JSONObject(currentSessionString);

                        int sessionId = json.getInt("id");

                        //começa do final do array pois a sessao sempre esta no final
                        //utilizei o for para caso haja algum erro e ele nao esteja exatamente no final
                        for (int i = sessionsArray.length()-1; i >= 0; i--) {
                            JSONObject session = sessionsArray.getJSONObject(i);

                            if (session.getInt("id") == sessionId) {
                                session.put("end_time", json.getString("end_time"));
                                session.put("date", json.getString("date"));
                                session.put("total_focus", json.getString("total_focus"));
                                session.put("timer", json.getString("timer"));
                                break;
                            }
                        }

                        if (currentSessionJson.getInt("id") == sessionId) {
                            currentSessionJson.put("end_time", json.getString("end_time"));
                            currentSessionJson.put("date", json.getString("date"));
                            currentSessionJson.put("total_focus", json.getString("total_focus"));
                            currentSessionJson.put("timer", json.getString("timer"));
                        }

                        editor.putString("pomodorosessions", sessionsArray.toString());
                        editor.putString("current_session", currentSessionJson.toString());
                        editor.apply();

                        onComplete.run();
                        System.out.println("-------------------"+currentSessionJson.toString());

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onError(String error) {
                }
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void standByPomodoro(String session_id, int total_focus, String timer, int small_pauses, int big_pauses, boolean is_pause){
        JSONObject session_data = new JSONObject();
        String total_focus_formatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", total_focus/3600, (total_focus%3600)/60, total_focus%60);
        try {
            session_data.put("id", session_id);
            session_data.put("total_focus", total_focus_formatted);
            session_data.put("timer", timer);
            session_data.put("small_pauses", String.valueOf(small_pauses));
            session_data.put("big_pauses", String.valueOf(big_pauses));
            session_data.put("is_pause", String.valueOf(is_pause)); // "true"/"false"

            DataRequests standby = new DataRequests(context);
            standby.sendData(session_data, "standby_pomodoro", new DataRequests.OnDataReceived() {
                @Override
                public void onSuccess(JSONObject json) {
                    try {
                        SharedPreferences preferences = context.getSharedPreferences("BasicUserData", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        String sessionsString = preferences.getString("pomodorosessions", "[]");
                        String currentSessionString = preferences.getString("current_session", "{}");

                        JSONArray sessionsArray = new JSONArray(sessionsString);
                        JSONObject currentSessionJson = new JSONObject(currentSessionString);

                        int sessionId = json.getInt("id");

                        for (int i = sessionsArray.length()-1; i >= 0; i--) {
                            JSONObject session = sessionsArray.getJSONObject(i);
                            if (session.getInt("id") == sessionId) {
                                session.put("timer", json.getString("timer"));
                                session.put("total_focus", json.getString("total_focus"));
                                session.put("small_pauses", json.getString("small_pauses"));
                                session.put("big_pauses", json.getString("big_pauses"));
                                session.put("is_pause", json.getString("is_pause"));
                                break;
                            }
                        }

                        if (currentSessionJson.has("id") && currentSessionJson.getInt("id") == sessionId) {
                            currentSessionJson.put("timer", json.getString("timer"));
                            currentSessionJson.put("total_focus", json.getString("total_focus"));
                            currentSessionJson.put("small_pauses", json.getString("small_pauses"));
                            currentSessionJson.put("big_pauses", json.getString("big_pauses"));
                            currentSessionJson.put("is_pause", json.getString("is_pause"));
                        }

                        editor.putString("pomodorosessions", sessionsArray.toString());
                        editor.putString("current_session", currentSessionJson.toString());
                        editor.apply();

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onError(String error) {
                }
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
