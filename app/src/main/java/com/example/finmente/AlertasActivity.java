package com.example.finmente;

import android.os.Bundle;
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
import java.util.ArrayList;

public class AlertasActivity extends AppCompatActivity {

    Button btnVoltarAlerta;
    ListView listaAlertas;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alertas);

        session = new SessionManager(this);

        btnVoltarAlerta = findViewById(R.id.btnVoltarAlerta);
        listaAlertas = findViewById(R.id.listaAlertas);

        btnVoltarAlerta.setOnClickListener(v -> finish());

        gerarAlertas();
    }

    private void gerarAlertas() {
        try {
            Connection conn = ConexaoMySQL.conectar();
            if (conn == null) return;

            int userId = session.getUserId();
            Statement stmt = conn.createStatement();
            
            ResultSet rsR = stmt.executeQuery("SELECT SUM(valor) as total FROM receitas WHERE usuario_id = " + userId);
            double totalReceitas = 0;
            if (rsR.next()) totalReceitas = rsR.getDouble("total");
            rsR.close();

            ResultSet rsD = stmt.executeQuery("SELECT SUM(valor) as total FROM despesas WHERE usuario_id = " + userId);
            double totalDespesas = 0;
            if (rsD.next()) totalDespesas = rsD.getDouble("total");
            rsD.close();

            ArrayList<String> alertas = new ArrayList<>();
            
            if (totalDespesas > totalReceitas) {
                alertas.add("⚠️ ATENÇÃO: Suas despesas (R$ " + totalDespesas + ") superaram suas receitas (R$ " + totalReceitas + ")!");
            } else {
                alertas.add("✅ Parabéns! Suas finanças estão equilibradas.");
            }
            
            ResultSet rsM = stmt.executeQuery("SELECT titulo, valor_alvo, valor_atual FROM metas WHERE usuario_id = " + userId);
            while (rsM.next()) {
                String nome = rsM.getString("titulo");
                double vm = rsM.getDouble("valor_alvo");
                double vg = rsM.getDouble("valor_atual");
                if (vg >= vm) {
                    alertas.add("🎯 META ALCANÇADA: " + nome + "!");
                }
            }
            rsM.close();

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this,
                    R.layout.list_item_generic,
                    R.id.tvCol2,
                    alertas
            ) {
                @Override
                public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                    android.view.View view = super.getView(position, convertView, parent);
                    TextView tvCol1 = view.findViewById(R.id.tvCol1);
                    TextView tvCol2 = view.findViewById(R.id.tvCol2);
                    TextView tvCol3 = view.findViewById(R.id.tvCol3);

                    String item = getItem(position);
                    
                    tvCol1.setText("ALERTA");
                    tvCol1.setTextColor(getResources().getColor(R.color.text_secondary));
                    
                    tvCol2.setText(item);
                    tvCol2.setTypeface(null, android.graphics.Typeface.BOLD);
                    
                    tvCol3.setText("INFO");
                    tvCol3.setTextColor(getResources().getColor(R.color.purple_primary));
                    
                    return view;
                }
            };
            listaAlertas.setAdapter(adapter);

            stmt.close();
            ConexaoMySQL.fecharConexao(conn);

        } catch (SQLException e) {
            Toast.makeText(this, "Erro ao gerar alertas!", Toast.LENGTH_SHORT).show();
        }
    }
}
