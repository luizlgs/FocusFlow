package com.example.focusflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProfileActivity extends AppCompatActivity {
    private Button logout_button;
    private ImageButton back_to_initial;
    private TextView welcome_text;
    private TextView id_text;
    private TextView name_text;
    private TextView email_text;
    private TextView age_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // carrega as informacoes basicas do usuario salvos no login
        SharedPreferences preferences = getSharedPreferences("BasicUserData", ProfileActivity.this.MODE_PRIVATE);

        int user_id = preferences.getInt("user_id", -1);
        String user_name = preferences.getString("user_name", "UserNotFound");
        String user_email = preferences.getString("user_email", "EmailNotFound");
        int user_age = preferences.getInt("user_age", -1);

        welcome_text = findViewById(R.id.welcometext);
        welcome_text.setText("Hello "+user_name.split(" ")[0]+"!");

        id_text = findViewById(R.id.user_id_text);
        id_text.setText("ID: "+user_id);

        name_text = findViewById(R.id.user_name_text);
        name_text.setText("Nome: "+user_name);

        email_text = findViewById(R.id.user_email_text);
        email_text.setText("Email: "+user_email);

        age_text = findViewById(R.id.user_age_text);
        age_text.setText("Idade: "+user_age);

        logout_button = findViewById(R.id.logoutbutton);
        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //limpa os dados do usuarios salvos localmente
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.apply();

                //vai para a tela de login/registro
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        back_to_initial = findViewById(R.id.back_to_initial);
        back_to_initial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, InitialScreenActivity.class);
                startActivity(intent);
            }
        });

    }
}