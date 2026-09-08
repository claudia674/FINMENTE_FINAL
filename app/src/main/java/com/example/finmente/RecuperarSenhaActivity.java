package com.example.finmente;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finmente.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RecuperarSenhaActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendRecovery;
    private TextView tvBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_senha);

        etEmail = findViewById(R.id.et_email);
        btnSendRecovery = findViewById(R.id.btn_send_recovery);
        tvBack = findViewById(R.id.tv_back);

        if (btnSendRecovery != null) {
            btnSendRecovery.setOnClickListener(v -> recuperarSenha());
        }

        if (tvBack != null) {
            tvBack.setOnClickListener(v -> finish());
        }
    }

    private void recuperarSenha() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Digite seu e-mail!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Connection conn = ConexaoMySQL.conectar();
            if (conn == null) {
                Toast.makeText(this, "Erro de conexão!", Toast.LENGTH_SHORT).show();
                return;
            }

            String sql = "SELECT senha FROM usuarios WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String senha = rs.getString("senha");
                Toast.makeText(this, "Sua senha é: " + senha, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "E-mail não encontrado!", Toast.LENGTH_SHORT).show();
            }

            rs.close();
            stmt.close();
            ConexaoMySQL.fecharConexao(conn);

        } catch (SQLException e) {
            android.util.Log.e("RecuperarSenhaActivity", "Erro SQL: " + e.getMessage());
            Toast.makeText(this, "Erro ao recuperar senha!", Toast.LENGTH_SHORT).show();
        }
    }
}
