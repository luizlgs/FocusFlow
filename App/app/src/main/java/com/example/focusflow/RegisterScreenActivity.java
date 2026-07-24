package com.example.focusflow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focusflow.api.Acess;

public class RegisterScreenActivity extends AppCompatActivity {
    private ImageButton back_button;
    private Button register_button;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        back_button = findViewById(R.id.register_back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(RegisterScreenActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        register_button = findViewById(R.id.register_button);
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                EditText nameField = findViewById(R.id.register_name_field);
                String name = nameField.getText().toString();

                EditText emailField = findViewById(R.id.register_email_field);
                String email = emailField.getText().toString();

                EditText pass1Field = findViewById(R.id.register_pass_field1);
                String pass1 = pass1Field.getText().toString();

                EditText pass2Field = findViewById(R.id.register_pass_field2);
                String pass2 = pass2Field.getText().toString();

                EditText ageField = findViewById(R.id.register_age_field);
                String ageString = ageField.getText().toString();
                int age=0;
                if (!ageString.isEmpty()) {
                    age = Integer.parseInt(ageString);
                }

                Acess register = new Acess(RegisterScreenActivity.this);

                Log.d("TesteRegistro", "Dados lidos: " + name + " | " + email + " | " + age + " | " + pass1);
                if(register.sendRegistryData(name, email, age, pass1, pass2)){
                    findViewById(R.id.account_created).setVisibility(View.VISIBLE);
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Este código roda AUTOMATICAMENTE após 3 segundos
                            Intent intent = new Intent(RegisterScreenActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // Opcional: fecha a tela de registro para o usuário não voltar nela
                        }
                    }, 500);
                }
            }
        });
    }
}