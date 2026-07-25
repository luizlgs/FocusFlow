package com.example.focusflow.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DeleteObjects {
    private Context context;

    public DeleteObjects(Context context) {
        this.context = context;
    }

    public void deletePomodoroSession(String session_id) {
        JSONObject session_id_json = new JSONObject();

        try {
            session_id_json.put("id", session_id);

            DataRequests delete_session = new DataRequests();

            delete_session.sendData(session_id_json, "delete_pomodoro_session", new DataRequests.OnDataReceived() {
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

                        JSONArray updatedSessionsArray = new JSONArray();
                        for (int i = 0; i < sessionsArray.length(); i++) {
                            JSONObject session = sessionsArray.getJSONObject(i);
                            if (session.getInt("id") != sessionId) {
                                updatedSessionsArray.put(session);
                            }
                        }

                        if (currentSessionJson.has("id") && currentSessionJson.getInt("id") == sessionId) {
                            currentSessionJson = new JSONObject();
                        }

                        editor.putString("pomodorosessions", updatedSessionsArray.toString());
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

    public void deleteTask(String task_id) {
        JSONObject task_id_json = new JSONObject();

        try {
            task_id_json.put("id", task_id);

            DataRequests delete_task = new DataRequests();

            delete_task.sendData(task_id_json, "delete_task", new DataRequests.OnDataReceived() {
                @Override
                public void onSuccess(JSONObject json) {
                    try {
                        SharedPreferences preferences = context.getSharedPreferences("BasicUserData", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        String tasksString = preferences.getString("tasks", "[]");
                        String currentTaskString = preferences.getString("current_task", "{}");

                        JSONArray tasksArray = new JSONArray(tasksString);
                        JSONObject currentTaskJson = new JSONObject(currentTaskString);

                        int taskId = json.getInt("id");

                        JSONArray updatedTasksArray = new JSONArray();
                        for (int i = 0; i < tasksArray.length(); i++) {
                            JSONObject task = tasksArray.getJSONObject(i);
                            if (task.getInt("id") != taskId) {
                                updatedTasksArray.put(task);
                            }
                        }

                        if (currentTaskJson.has("id") && currentTaskJson.getInt("id") == taskId) {
                            currentTaskJson = new JSONObject();
                        }

                        editor.putString("tasks", updatedTasksArray.toString());
                        editor.putString("current_task", currentTaskJson.toString());
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

    public void deleteProject(String project_id) {
        JSONObject project_id_json = new JSONObject();

        try {
            project_id_json.put("id", project_id);

            DataRequests delete_project = new DataRequests();

            delete_project.sendData(project_id_json, "delete_project", new DataRequests.OnDataReceived() {
                @Override
                public void onSuccess(JSONObject json) {
                    try {
                        SharedPreferences preferences = context.getSharedPreferences("BasicUserData", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        String projectsString = preferences.getString("projects", "[]");
                        String currentProjectString = preferences.getString("current_project", "{}");

                        JSONArray projectsArray = new JSONArray(projectsString);
                        JSONObject currentProjectJson = new JSONObject(currentProjectString);

                        int projectId = json.getInt("id");

                        JSONArray updatedProjectsArray = new JSONArray();
                        for (int i = 0; i < projectsArray.length(); i++) {
                            JSONObject project = projectsArray.getJSONObject(i);
                            if (project.getInt("id") != projectId) {
                                updatedProjectsArray.put(project);
                            }
                        }

                        if (currentProjectJson.has("id") && currentProjectJson.getInt("id") == projectId) {
                            currentProjectJson = new JSONObject();
                        }

                        editor.putString("projects", updatedProjectsArray.toString());
                        editor.putString("current_project", currentProjectJson.toString());
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
