package com.example.finmente;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finmente.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DespesasActivity extends AppCompatActivity {

    Button btnVoltar, btnSalvar, btnConsultar, btnAlterar, btnExcluir, btnLimpar;
    EditText edtDescricao, edtValor, edtData;
    ListView listaDespesas;
    int despesaSelecionadaId = -1;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_despesas);

        session = new SessionManager(this);

        btnVoltar = findViewById(R.id.btnVoltar);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnConsultar = findViewById(R.id.btnConsultar);
        btnAlterar = findViewById(R.id.btnAlterar);
        btnExcluir = findViewById(R.id.btnExcluir);
        btnLimpar = findViewById(R.id.btnLimpar);
        edtDescricao = findViewById(R.id.edtDescricao);
        edtValor = findViewById(R.id.edtValor);
        edtData = findViewById(R.id.edtDataDespesa);
        listaDespesas = findViewById(R.id.listaDespesas);

        edtData.setOnClickListener(v -> {
            android.app.DatePickerDialog dp = new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                edtData.setText(String.format(java.util.Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth));
            }, java.util.Calendar.getInstance().get(java.util.Calendar.YEAR), 
               java.util.Calendar.getInstance().get(java.util.Calendar.MONTH), 
               java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH));
            dp.show();
        });

        btnVoltar.setOnClickListener(v -> finish());
        btnSalvar.setOnClickListener(v -> cadastrarDespesa());
        btnConsultar.setOnClickListener(v -> consultarDespesas());
        btnAlterar.setOnClickListener(v -> alterarDespesa());
        btnExcluir.setOnClickListener(v -> excluirDespesa());
        btnLimpar.setOnClickListener(v -> {
            despesaSelecionadaId = -1;
            edtDescricao.setText("");
            edtValor.setText("");
            edtData.setText("");
            Toast.makeText(this, "Campos limpos!", Toast.LENGTH_SHORT).show();
        });
    }

    private void cadastrarDespesa() {
        String nome = edtDescricao.getText().toString().trim();
        String valorStr = edtValor.getText().toString().trim();
        String data = edtData.getText().toString().trim();

        if (nome.isEmpty() || valorStr.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double valor = Double.parseDouble(valorStr);
            Connection conn = ConexaoMySQL.conectar();
            if (conn == null) {
                Toast.makeText(this, "Falha na conexão!", Toast.LENGTH_SHORT).show();
                return;
            }

            String sql = "INSERT INTO despesas (usuario_id, nome, valor, data) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, session.getUserId());
            stmt.setString(2, nome);
            stmt.setDouble(3, valor);
            
            if (data.isEmpty()) {
                stmt.setDate(4, new java.sql.Date(System.currentTimeMillis()));
            } else {
                stmt.setString(4, data);
            }

            stmt.executeUpdate();
            stmt.close();
            ConexaoMySQL.fecharConexao(conn);

            Toast.makeText(this, "Despesa cadastrada!", Toast.LENGTH_SHORT).show();
            edtDescricao.setText("");
            edtValor.setText("");
            edtData.setText("");
            consultarDespesas();

        } catch (Exception e) {
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void consultarDespesas() {
        try {
            Connection conn = ConexaoMySQL.conectar();
            if (conn == null) return;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, nome, valor FROM despesas WHERE usuario_id = " + session.getUserId());

            ArrayList<String> lista = new ArrayList<>();
            ArrayList<Integer> ids = new ArrayList<>();

            while (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                double valor = rs.getDouble("valor");
                ids.add(id);
                lista.add(id + " - " + nome + " - R$ " + valor);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this,
                    R.layout.list_item_generic,
                    R.id.tvCol2,
                    lista
            ) {
                @Override
                public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                    android.view.View view = super.getView(position, convertView, parent);
                    TextView tvCol1 = view.findViewById(R.id.tvCol1);
                    TextView tvCol2 = view.findViewById(R.id.tvCol2);
                    TextView tvCol3 = view.findViewById(R.id.tvCol3);

                    String item = getItem(position);
                    String[] partes = item.split(" - ");

                    tvCol1.setText("DESPESA");
                    tvCol1.setTextColor(getResources().getColor(R.color.text_secondary));

                    tvCol2.setText(partes[1]);
                    tvCol2.setTypeface(null, android.graphics.Typeface.BOLD);

                    tvCol3.setText("- " + partes[2]);
                    tvCol3.setTextColor(getResources().getColor(R.color.red_negative));

                    return view;
                }
            };
            listaDespesas.setAdapter(adapter);

            listaDespesas.setOnItemClickListener((parent, view, position, id) -> {
                despesaSelecionadaId = ids.get(position);
                String item = lista.get(position);
                String[] partes = item.split(" - ");
                edtDescricao.setText(partes[1]);
                edtValor.setText(partes[2].replace("R$ ", ""));
                Toast.makeText(this, "Selecionado!", Toast.LENGTH_SHORT).show();
            });

            rs.close();
            stmt.close();
            ConexaoMySQL.fecharConexao(conn);
        } catch (SQLException e) {
            Toast.makeText(this, "Erro ao consultar!", Toast.LENGTH_SHORT).show();
        }
    }

    private void alterarDespesa() {
        if (despesaSelecionadaId == -1) return;

        String nome = edtDescricao.getText().toString().trim();
        String valorStr = edtValor.getText().toString().trim();

        try {
            double valor = Double.parseDouble(valorStr);
            Connection conn = ConexaoMySQL.conectar();
            if (conn == null) return;

            String sql = "UPDATE despesas SET nome = ?, valor = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nome);
            stmt.setDouble(2, valor);
            stmt.setInt(3, despesaSelecionadaId);

            stmt.executeUpdate();
            stmt.close();
            ConexaoMySQL.fecharConexao(conn);

            Toast.makeText(this, "Alterado!", Toast.LENGTH_SHORT).show();
            despesaSelecionadaId = -1;
            edtDescricao.setText("");
            edtValor.setText("");
            consultarDespesas();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao alterar!", Toast.LENGTH_SHORT).show();
        }
    }

    private void excluirDespesa() {
        if (despesaSelecionadaId == -1) return;
        try {
            Connection conn = ConexaoMySQL.conectar();
            if (conn == null) return;
            String sql = "DELETE FROM despesas WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, despesaSelecionadaId);
            stmt.executeUpdate();
            stmt.close();
            ConexaoMySQL.fecharConexao(conn);
            Toast.makeText(this, "Excluído!", Toast.LENGTH_SHORT).show();
            despesaSelecionadaId = -1;
            edtDescricao.setText("");
            edtValor.setText("");
            consultarDespesas();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao excluir!", Toast.LENGTH_SHORT).show();
        }
    }
}
