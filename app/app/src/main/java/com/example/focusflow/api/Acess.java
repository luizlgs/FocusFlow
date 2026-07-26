package com.example.focusflow.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

public class Acess {
    Context context;
    public Acess(Context context){
        this.context = context;
    }

    public boolean sendLoginData(String email, String pass) {
        DataRequests login = new DataRequests(context);
        JSONObject user_data = new JSONObject();
        try {
            user_data.put("email", email);
            user_data.put("pass", pass);
        } catch (Exception e){
        Log.e("Erro", "erro ao adicionar atributos no novo json de task");
        }

        return login.sendData(user_data, "login", new DataRequests.OnDataReceived() {
            @Override
            public void onSuccess(JSONObject json) {
                try{
                    int receivedID = json.getInt("id");
                    String receivedName = json.getString("name");
                    String receivedEmail = json.getString("email");
                    int receivedAge = json.getInt("age");

                    String projects = json.getString("projects");

                    String tasks = json.getString("tasks");

                    String pomodorosessions = json.getString("pomodorosessions");

                    String token = json.getString("token");

                    SharedPreferences preferenciais = context.getSharedPreferences("BasicUserData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferenciais.edit();

                    //informacoes de login

                    editor.putInt("user_id", receivedID);
                    editor.putString("user_name", receivedName);
                    editor.putString("user_email", receivedEmail);
                    editor.putInt("user_age", receivedAge);
                    editor.putBoolean("is_logged_in", true);

                    //informacoes de projetos
                    editor.putString("projects", projects);

                    //informacoes de tasks
                    editor.putString("tasks", tasks);

                    //informacoes das pomodoro sessions
                    editor.putString("pomodorosessions", pomodorosessions);

                    //token de autenticacao
                    editor.putString("token", token);

                    Log.d("POMODOROINFO", pomodorosessions);

                    editor.apply();

                }catch (Exception e){
                    Log.e("Erro", "dados do usuário não recebidos");
                }
            }
            @Override
            public void onError(String error){
                Log.e("Login", error);
            }
        });
    }

    public boolean sendRegistryData(String name, String email, int age, String pass1, String pass2) {
        DataRequests register = new DataRequests(context);
        JSONObject user_data = new JSONObject();
        try {
            user_data.put("name", name);
            user_data.put("email", email);
            user_data.put("age", age);
            user_data.put("pass1", pass1);
            user_data.put("pass2", pass2);
        } catch (Exception e){
            Log.e("Erro", "erro ao adicionar atributos no novo json de task");
        }
        return register.sendData(user_data, "register", new DataRequests.OnDataReceived() {
            @Override
            public void onSuccess(JSONObject json) {
                Log.d("NewUser", "Novo Usuario adicionado");
            }

            @Override
            public void onError(String error) {
                Log.e("NewUser", "Erro ao adicionar novo usuario");
            }
        });
    }
}
