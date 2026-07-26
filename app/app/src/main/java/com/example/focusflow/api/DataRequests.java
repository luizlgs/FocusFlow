package com.example.focusflow.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;



public class DataRequests {
    private AtomicBoolean post_stat = new AtomicBoolean(false);
    private AtomicBoolean get_stat = new AtomicBoolean(false);
    private Context context;

    public DataRequests(Context context) {
        this.context = context;
    }

    public interface OnDataReceived {
        void onSuccess(JSONObject json);
        void onError(String error);
    }


    public boolean sendData(JSONObject user_data, String dataType, OnDataReceived callback){
        Thread t = new Thread(() -> {
            try {
                SharedPreferences preferences = context.getSharedPreferences("BasicUserData", Context.MODE_PRIVATE);
                String token = preferences.getString("token", "");
                user_data.put("token", token);
                String user_data_string = user_data.toString();

                URL url = new URL("http://192.168.18.8:18080/"+dataType);
                HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
                conexao.setRequestMethod("POST");

                conexao.setRequestProperty("Content-Type", "application/json");
                conexao.setRequestProperty("Accept", "application/json");
                conexao.setDoOutput(true); // Diz que vamos enviar um "corpo" (body) na requisição

                // 4. Escreve os dados e envia para o servidor
                OutputStream os = conexao.getOutputStream();
                byte[] input = user_data_string.getBytes("utf-8");
                os.write(input, 0, input.length);
                os.flush(); // Empurra os dados pela rede
                os.close();

                int codigoResposta = conexao.getResponseCode();
                if (codigoResposta == HttpURLConnection.HTTP_OK) {
                    Log.d("Step2Server", "Dados de "+dataType+" enviados com sucesso!");
                    InputStream inputStream = conexao.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder textAnswer = new StringBuilder();
                    String linha;

                    while ((linha = reader.readLine()) != null) {
                        textAnswer.append(linha);
                    }
                    reader.close();
                    inputStream.close();

                    Log.d("RespostaServidor", textAnswer.toString());
                    JSONObject receivedJson= new JSONObject(textAnswer.toString());

                    callback.onSuccess(receivedJson);

                    post_stat.set(true);

                } else {
                    Log.e("Step2Server", "Erro ao enviar: Código " + codigoResposta);
                    post_stat.set(false);
                }

            } catch (Exception e) {
                Log.e("ServidorCrow", "Erro na requisição: " + e.getMessage());
                callback.onError("Falha: " + e.getMessage());
                post_stat.set(false);

            }
        });
        t.start();

        try {
            t.join(); // Faz a thread principal esperar a thread t acabar para retornar a situacao do request
        } catch (InterruptedException e) {
            Log.e("Erro", "Thread interrompida");
        }

        return post_stat.get();
    }

}
