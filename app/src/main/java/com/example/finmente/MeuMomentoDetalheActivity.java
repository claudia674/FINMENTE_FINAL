package com.example.finmente;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MeuMomentoDetalheActivity extends AppCompatActivity {

    private String categoria;
    private EditText edtTitulo, edtDescricao;
    private TextView tvTituloCategoria;
    private Button btnSalvar, btnEditar, btnExcluir, btnVoltar;
    private ListView listViewMomento;

    private int registroSelecionadoId = -1;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meu_momento_detalhe);

        session = new SessionManager(this);

        categoria = getIntent().getStringExtra("categoria");
        if (categoria == null) categoria = "Livre";

        edtTitulo = findViewById(R.id.edtTituloMomento);
        edtDescricao = findViewById(R.id.edtDescricaoMomento);
        tvTituloCategoria = findViewById(R.id.tvTituloCategoria);
        btnSalvar = findViewById(R.id.btnSalvarMomento);
        btnEditar = findViewById(R.id.btnEditarMomento);
        btnExcluir = findViewById(R.id.btnExcluirMomento);
        btnVoltar = findViewById(R.id.btnVoltarMomentoDetalhe);
        listViewMomento = findViewById(R.id.listViewMomento);

        tvTituloCategoria.setText(categoria);

        btnSalvar.setOnClickListener(v -> salvarRegistro());
        btnEditar.setOnClickListener(v -> editarRegistro());
        btnExcluir.setOnClickListener(v -> excluirRegistro());
        btnVoltar.setOnClickListener(v -> finish());

        consultarRegistros();
    }

    private void salvarRegistro() {
        String titulo = edtTitulo.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();

        if (titulo.isEmpty() || descricao.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        try (Connection conn = ConexaoMySQL.conectar()) {
            if (conn == null) return;

            String sql = "INSERT INTO meu_momento (usuario_id, tipo, titulo, descricao) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, session.getUserId());
            stmt.setString(2, categoria);
            stmt.setString(3, titulo);
            stmt.setString(4, descricao);
            stmt.executeUpdate();

            Toast.makeText(this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show();
            limparCampos();
            consultarRegistros();

        } catch (SQLException e) {
            Log.e("MeuMomentoDetalhe", "Erro ao salvar: " + e.getMessage());
        }
    }

    private void consultarRegistros() {
        try (Connection conn = ConexaoMySQL.conectar()) {
            if (conn == null) return;

            String sql = "SELECT id, titulo, descricao, data FROM meu_momento WHERE tipo = ? AND usuario_id = ? ORDER BY data DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, categoria);
            stmt.setInt(2, session.getUserId());
            ResultSet rs = stmt.executeQuery();

            ArrayList<RegistroMomento> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new RegistroMomento(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("descricao"),
                        rs.getTimestamp("data")
                ));
            }

            MomentoAdapter adapter = new MomentoAdapter(lista);
            listViewMomento.setAdapter(adapter);

        } catch (SQLException e) {
            Log.e("MeuMomentoDetalhe", "Erro ao consultar: " + e.getMessage());
        }
    }

    private void editarRegistro() {
        if (registroSelecionadoId == -1) return;
        String titulo = edtTitulo.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();

        try (Connection conn = ConexaoMySQL.conectar()) {
            if (conn == null) return;
            String sql = "UPDATE meu_momento SET titulo = ?, descricao = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, titulo);
            stmt.setString(2, descricao);
            stmt.setInt(3, registroSelecionadoId);
            stmt.executeUpdate();

            Toast.makeText(this, "Atualizado com sucesso!", Toast.LENGTH_SHORT).show();
            limparCampos();
            consultarRegistros();
        } catch (SQLException e) {
            Log.e("MeuMomentoDetalhe", "Erro ao editar: " + e.getMessage());
        }
    }

    private void excluirRegistro() {
        if (registroSelecionadoId == -1) return;
        try (Connection conn = ConexaoMySQL.conectar()) {
            if (conn == null) return;
            String sql = "DELETE FROM meu_momento WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, registroSelecionadoId);
            stmt.executeUpdate();

            Toast.makeText(this, "Excluído com sucesso!", Toast.LENGTH_SHORT).show();
            limparCampos();
            consultarRegistros();
        } catch (SQLException e) {
            Log.e("MeuMomentoDetalhe", "Erro ao excluir: " + e.getMessage());
        }
    }

    private void limparCampos() {
        registroSelecionadoId = -1;
        edtTitulo.setText("");
        edtDescricao.setText("");
    }

    private static class RegistroMomento {
        int id;
        String titulo, descricao;
        java.sql.Timestamp data;

        RegistroMomento(int id, String titulo, String descricao, java.sql.Timestamp data) {
            this.id = id;
            this.titulo = titulo;
            this.descricao = descricao;
            this.data = data;
        }
    }

    private class MomentoAdapter extends ArrayAdapter<RegistroMomento> {
        MomentoAdapter(ArrayList<RegistroMomento> registros) {
            super(MeuMomentoDetalheActivity.this, R.layout.list_item_momento, registros);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_momento, parent, false);
            }

            RegistroMomento r = getItem(position);
            TextView tvTitulo = convertView.findViewById(R.id.tvTituloItemMomento);
            TextView tvData = convertView.findViewById(R.id.tvDataItemMomento);
            TextView tvDesc = convertView.findViewById(R.id.tvDescItemMomento);

            tvTitulo.setText(r.titulo);
            tvDesc.setText(r.descricao);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", new Locale("pt", "BR"));
            tvData.setText(sdf.format(r.data));

            convertView.setOnClickListener(v -> {
                registroSelecionadoId = r.id;
                edtTitulo.setText(r.titulo);
                edtDescricao.setText(r.descricao);
            });

            return convertView;
        }
    }
}
