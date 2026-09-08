package com.example.finmente;

import android.content.Intent;
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

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        TextView tvCreateAccount = findViewById(R.id.tv_create_account);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);
        Button btnLogin = findViewById(R.id.btn_login);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                validarLogin();
            });
        }

        if (tvCreateAccount != null) {
            tvCreateAccount.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CadastroActivity.class);
                startActivity(intent);
            });
        }

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RecuperarSenhaActivity.class);
                startActivity(intent);
            });
        }
    }

    private void validarLogin() {
        String email = etEmail.getText().toString().trim();
        String senha = etPassword.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        Button btnLogin = findViewById(R.id.btn_login);
        btnLogin.setEnabled(false);
        btnLogin.setText("Aguarde...");

        new Thread(() -> {
            Connection conn = null;
            try {
                conn = ConexaoMySQL.conectar();
                
                if (conn == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Erro: Não foi possível conectar ao banco!", Toast.LENGTH_LONG).show();
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Entrar");
                    });
                    return;
                }

                // Garante que as tabelas existem antes de tentar o login
                ConexaoMySQL.inicializarBanco(conn);

                String sql = "SELECT id FROM usuarios WHERE email = ? AND senha = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, email);
                stmt.setString(2, senha);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int userId = rs.getInt("id");
                    SessionManager session = new SessionManager(MainActivity.this);
                    session.setUserId(userId);

                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Bem-vindo!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "E-mail ou senha incorretos!", Toast.LENGTH_SHORT).show();
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Entrar");
                    });
                }

                rs.close();
                stmt.close();
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Entrar");
                });
            } finally {
                if (conn != null) {
                    ConexaoMySQL.fecharConexao(conn);
                }
            }
        }).start();
    }
}
