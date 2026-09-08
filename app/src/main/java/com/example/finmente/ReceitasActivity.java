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

public class ReceitasActivity extends AppCompatActivity {

    Button btnVoltar, btnCadastrarReceita, btnConsultar, btnAlterarReceita, btnExcluirReceita, btnLimparReceita;
    EditText edtNomeReceita, edtValorReceita, edtDataReceita;
    ListView listaReceitas;
    int receitaSelecionadaId = -1;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receitas);

        session = new SessionManager(this);

        btnVoltar = findViewById(R.id.btnVoltar);
        btnCadastrarReceita = findViewById(R.id.btnCadastrarReceita);
        btnConsultar = findViewById(R.id.btnConsultar);
        btnAlterarReceita = findViewById(R.id.btnAlterarReceita);
        btnExcluirReceita = findViewById(R.id.btnExcluirReceita);
        btnLimparReceita = findViewById(R.id.btnLimparReceita);

        edtNomeReceita = findViewById(R.id.edtNomeReceita);
        edtValorReceita = findViewById(R.id.edtValorReceita);
        edtDataReceita = findViewById(R.id.edtDataReceita);

        listaReceitas = findViewById(R.id.listaReceitas);

        edtDataReceita.setOnClickListener(v -> {
            android.app.DatePickerDialog dp = new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                edtDataReceita.setText(String.format(java.util.Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth));
            }, java.util.Calendar.getInstance().get(java.util.Calendar.YEAR), 
               java.util.Calendar.getInstance().get(java.util.Calendar.MONTH), 
               java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH));
            dp.show();
        });

        btnVoltar.setOnClickListener(v -> finish());
        btnCadastrarReceita.setOnClickListener(v -> cadastrarReceita());
        btnConsultar.setOnClickListener(v -> consultarReceitas());
        btnAlterarReceita.setOnClickListener(v -> alterarReceita());
        btnExcluirReceita.setOnClickListener(v -> excluirReceita());
        btnLimparReceita.setOnClickListener(v -> {
            receitaSelecionadaId = -1;
            edtNomeReceita.setText("");
            edtValorReceita.setText("");
            edtDataReceita.setText("");
            Toast.makeText(this, "Campos limpos!", Toast.LENGTH_SHORT).show();
        });
    }

    private void excluirReceita() {
        if (receitaSelecionadaId == -1) {
            Toast.makeText(this, "Selecione uma receita na lista!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Connection conn = ConexaoMySQL.conectar();
            if (conn == null) return;

            String sql = "DELETE FROM receitas WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, receitaSelecionadaId);

            int res = stmt.executeUpdate();
            if (res > 0) {
                Toast.makeText(this, "Receita excluída!", Toast.LENGTH_SHORT).show();
                receitaSelecionadaId = -1;
                edtNomeReceita.setText("");
                edtValorReceita.setText("");
                consultarReceitas();
            }

            stmt.close();
            ConexaoMySQL.fecharConexao(conn);
        } catch (SQLException e) {
            Toast.makeText(this, "Erro ao excluir!", Toast.LENGTH_SHORT).show();
        }
    }

    private void cadastrarReceita() {
        String nome = edtNomeReceita.getText().toString().trim();
        String valorStr = edtValorReceita.getText().toString().trim();
        String data = edtDataReceita.getText().toString().trim();

        if (nome.isEmpty() || valorStr.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double valor = Double.parseDouble(valorStr);
            Connection conn = ConexaoMySQL.conectar();
            if (conn == null) return;

            String sql = "INSERT INTO receitas (usuario_id, nome, valor, data) VALUES (?, ?, ?, ?)";
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

            Toast.makeText(this, "Receita cadastrada!", Toast.LENGTH_SHORT).show();
            edtNomeReceita.setText("");
            edtValorReceita.setText("");
            edtDataReceita.setText("");
            consultarReceitas();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao cadastrar!", Toast.LENGTH_SHORT).show();
        }
    }

    private void consultarReceitas() {
        try {
            Connection conn = ConexaoMySQL.conectar();
            if (conn == null) return;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, nome, valor FROM receitas WHERE usuario_id = " + session.getUserId());

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
                    
                    tvCol1.setText("RECEITA");
                    tvCol1.setTextColor(getResources().getColor(R.color.text_secondary));
                    
                    tvCol2.setText(partes[1]);
                    tvCol2.setTypeface(null, android.graphics.Typeface.BOLD);
                    
                    tvCol3.setText(partes[2]);
                    tvCol3.setTextColor(getResources().getColor(R.color.green_positive));
                    
                    return view;
                }
            };
            listaReceitas.setAdapter(adapter);

            listaReceitas.setOnItemClickListener((parent, view, position, id) -> {
                receitaSelecionadaId = ids.get(position);
                String receita = lista.get(position);
                String[] partes = receita.split(" - ");
                edtNomeReceita.setText(partes[1]);
                edtValorReceita.setText(partes[2].replace("R$ ", ""));
                Toast.makeText(this, "Receita selecionada!", Toast.LENGTH_SHORT).show();
            });

            rs.close();
            stmt.close();
            ConexaoMySQL.fecharConexao(conn);
        } catch (SQLException e) {
            Toast.makeText(this, "Erro ao consultar!", Toast.LENGTH_SHORT).show();
        }
    }

    private void alterarReceita() {
        if (receitaSelecionadaId == -1) return;
        String nome = edtNomeReceita.getText().toString().trim();
        String valorStr = edtValorReceita.getText().toString().trim();

        if (nome.isEmpty() || valorStr.isEmpty()) {
            Toast.makeText(this, "Preencha nome e valor!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double valor = Double.parseDouble(valorStr);
            Connection conn = ConexaoMySQL.conectar();
            if (conn == null) return;

            String sql = "UPDATE receitas SET nome = ?, valor = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nome);
            stmt.setDouble(2, valor);
            stmt.setInt(3, receitaSelecionadaId);

            stmt.executeUpdate();
            stmt.close();
            ConexaoMySQL.fecharConexao(conn);

            Toast.makeText(this, "Receita alterada!", Toast.LENGTH_SHORT).show();
            edtNomeReceita.setText("");
            edtValorReceita.setText("");
            receitaSelecionadaId = -1;
            consultarReceitas();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao alterar!", Toast.LENGTH_SHORT).show();
        }
    }
}
