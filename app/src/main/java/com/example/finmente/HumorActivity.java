package com.example.finmente;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finmente.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HumorActivity extends AppCompatActivity {

    Button btnVoltarHumor, btnSalvarHumor;
    RadioGroup radioHumor;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_humor);

        session = new SessionManager(this);

        btnVoltarHumor = findViewById(R.id.btnVoltarHumor);
        btnSalvarHumor = findViewById(R.id.btnSalvarHumor);
        radioHumor = findViewById(R.id.radioHumor);

        btnVoltarHumor.setOnClickListener(v -> finish());
        btnSalvarHumor.setOnClickListener(v -> salvarHumor());
    }

    private void salvarHumor() {
        int selectedId = radioHumor.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Selecione uma opção!", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton rb = findViewById(selectedId);
        String humor = rb.getText().toString();

        try {
            Connection conn = ConexaoMySQL.conectar();
            if (conn == null) return;

            String sql = "INSERT INTO humor (usuario_id, estado, data) VALUES (?, ?, CURDATE())";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, session.getUserId());
            stmt.setString(2, humor);

            stmt.executeUpdate();
            stmt.close();
            ConexaoMySQL.fecharConexao(conn);

            Toast.makeText(this, "Humor salvo!", Toast.LENGTH_SHORT).show();
        } catch (SQLException e) {
            Toast.makeText(this, "Erro ao salvar humor!", Toast.LENGTH_SHORT).show();
        }
    }
}
