package com.example.focusflow;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focusflow.api.Acess;

public class RegisterScreenActivity extends AppCompatActivity {
    private ImageButton back_button;
    private Button register_button;
    private TextView account_created;


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

                account_created = findViewById(R.id.account_created);
                int age=0;
                try {
                    if (!ageString.isEmpty()) {
                        age = Integer.parseInt(ageString);
                    }
                }catch (NumberFormatException e) {
                    account_created.setTextColor(Color.parseColor("#DC143C"));
                    account_created.setText("Dados de registro inválidos");
                    account_created.setVisibility(View.VISIBLE);
                    return;
                }

                Acess register = new Acess(RegisterScreenActivity.this);
                Log.d("TesteRegistro", "Dados lidos: " + name + " | " + email + " | " + age + " | " + pass1);
                if(register.sendRegistryData(name, email, age, pass1, pass2)){
                    account_created.setTextColor(Color.parseColor("#32CD32"));
                    account_created.setText("Conta criada com sucesso!");
                    account_created.setVisibility(View.VISIBLE);
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(RegisterScreenActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 200);
                }
                else{
                    account_created.setTextColor(Color.parseColor("#DC143C"));
                    account_created.setText("Dados de registro inválidos");
                    account_created.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}