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
import java.sql.SQLException;

public class CadastroActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> cadastrarUsuario());
        }

        TextView tvAlreadyHaveAccount = findViewById(R.id.tv_already_have_account);
        if (tvAlreadyHaveAccount != null) {
            tvAlreadyHaveAccount.setOnClickListener(v -> finish());
        }
    }

    private void cadastrarUsuario() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Connection conn = ConexaoMySQL.conectar();
            if (conn == null) {
                Toast.makeText(this, "Erro de conexão!", Toast.LENGTH_SHORT).show();
                return;
            }

            String sql = "INSERT INTO usuarios (nome, email, senha) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);

            int res = stmt.executeUpdate();
            if (res > 0) {
                Toast.makeText(this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            }

            stmt.close();
            ConexaoMySQL.fecharConexao(conn);

        } catch (SQLException e) {
            android.util.Log.e("CadastroActivity", "Erro SQL: " + e.getMessage());
            Toast.makeText(this, "Erro ao cadastrar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
