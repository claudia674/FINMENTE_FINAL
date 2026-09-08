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

public class DiarioActivity extends AppCompatActivity {

    private EditText edtTitulo, edtConteudo;
    private TextView tvFrase;
    private Button btnSalvar, btnEditar, btnExcluir, btnVoltar;
    private ListView listViewDiario;

    private int registroSelecionadoId = -1;
    private SessionManager session;
    private final String[] frases = {
            "Organize uma pequena parte da sua vida.",
            "Pare de esperar o momento perfeito e comece."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diario);

        session = new SessionManager(this);

        edtTitulo = findViewById(R.id.edtTituloDiario);
        edtConteudo = findViewById(R.id.edtConteudoDiario);
        tvFrase = findViewById(R.id.tvFraseMotivacional);
        btnSalvar = findViewById(R.id.btnSalvarDiario);
        btnEditar = findViewById(R.id.btnEditarDiario);
        btnExcluir = findViewById(R.id.btnExcluirDiario);
        btnVoltar = findViewById(R.id.btnVoltarDiario);
        listViewDiario = findViewById(R.id.listViewDiario);

        int index = (int) (Math.random() * frases.length);
        tvFrase.setText(frases[index]);

        btnSalvar.setOnClickListener(v -> salvarRegistro());
        btnEditar.setOnClickListener(v -> editarRegistro());
        btnExcluir.setOnClickListener(v -> excluirRegistro());
        btnVoltar.setOnClickListener(v -> finish());

        consultarRegistros();
    }

    private void salvarRegistro() {
        String titulo = edtTitulo.getText().toString().trim();
        String conteudo = edtConteudo.getText().toString().trim();

        if (titulo.isEmpty() || conteudo.isEmpty()) {
            Toast.makeText(this, "Preencha o título e o conteúdo!", Toast.LENGTH_SHORT).show();
            return;
        }

        try (Connection conn = ConexaoMySQL.conectar()) {
            if (conn == null) return;

            String sql = "INSERT INTO diario (usuario_id, titulo, conteudo) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, session.getUserId());
            stmt.setString(2, titulo);
            stmt.setString(3, conteudo);
            stmt.executeUpdate();

            Toast.makeText(this, "Registro salvo!", Toast.LENGTH_SHORT).show();
            limparCampos();
            consultarRegistros();

        } catch (SQLException e) {
            Log.e("DiarioActivity", "Erro ao salvar: " + e.getMessage());
        }
    }

    private void consultarRegistros() {
        try (Connection conn = ConexaoMySQL.conectar()) {
            if (conn == null) return;

            String sql = "SELECT id, titulo, conteudo, data FROM diario WHERE usuario_id = ? ORDER BY data DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, session.getUserId());
            ResultSet rs = stmt.executeQuery();

            ArrayList<RegistroDiario> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new RegistroDiario(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("conteudo"),
                        rs.getTimestamp("data")
                ));
            }

            DiarioAdapter adapter = new DiarioAdapter(lista);
            listViewDiario.setAdapter(adapter);

        } catch (SQLException e) {
            Log.e("DiarioActivity", "Erro ao consultar: " + e.getMessage());
        }
    }

    private void editarRegistro() {
        if (registroSelecionadoId == -1) return;

        String titulo = edtTitulo.getText().toString().trim();
        String conteudo = edtConteudo.getText().toString().trim();

        try (Connection conn = ConexaoMySQL.conectar()) {
            if (conn == null) return;
            String sql = "UPDATE diario SET titulo = ?, conteudo = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, titulo);
            stmt.setString(2, conteudo);
            stmt.setInt(3, registroSelecionadoId);
            stmt.executeUpdate();

            Toast.makeText(this, "Registro atualizado!", Toast.LENGTH_SHORT).show();
            limparCampos();
            consultarRegistros();
        } catch (SQLException e) {
            Log.e("DiarioActivity", "Erro ao editar: " + e.getMessage());
        }
    }

    private void excluirRegistro() {
        if (registroSelecionadoId == -1) return;

        try (Connection conn = ConexaoMySQL.conectar()) {
            if (conn == null) return;
            String sql = "DELETE FROM diario WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, registroSelecionadoId);
            stmt.executeUpdate();

            Toast.makeText(this, "Registro excluído!", Toast.LENGTH_SHORT).show();
            limparCampos();
            consultarRegistros();
        } catch (SQLException e) {
            Log.e("DiarioActivity", "Erro ao excluir: " + e.getMessage());
        }
    }

    private void limparCampos() {
        registroSelecionadoId = -1;
        edtTitulo.setText("");
        edtConteudo.setText("");
    }

    private static class RegistroDiario {
        int id;
        String titulo, conteudo;
        java.sql.Timestamp data;

        RegistroDiario(int id, String titulo, String conteudo, java.sql.Timestamp data) {
            this.id = id;
            this.titulo = titulo;
            this.conteudo = conteudo;
            this.data = data;
        }
    }

    private class DiarioAdapter extends ArrayAdapter<RegistroDiario> {
        DiarioAdapter(ArrayList<RegistroDiario> registros) {
            super(DiarioActivity.this, R.layout.list_item_diario, registros);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_diario, parent, false);
            }

            RegistroDiario r = getItem(position);
            TextView tvTitulo = convertView.findViewById(R.id.tvTituloItemDiario);
            TextView tvData = convertView.findViewById(R.id.tvDataItemDiario);
            TextView tvConteudo = convertView.findViewById(R.id.tvConteudoItemDiario);

            tvTitulo.setText(r.titulo);
            tvConteudo.setText(r.conteudo);

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", new Locale("pt", "BR"));
            tvData.setText(sdf.format(r.data));

            convertView.setOnClickListener(v -> {
                registroSelecionadoId = r.id;
                edtTitulo.setText(r.titulo);
                edtConteudo.setText(r.conteudo);
            });

            return convertView;
        }
    }
}
