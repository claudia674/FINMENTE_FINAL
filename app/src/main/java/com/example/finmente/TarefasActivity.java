package com.example.finmente;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finmente.R;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TarefasActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private EditText edtTituloTarefa, edtHorarioTarefa;
    private Spinner spinnerPrioridade;
    private MaterialSwitch switchAlerta;
    private Button btnAdicionar, btnAlterar, btnExcluir, btnVoltar;
    private ListView listViewTarefas;
    private TextView tvDataHoje;

    private int tarefaSelecionadaId = -1;
    private String dataSelecionada;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarefas);

        session = new SessionManager(this);

        calendarView = findViewById(R.id.calendarView);
        edtTituloTarefa = findViewById(R.id.edtTituloTarefa);
        edtHorarioTarefa = findViewById(R.id.edtHorarioTarefa);
        spinnerPrioridade = findViewById(R.id.spinnerPrioridade);
        switchAlerta = findViewById(R.id.switchAlerta);
        btnAdicionar = findViewById(R.id.btnAdicionarTarefa);
        btnAlterar = findViewById(R.id.btnAlterarTarefa);
        btnExcluir = findViewById(R.id.btnExcluirTarefa);
        btnVoltar = findViewById(R.id.btnVoltarTarefas);
        listViewTarefas = findViewById(R.id.listViewTarefas);
        tvDataHoje = findViewById(R.id.tvDataHoje);

        edtHorarioTarefa.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            new TimePickerDialog(this, (view, h, m) -> {
                edtHorarioTarefa.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
            }, hour, minute, true).show();
        });

        Calendar cal = Calendar.getInstance();
        updateDateLabel(cal.getTimeInMillis());
        dataSelecionada = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar c = Calendar.getInstance();
            c.set(year, month, dayOfMonth);
            updateDateLabel(c.getTimeInMillis());
            dataSelecionada = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            consultarTarefas();
        });

        btnAdicionar.setOnClickListener(v -> cadastrarTarefa());
        btnAlterar.setOnClickListener(v -> alterarTarefa());
        btnExcluir.setOnClickListener(v -> excluirTarefa());
        btnVoltar.setOnClickListener(v -> finish());

        consultarTarefas();
    }

    private void updateDateLabel(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d 'DE' MMMM", new Locale("pt", "BR"));
        tvDataHoje.setText(sdf.format(timeInMillis).toUpperCase());
    }

    private void cadastrarTarefa() {
        String titulo = edtTituloTarefa.getText().toString().trim();
        String horario = edtHorarioTarefa.getText().toString().trim();
        String prioridade = spinnerPrioridade.getSelectedItem().toString();
        boolean alerta = switchAlerta.isChecked();

        if (titulo.isEmpty()) {
            Toast.makeText(this, "Informe o título da tarefa!", Toast.LENGTH_SHORT).show();
            return;
        }

        try (Connection conn = ConexaoMySQL.conectar()) {
            if (conn == null) return;

            String sql = "INSERT INTO tarefas (usuario_id, titulo, horario, data, prioridade, alerta) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, session.getUserId());
            stmt.setString(2, titulo);
            stmt.setString(3, horario);
            stmt.setString(4, dataSelecionada);
            stmt.setString(5, prioridade);
            stmt.setInt(6, alerta ? 1 : 0);

            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                if (alerta && !horario.isEmpty()) {
                    scheduleAlarm(id, titulo, horario, dataSelecionada);
                }
            }

            Toast.makeText(this, "Tarefa adicionada!", Toast.LENGTH_SHORT).show();
            limparCampos();
            consultarTarefas();

        } catch (SQLException e) {
            Log.e("TarefasActivity", "Erro ao cadastrar: " + e.getMessage());
        }
    }

    private void scheduleAlarm(int id, String titulo, String horarioStr, String dataStr) {
        try {
            String[] timeParts = horarioStr.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            cal.setTime(sdf.parse(dataStr));
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);

            if (cal.getTimeInMillis() <= System.currentTimeMillis()) return;

            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("titulo", titulo);
            intent.putExtra("id", id);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
            }
        } catch (Exception e) {
            Log.e("TarefasActivity", "Erro ao agendar: " + e.getMessage());
        }
    }

    private void cancelAlarm(int id) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private void consultarTarefas() {
        try (Connection conn = ConexaoMySQL.conectar()) {
            if (conn == null) return;

            String sql = "SELECT id, titulo, horario, prioridade, concluida, alerta FROM tarefas WHERE data = ? AND usuario_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, dataSelecionada);
            stmt.setInt(2, session.getUserId());
            ResultSet rs = stmt.executeQuery();

            ArrayList<Tarefa> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new Tarefa(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("horario"),
                        rs.getString("prioridade"),
                        rs.getInt("concluida") == 1,
                        rs.getInt("alerta") == 1
                ));
            }

            TarefaAdapter adapter = new TarefaAdapter(lista);
            listViewTarefas.setAdapter(adapter);

        } catch (SQLException e) {
            Log.e("TarefasActivity", "Erro ao consultar: " + e.getMessage());
        }
    }

    private void alterarTarefa() {
        if (tarefaSelecionadaId == -1) return;
        String titulo = edtTituloTarefa.getText().toString().trim();
        String horario = edtHorarioTarefa.getText().toString().trim();
        String prioridade = spinnerPrioridade.getSelectedItem().toString();
        boolean alerta = switchAlerta.isChecked();

        try (Connection conn = ConexaoMySQL.conectar()) {
            if (conn == null) return;
            String sql = "UPDATE tarefas SET titulo = ?, horario = ?, prioridade = ?, alerta = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, titulo);
            stmt.setString(2, horario);
            stmt.setString(3, prioridade);
            stmt.setInt(4, alerta ? 1 : 0);
            stmt.setInt(5, tarefaSelecionadaId);
            stmt.executeUpdate();
            
            cancelAlarm(tarefaSelecionadaId);
            if (alerta && !horario.isEmpty()) {
                scheduleAlarm(tarefaSelecionadaId, titulo, horario, dataSelecionada);
            }

            Toast.makeText(this, "Tarefa alterada!", Toast.LENGTH_SHORT).show();
            limparCampos();
            consultarTarefas();
        } catch (SQLException e) {
            Log.e("TarefasActivity", "Erro ao alterar: " + e.getMessage());
        }
    }

    private void excluirTarefa() {
        if (tarefaSelecionadaId == -1) return;
        try (Connection conn = ConexaoMySQL.conectar()) {
            if (conn == null) return;
            String sql = "DELETE FROM tarefas WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tarefaSelecionadaId);
            stmt.executeUpdate();
            
            cancelAlarm(tarefaSelecionadaId);

            Toast.makeText(this, "Tarefa excluída!", Toast.LENGTH_SHORT).show();
            limparCampos();
            consultarTarefas();
        } catch (SQLException e) {
            Log.e("TarefasActivity", "Erro ao excluir: " + e.getMessage());
        }
    }

    private void limparCampos() {
        tarefaSelecionadaId = -1;
        edtTituloTarefa.setText("");
        edtHorarioTarefa.setText("");
        switchAlerta.setChecked(false);
    }

    private static class Tarefa {
        int id;
        String titulo, horario, prioridade;
        boolean concluida, alerta;

        Tarefa(int id, String titulo, String horario, String prioridade, boolean concluida, boolean alerta) {
            this.id = id;
            this.titulo = titulo;
            this.horario = horario;
            this.prioridade = prioridade;
            this.concluida = concluida;
            this.alerta = alerta;
        }
    }

    private class TarefaAdapter extends ArrayAdapter<Tarefa> {
        TarefaAdapter(ArrayList<Tarefa> tarefas) {
            super(TarefasActivity.this, R.layout.list_item_tarefa, tarefas);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_tarefa, parent, false);
            }

            Tarefa t = getItem(position);
            TextView tvTitulo = convertView.findViewById(R.id.tvTituloTarefa);
            TextView tvDetalhes = convertView.findViewById(R.id.tvDetalhesTarefa);
            TextView tvPrioridade = convertView.findViewById(R.id.tvPrioridade);
            CheckBox cb = convertView.findViewById(R.id.checkConcluida);
            ImageView imgAlerta = convertView.findViewById(R.id.imgAlertaAtivo);

            tvTitulo.setText(t.titulo);
            tvDetalhes.setText(t.horario);
            tvPrioridade.setText(t.prioridade);
            cb.setChecked(t.concluida);
            imgAlerta.setVisibility(t.alerta ? View.VISIBLE : View.GONE);

            int color = 0xFF9E9E9E; 
            if (t.prioridade.equals("ALTA")) color = 0xFFE91E63;
            else if (t.prioridade.equals("MÉDIA")) color = 0xFFFFC107;
            else if (t.prioridade.equals("LEVE")) color = 0xFF4CAF50;
            
            tvPrioridade.getBackground().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);

            convertView.setOnClickListener(v -> {
                tarefaSelecionadaId = t.id;
                edtTituloTarefa.setText(t.titulo);
                edtHorarioTarefa.setText(t.horario);
                switchAlerta.setChecked(t.alerta);
                for (int i = 0; i < spinnerPrioridade.getCount(); i++) {
                    if (spinnerPrioridade.getItemAtPosition(i).toString().equals(t.prioridade)) {
                        spinnerPrioridade.setSelection(i);
                        break;
                    }
                }
            });

            cb.setOnClickListener(v -> {
                atualizarStatusConclusao(t.id, cb.isChecked());
            });

            return convertView;
        }
    }

    private void atualizarStatusConclusao(int id, boolean concluida) {
        try (Connection conn = ConexaoMySQL.conectar()) {
            if (conn == null) return;
            String sql = "UPDATE tarefas SET concluida = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, concluida ? 1 : 0);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Log.e("TarefasActivity", "Erro ao atualizar status: " + e.getMessage());
        }
    }
}
