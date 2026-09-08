package com.example.finmente;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finmente.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PerfilActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        session = new SessionManager(this);

        findViewById(R.id.btn_sair_perfil).setOnClickListener(v -> {
            session.logout();
            Intent intent = new Intent(PerfilActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btn_voltar_perfil).setOnClickListener(v -> finish());

        Button btnZerar = findViewById(R.id.btn_zerar_dados);
        if (btnZerar != null) {
            btnZerar.setOnClickListener(v -> confirmarExclusao());
        }
    }

    private void confirmarExclusao() {
        new AlertDialog.Builder(this)
                .setTitle("Zerar Dados")
                .setMessage("Deseja realmente apagar todos os seus registros? Esta ação não pode ser desfeita.")
                .setPositiveButton("Sim, Zerar", (dialog, which) -> zerarDados())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void zerarDados() {
        int userId = session.getUserId();
        if (userId == -1) return;

        try (Connection conn = ConexaoMySQL.conectar()) {
            if (conn == null) return;

            String[] tabelas = {"receitas", "despesas", "metas", "tarefas", "humor", "diario", "meu_momento"};

            for (String tabela : tabelas) {
                String sql = "DELETE FROM " + tabela + " WHERE usuario_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                stmt.executeUpdate();
                stmt.close();
            }

            Toast.makeText(this, "Dados zerados !", Toast.LENGTH_SHORT).show();
            finish();

        } catch (SQLException e) {
            Toast.makeText(this, "Erro ao zerar dados!", Toast.LENGTH_SHORT).show();
        }
    }
}
