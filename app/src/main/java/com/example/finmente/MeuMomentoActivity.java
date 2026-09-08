package com.example.finmente;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finmente.R;

public class MeuMomentoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meu_momento);

        findViewById(R.id.menu_livre).setOnClickListener(v -> abrirDetalhe("Livre"));
        findViewById(R.id.menu_pessoal).setOnClickListener(v -> abrirDetalhe("Pessoal"));
        findViewById(R.id.menu_estetica).setOnClickListener(v -> abrirDetalhe("Estética"));

        findViewById(R.id.btnVoltarMeuMomento).setOnClickListener(v -> finish());
    }

    private void abrirDetalhe(String categoria) {
        Intent intent = new Intent(this, MeuMomentoDetalheActivity.class);
        intent.putExtra("categoria", categoria);
        startActivity(intent);
    }
}
