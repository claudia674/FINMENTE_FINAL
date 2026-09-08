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

public class MetasActivity extends AppCompatActivity {

    Button btnVoltarMeta, btnSalvarMeta, btnConsultar, btnAlterar, btnExcluir;
    EditText edtMeta, edtValorMeta, edtValorGuardado;
    ListView listaMetas;
    int metaSelecionadaId = -1;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metas);

        session = new SessionManager(this);

        btnVoltarMeta = findViewById(R.id.btnVoltarMeta);
        btnSalvarMeta = findViewById(R.id.btnSalvarMeta);
        btnConsultar = findViewById(R.id.btnConsultar);
        btnAlterar = findViewById(R.id.btnAlterar);
        btnExcluir = findViewById(R.id.btnExcluir);
        edtMeta = findViewById(R.id.edtMeta);
        edtValorMeta = findViewById(R.id.edtValorMeta);
        edtValorGuardado = findViewById(R.id.edtValorGuardado);
        listaMetas = findViewById(R.id.listaMetas);

        btnVoltarMeta.setOnClickListener(v -> finish());
        btnSalvarMeta.setOnClickListener(v -> cadastrarMeta());
        btnConsultar.setOnClickListener(v -> consultarMetas());
        btnAlterar.setOnClickListener(v -> alterarMeta());
        btnExcluir.setOnClickListener(v -> excluirMeta());
    }

    private void cadastrarMeta() {
        String nome = edtMeta.getText().toString().trim();
        String valorMetaStr = edtValorMeta.getText().toString().trim();
        String valorGuardadoStr = edtValorGuardado.getText().toString().trim();

        if (nome.isEmpty() || valorMetaStr.isEmpty() || valorGuardadoStr.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double vMeta = Double.parseDouble(valorMetaStr);
            double vGuardado = Double.parseDouble(valorGuardadoStr);
            Connection conn = ConexaoMySQL.conectar();
            if (conn == null) return;

            String sql = "INSERT INTO metas (usuario_id, titulo, valor_alvo, valor_atual) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, session.getUserId());
            stmt.setString(2, nome);
            stmt.setDouble(3, vMeta);
            stmt.setDouble(4, vGuardado);

            stmt.executeUpdate();
            stmt.close();
            ConexaoMySQL.fecharConexao(conn);

            Toast.makeText(this, "Meta cadastrada!", Toast.LENGTH_SHORT).show();
            limparCampos();
            consultarMetas();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao cadastrar!", Toast.LENGTH_SHORT).show();
        }
    }

    private void consultarMetas() {
        try {
            Connection conn = ConexaoMySQL.conectar();
            if (conn == null) return;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, titulo, valor_alvo, valor_atual FROM metas WHERE usuario_id = " + session.getUserId());

            ArrayList<String> lista = new ArrayList<>();
            ArrayList<Integer> ids = new ArrayList<>();

            while (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("titulo");
                double vm = rs.getDouble("valor_alvo");
                double vg = rs.getDouble("valor_atual");
                ids.add(id);
                lista.add(id + " - " + nome + " (" + vg + "/" + vm + ")");
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
                    
                    tvCol1.setText("META");
                    tvCol1.setTextColor(getResources().getColor(R.color.text_secondary));
                    
                    tvCol2.setText(partes[1]);
                    tvCol2.setTypeface(null, android.graphics.Typeface.BOLD);
                    
                    int start = item.lastIndexOf("(");
                    int end = item.lastIndexOf(")");
                    if (start != -1 && end != -1) {
                        tvCol3.setText(item.substring(start));
                    }
                    tvCol3.setTextColor(getResources().getColor(R.color.purple_primary));
                    
                    return view;
                }
            };
            listaMetas.setAdapter(adapter);

            listaMetas.setOnItemClickListener((parent, view, position, id) -> {
                metaSelecionadaId = ids.get(position);

                try {
                    Connection conexao = ConexaoMySQL.conectar();
                    if (conexao == null) return;

                    String sqlMeta = "SELECT titulo, valor_alvo, valor_atual FROM metas WHERE id = ?";
                    PreparedStatement stmtMeta = conexao.prepareStatement(sqlMeta);
                    stmtMeta.setInt(1, metaSelecionadaId);

                    ResultSet rsMeta = stmtMeta.executeQuery();
                    if (rsMeta.next()) {
                        edtMeta.setText(rsMeta.getString("titulo"));
                        edtValorMeta.setText(String.valueOf(rsMeta.getDouble("valor_alvo")));
                        edtValorGuardado.setText(String.valueOf(rsMeta.getDouble("valor_atual")));
                    }

                    rsMeta.close();
                    stmtMeta.close();
                    ConexaoMySQL.fecharConexao(conexao);
                } catch (Exception e) {
                    Toast.makeText(this, "Erro ao carregar meta!", Toast.LENGTH_SHORT).show();
                }
            });
            rs.close();
            stmt.close();
            ConexaoMySQL.fecharConexao(conn);
        } catch (SQLException e) {
            Toast.makeText(this, "Erro ao consultar!", Toast.LENGTH_SHORT).show();
        }
    }

    private void alterarMeta() {
        if (metaSelecionadaId == -1) return;
        try {
            Connection conn = ConexaoMySQL.conectar();
            if (conn == null) return;
            String sql = "UPDATE metas SET titulo = ?, valor_alvo = ?, valor_atual = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, edtMeta.getText().toString());
            stmt.setDouble(2, Double.parseDouble(edtValorMeta.getText().toString()));
            stmt.setDouble(3, Double.parseDouble(edtValorGuardado.getText().toString()));
            stmt.setInt(4, metaSelecionadaId);
            stmt.executeUpdate();
            stmt.close();
            ConexaoMySQL.fecharConexao(conn);
            Toast.makeText(this, "Alterado!", Toast.LENGTH_SHORT).show();
            limparCampos();
            consultarMetas();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao alterar!", Toast.LENGTH_SHORT).show();
        }
    }

    private void excluirMeta() {
        if (metaSelecionadaId == -1) return;
        try {
            Connection conn = ConexaoMySQL.conectar();
            if (conn == null) return;
            String sql = "DELETE FROM metas WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, metaSelecionadaId);
            stmt.executeUpdate();
            stmt.close();
            ConexaoMySQL.fecharConexao(conn);
            Toast.makeText(this, "Excluído!", Toast.LENGTH_SHORT).show();
            limparCampos();
            consultarMetas();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao excluir!", Toast.LENGTH_SHORT).show();
        }
    }

    private void limparCampos() {
        metaSelecionadaId = -1;
        edtMeta.setText("");
        edtValorMeta.setText("");
        edtValorGuardado.setText("");
    }
}
