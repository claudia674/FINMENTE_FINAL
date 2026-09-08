package com.example.finmente;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finmente.R;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Locale;

public class MenuActivity extends AppCompatActivity {

    private TextView tvTotalEntradas, tvTotalSaidas, tvPorcentagemSaude;
    private com.google.android.material.progressindicator.CircularProgressIndicator progressFinanceiro;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        session = new SessionManager(this);
        if (session.getUserId() == -1) {
            finish();
            return;
        }

        tvTotalEntradas = findViewById(R.id.tvTotalEntradas);
        tvTotalSaidas = findViewById(R.id.tvTotalSaidas);
        tvPorcentagemSaude = findViewById(R.id.tvPorcentagemSaude);
        progressFinanceiro = findViewById(R.id.progressFinanceiro);

        setupMenuClick(R.id.menu_metas, MetasActivity.class);
        setupMenuClick(R.id.menu_alertas, AlertasActivity.class);
        setupMenuClick(R.id.btn_perfil, PerfilActivity.class);

        View btnLogout = findViewById(R.id.btn_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                session.logout();
                Toast.makeText(this, "Sessão encerrada", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        setupMenuClick(R.id.card_entradas, ReceitasActivity.class);
        setupMenuClick(R.id.card_saidas, DespesasActivity.class);
        setupMenuClick(R.id.menu_registro_tarefas, TarefasActivity.class);
        setupMenuClick(R.id.menu_autocuidado, MeuMomentoActivity.class);
        setupMenuClick(R.id.menu_diario, DiarioActivity.class);
        setupMenuClick(R.id.menu_fluxo, FluxoActivity.class);
        setupMenuClick(R.id.menu_relatorios, RelatoriosActivity.class);

        atualizarDashboard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarDashboard();
    }

    private void setupMenuClick(int viewId, Class<?> activityClass) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> {
                Intent intent = new Intent(MenuActivity.this, activityClass);
                startActivity(intent);
            });
        }
    }

    private void atualizarDashboard() {
        new Thread(() -> {
            try {
                Connection conn = ConexaoMySQL.conectar();
                if (conn == null) return;

                int userId = session.getUserId();
                Statement stmt = conn.createStatement();
                
                String sqlR = "SELECT SUM(valor) FROM receitas WHERE usuario_id = " + userId;
                ResultSet rsR = stmt.executeQuery(sqlR);
                double totalEntradas = 0;
                if (rsR.next()) totalEntradas = rsR.getDouble(1);
                rsR.close();

                String sqlD = "SELECT SUM(valor) FROM despesas WHERE usuario_id = " + userId;
                ResultSet rsD = stmt.executeQuery(sqlD);
                double totalSaidas = 0;
                if (rsD.next()) totalSaidas = rsD.getDouble(1);
                rsD.close();

                stmt.close();
                ConexaoMySQL.fecharConexao(conn);

                NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
                final double fEntradas = totalEntradas;
                final double fSaidas = totalSaidas;

                runOnUiThread(() -> {
                    tvTotalEntradas.setText(format.format(fEntradas));
                    tvTotalSaidas.setText(format.format(fSaidas));

                    if (fEntradas > 0) {
                        int saude = (int) Math.max(0, Math.min(100, ((fEntradas - fSaidas) / fEntradas) * 100));
                        progressFinanceiro.setProgress(saude);
                        tvPorcentagemSaude.setText(saude + "%");
                    } else {
                        progressFinanceiro.setProgress(0);
                        tvPorcentagemSaude.setText("0%");
                    }
                });

            } catch (SQLException e) {
                android.util.Log.e("MenuActivity", "Erro ao atualizar dashboard: " + e.getMessage());
            }
        }).start();
    }
}
