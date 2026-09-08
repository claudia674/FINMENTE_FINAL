package com.example.finmente;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finmente.R;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class RelatoriosActivity extends AppCompatActivity {

    private TextView tvEntradas, tvSaidas, tvSaldo;
    private ListView listaRelatorio;
    private Button btnVoltar;
    private SessionManager session;
    private NumberFormat format;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorios);

        session = new SessionManager(this);
        format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        tvEntradas = findViewById(R.id.tvRelatorioEntradas);
        tvSaidas = findViewById(R.id.tvRelatorioSaidas);
        tvSaldo = findViewById(R.id.tvRelatorioSaldo);
        listaRelatorio = findViewById(R.id.listaRelatorio);
        btnVoltar = findViewById(R.id.btnVoltarRelatorio);

        btnVoltar.setOnClickListener(v -> finish());

        findViewById(R.id.btn_add_receita_rel).setOnClickListener(v -> {
            Intent intent = new Intent(this, ReceitasActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btn_add_despesa_rel).setOnClickListener(v -> {
            Intent intent = new Intent(this, DespesasActivity.class);
            startActivity(intent);
        });

        carregarRelatorio();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarRelatorio();
    }

    private void carregarRelatorio() {
        try (Connection conn = ConexaoMySQL.conectar()) {
            if (conn == null) return;

            int userId = session.getUserId();
            Statement stmt = conn.createStatement();

            ResultSet rsR = stmt.executeQuery("SELECT SUM(valor) FROM receitas WHERE usuario_id = " + userId);
            double totalEntradas = 0;
            if (rsR.next()) totalEntradas = rsR.getDouble(1);
            rsR.close();

            ResultSet rsD = stmt.executeQuery("SELECT SUM(valor) FROM despesas WHERE usuario_id = " + userId);
            double totalSaidas = 0;
            if (rsD.next()) totalSaidas = rsD.getDouble(1);
            rsD.close();

            tvEntradas.setText(format.format(totalEntradas));
            tvSaidas.setText(format.format(totalSaidas));
            tvSaldo.setText(format.format(totalEntradas - totalSaidas));

            String sql = "SELECT 'RECEITA' as tipo, nome, valor, data FROM receitas WHERE usuario_id = " + userId +
                         " UNION ALL " +
                         "SELECT 'DESPESA' as tipo, nome, valor, data FROM despesas WHERE usuario_id = " + userId +
                         " ORDER BY data DESC";

            ResultSet rs = stmt.executeQuery(sql);
            ArrayList<Movimentacao> movimentacoes = new ArrayList<>();

            while (rs.next()) {
                movimentacoes.add(new Movimentacao(
                        rs.getString("tipo"),
                        rs.getString("nome"),
                        rs.getDouble("valor")
                ));
            }
            rs.close();

            RelatorioAdapter adapter = new RelatorioAdapter(movimentacoes);
            listaRelatorio.setAdapter(adapter);

        } catch (SQLException e) {
            Log.e("RelatoriosActivity", "Erro: " + e.getMessage());
            Toast.makeText(this, "Erro ao carregar relatório", Toast.LENGTH_SHORT).show();
        }
    }

    private static class Movimentacao {
        String tipo, nome;
        double valor;

        Movimentacao(String tipo, String nome, double valor) {
            this.tipo = tipo;
            this.nome = nome;
            this.valor = valor;
        }
    }

    private class RelatorioAdapter extends ArrayAdapter<Movimentacao> {
        RelatorioAdapter(ArrayList<Movimentacao> lista) {
            super(RelatoriosActivity.this, R.layout.list_item_generic, lista);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_generic, parent, false);
            }

            Movimentacao m = getItem(position);
            TextView tvCol1 = convertView.findViewById(R.id.tvCol1);
            TextView tvCol2 = convertView.findViewById(R.id.tvCol2);
            TextView tvCol3 = convertView.findViewById(R.id.tvCol3);

            tvCol1.setText(m.tipo);
            tvCol2.setText(m.nome);
            tvCol2.setTypeface(null, android.graphics.Typeface.BOLD);
            
            if (m.tipo.equals("RECEITA")) {
                tvCol1.setTextColor(getResources().getColor(R.color.text_secondary));
                tvCol3.setText(format.format(m.valor));
                tvCol3.setTextColor(getResources().getColor(R.color.green_positive));
            } else {
                tvCol1.setTextColor(getResources().getColor(R.color.text_secondary));
                tvCol3.setText("- " + format.format(m.valor));
                tvCol3.setTextColor(getResources().getColor(R.color.red_negative));
            }

            return convertView;
        }
    }
}
