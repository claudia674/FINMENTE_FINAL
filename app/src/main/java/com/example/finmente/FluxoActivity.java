package com.example.finmente;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finmente.R;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FluxoActivity extends AppCompatActivity {

    private TextView tvSaldoPrevisto, tvLabelSaldo, tvDataMeta;
    private LinearProgressIndicator progressFluxo;
    private LinearLayout containerLancamentos;
    
    private EditText edtDescricao, edtValor, edtData;
    private MaterialButtonToggleGroup toggleTipo;
    private Button btnSalvar, btnAlterar, btnExcluir, btnLimpar;
    
    private SessionManager session;
    private NumberFormat format;
    private int registroSelecionadoId = -1;
    private String tipoSelecionado = "RECEITA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fluxo);

        session = new SessionManager(this);
        format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        tvSaldoPrevisto = findViewById(R.id.tvValorSaldoPrevisto);
        tvLabelSaldo = findViewById(R.id.tvLabelSaldoPrevisto);
        tvDataMeta = findViewById(R.id.tvDataMetaPrevista);
        progressFluxo = findViewById(R.id.progressFluxo);
        containerLancamentos = findViewById(R.id.containerLancamentos);

        edtDescricao = findViewById(R.id.edtDescricaoFluxo);
        edtValor = findViewById(R.id.edtValorFluxo);
        edtData = findViewById(R.id.edtDataFluxo);
        toggleTipo = findViewById(R.id.toggleTipoFluxo);
        
        btnSalvar = findViewById(R.id.btnSalvarFluxo);
        btnAlterar = findViewById(R.id.btnAlterarFluxo);
        btnExcluir = findViewById(R.id.btnExcluirFluxo);
        btnLimpar = findViewById(R.id.btnLimparFluxo);

        setupListeners();
        atualizarFluxo();
    }

    private void setupListeners() {
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        edtData.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                edtData.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        toggleTipo.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                tipoSelecionado = (checkedId == R.id.btnTipoReceita) ? "RECEITA" : "DESPESA";
            }
        });

        btnSalvar.setOnClickListener(v -> salvarLancamento());
        btnAlterar.setOnClickListener(v -> alterarLancamento());
        btnExcluir.setOnClickListener(v -> excluirLancamento());
        btnLimpar.setOnClickListener(v -> limparCampos());
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarFluxo();
    }

    private void salvarLancamento() {
        String desc = edtDescricao.getText().toString().trim();
        String valorStr = edtValor.getText().toString().trim();
        String data = edtData.getText().toString().trim();

        if (desc.isEmpty() || valorStr.isEmpty()) {
            Toast.makeText(this, "Preencha os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        try (Connection conn = ConexaoMySQL.conectar()) {
            if (conn == null) return;
            double valor = Double.parseDouble(valorStr);
            String tabela = tipoSelecionado.equals("RECEITA") ? "receitas" : "despesas";
            
            String sql = "INSERT INTO " + tabela + " (usuario_id, nome, valor, data) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, session.getUserId());
            stmt.setString(2, desc);
            stmt.setDouble(3, valor);
            stmt.setString(4, data.isEmpty() ? new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()) : data);
            
            stmt.executeUpdate();
            Toast.makeText(this, "Lançado com sucesso!", Toast.LENGTH_SHORT).show();
            limparCampos();
            atualizarFluxo();
        } catch (Exception e) {
            Log.e("FluxoActivity", "Erro salvar: " + e.getMessage());
        }
    }

    private void alterarLancamento() {
        if (registroSelecionadoId == -1) {
            Toast.makeText(this, "Selecione um item!", Toast.LENGTH_SHORT).show();
            return;
        }

        String desc = edtDescricao.getText().toString().trim();
        String valorStr = edtValor.getText().toString().trim();
        String data = edtData.getText().toString().trim();

        try (Connection conn = ConexaoMySQL.conectar()) {
            if (conn == null) return;
            String tabela = tipoSelecionado.equals("RECEITA") ? "receitas" : "despesas";
            
            String sql = "UPDATE " + tabela + " SET nome = ?, valor = ?, data = ? WHERE id = ? AND usuario_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, desc);
            stmt.setDouble(2, Double.parseDouble(valorStr));
            stmt.setString(3, data);
            stmt.setInt(4, registroSelecionadoId);
            stmt.setInt(5, session.getUserId());
            
            stmt.executeUpdate();
            Toast.makeText(this, "Alterado!", Toast.LENGTH_SHORT).show();
            limparCampos();
            atualizarFluxo();
        } catch (Exception e) {
            Log.e("FluxoActivity", "Erro alterar: " + e.getMessage());
        }
    }

    private void excluirLancamento() {
        if (registroSelecionadoId == -1) return;

        try (Connection conn = ConexaoMySQL.conectar()) {
            if (conn == null) return;
            String tabela = tipoSelecionado.equals("RECEITA") ? "receitas" : "despesas";
            
            String sql = "DELETE FROM " + tabela + " WHERE id = ? AND usuario_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, registroSelecionadoId);
            stmt.setInt(2, session.getUserId());
            
            stmt.executeUpdate();
            Toast.makeText(this, "Excluído!", Toast.LENGTH_SHORT).show();
            limparCampos();
            atualizarFluxo();
        } catch (Exception e) {
            Log.e("FluxoActivity", "Erro excluir: " + e.getMessage());
        }
    }

    private void limparCampos() {
        registroSelecionadoId = -1;
        edtDescricao.setText("");
        edtValor.setText("");
        edtData.setText("");
        toggleTipo.check(R.id.btnTipoReceita);
    }

    private void atualizarFluxo() {
        containerLancamentos.removeAllViews();
        
        try (Connection conn = ConexaoMySQL.conectar()) {
            if (conn == null) return;

            int userId = session.getUserId();
            Statement stmt = conn.createStatement();

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdfLabel = new SimpleDateFormat("dd MMM", new Locale("pt", "BR"));
            tvLabelSaldo.setText("SALDO PREVISTO ATÉ " + sdfLabel.format(cal.getTime()).toUpperCase());
            tvDataMeta.setText("Até " + sdfLabel.format(cal.getTime()));

            ResultSet rsTotal = stmt.executeQuery(
                "SELECT " +
                "(SELECT IFNULL(SUM(valor), 0) FROM receitas WHERE usuario_id = " + userId + ") - " +
                "(SELECT IFNULL(SUM(valor), 0) FROM despesas WHERE usuario_id = " + userId + ")"
            );
            double saldoPrevisto = 0;
            if (rsTotal.next()) saldoPrevisto = rsTotal.getDouble(1);
            rsTotal.close();
            tvSaldoPrevisto.setText(format.format(saldoPrevisto) + " ✨");

            String sql = "SELECT id, 'RECEITA' as tipo, nome, valor, data FROM receitas WHERE usuario_id = " + userId +
                         " UNION ALL " +
                         "SELECT id, 'DESPESA' as tipo, nome, valor, data FROM despesas WHERE usuario_id = " + userId +
                         " ORDER BY data ASC";

            ResultSet rs = stmt.executeQuery(sql);
            String lastHeader = "";
            LayoutInflater inflater = LayoutInflater.from(this);

            while (rs.next()) {
                int id = rs.getInt("id");
                String tipo = rs.getString("tipo");
                String nome = rs.getString("nome");
                double valor = rs.getDouble("valor");
                java.sql.Date dataSql = rs.getDate("data");
                
                String header = getHeaderForDate(dataSql);
                if (!header.equals(lastHeader)) {
                    View hv = inflater.inflate(R.layout.list_group_header, containerLancamentos, false);
                    ((TextView) hv.findViewById(R.id.tvHeader)).setText(header);
                    containerLancamentos.addView(hv);
                    lastHeader = header;
                }

                View iv = inflater.inflate(R.layout.list_item_fluxo, containerLancamentos, false);
                TextView tvTitulo = iv.findViewById(R.id.tvTituloFluxo);
                TextView tvStatus = iv.findViewById(R.id.tvStatusFluxo);
                TextView tvValor = iv.findViewById(R.id.tvValorFluxo);
                TextView tvIcon = iv.findViewById(R.id.tvIconFluxo);
                MaterialCardView cardIcon = iv.findViewById(R.id.cardIconFluxo);

                tvTitulo.setText(nome);
                tvValor.setText(format.format(valor));
                
                if (tipo.equals("RECEITA")) {
                    tvIcon.setText("+");
                    tvIcon.setTextColor(getResources().getColor(R.color.vibrant_green));
                    cardIcon.setCardBackgroundColor(getResources().getColor(R.color.soft_green));
                    tvValor.setTextColor(getResources().getColor(R.color.green_positive));
                    tvStatus.setText("Recebido");
                } else {
                    tvIcon.setText("-");
                    tvIcon.setTextColor(getResources().getColor(R.color.red_negative));
                    cardIcon.setCardBackgroundColor(getResources().getColor(R.color.soft_pink));
                    tvValor.setTextColor(getResources().getColor(R.color.red_negative));
                    tvStatus.setText("Pendente");
                }

                iv.setOnClickListener(v -> {
                    registroSelecionadoId = id;
                    tipoSelecionado = tipo;
                    edtDescricao.setText(nome);
                    edtValor.setText(String.valueOf(valor));
                    edtData.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dataSql));
                    toggleTipo.check(tipo.equals("RECEITA") ? R.id.btnTipoReceita : R.id.btnTipoDespesa);
                    Toast.makeText(this, "Item selecionado para edição", Toast.LENGTH_SHORT).show();
                });

                containerLancamentos.addView(iv);
            }
            rs.close();
        } catch (SQLException e) {
            Log.e("FluxoActivity", "Erro atualizar: " + e.getMessage());
        }
    }

    private String getHeaderForDate(Date date) {
        Calendar target = Calendar.getInstance();
        target.setTime(date);
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d 'DE' MMMM", new Locale("pt", "BR"));
        if (isSameDay(target, today)) return "HOJE, " + sdf.format(date).split(",")[0].toUpperCase();
        if (isSameDay(target, tomorrow)) return "AMANHÃ, " + sdf.format(date).split(",")[0].toUpperCase();
        
        Calendar next7Days = Calendar.getInstance();
        next7Days.add(Calendar.DAY_OF_YEAR, 7);
        if (target.after(today) && target.before(next7Days)) return "PRÓXIMOS 7 DIAS";
        
        return new SimpleDateFormat("MMMM", new Locale("pt", "BR")).format(date).toUpperCase();
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
