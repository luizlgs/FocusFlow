package com.example.focusflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;

import com.example.focusflow.api.Acess;

public class LoginScreenActivity extends AppCompatActivity {
    private ImageButton back_button;
    private Button login_in_button;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        back_button = findViewById(R.id.login_back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(LoginScreenActivity.this, MainActivity.class);
                startActivity(intent);
            }

        });

        login_in_button = findViewById(R.id.log_in_button);
        login_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String email = ((EditText) findViewById(R.id.login_email_field)).getText().toString();
                String pass = ((EditText) findViewById(R.id.login_pass_field)).getText().toString();
                Acess login = new Acess(LoginScreenActivity.this);
                if(login.sendLoginData(email, pass)){

                    findViewById(R.id.log_unseccessful).setVisibility(View.INVISIBLE);
                    findViewById(R.id.log_in_seccessful).setVisibility(View.VISIBLE);
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(LoginScreenActivity.this, InitialScreenActivity.class);
                            startActivity(intent);
                            finish(); // Opcional: fecha a tela de registro para o usuário não voltar nela

                            SharedPreferences preferences = getSharedPreferences("BasicUserData", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("current_session", "{}");
                            editor.apply();
                        }
                    }, 500);
                }
                else{
                    findViewById(R.id.log_unseccessful).setVisibility(View.VISIBLE);
                }
            }
        });



    }
}