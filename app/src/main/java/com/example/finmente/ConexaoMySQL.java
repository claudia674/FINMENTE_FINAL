package com.example.finmente;


import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@SuppressWarnings("SpellCheckingInspection")
public class ConexaoMySQL {

    private static final String URL =

            "jdbc:mysql://finmente-claudia-80d2.b.aivencloud.com:14542/finmente"
                    + "?useSSL=true"
                    + "&requireSSL=true"
                    + "&verifyServerCertificate=false"
                    + "&serverTimezone=UTC";

    private static final String USUARIO = "avnadmin";
    private static final String SENHA = "SUA_SENHA_AIVEN";

    @Nullable
    public static Connection conectar() {
        try {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder()
                            .permitAll()
                            .build();

            StrictMode.setThreadPolicy(policy);

            Class.forName("com.mysql.jdbc.Driver");

            Connection conexao =
                    DriverManager.getConnection(
                            URL,
                            USUARIO,
                            SENHA
                    );

            Log.d("MySQL", "CONEXÃO ESTABELECIDA COM SUCESSO!");
            return conexao;

        } catch (ClassNotFoundException e) {
            Log.e("MySQL", "DRIVER NÃO ENCONTRADO: " + e.getMessage());
        } catch (SQLException e) {
            Log.e("MySQL", "ERRO DE SQL AO CONECTAR: " + e.getMessage());

            String mensagemErro = e.getMessage();
            if (mensagemErro != null && mensagemErro.contains("Access denied")) {
                Log.e("MySQL", "***************************************************");
                Log.e("MySQL", "ERRO: ACESSO NEGADO PELO BANCO DE DADOS!");
                Log.e("MySQL", "DICA 1: Verifique a SENHA configurada no local.properties.");

                String ipStr = "seu IP";
                try {
                    ipStr = mensagemErro.split("'")[3];
                } catch (Exception ignored) {}

                Log.e("MySQL", "DICA 2: Libere o IP " + ipStr + " no Allowlist do Aiven.");
                Log.e("MySQL", "***************************************************");
            }
        } catch (Exception e) {
            Log.e("MySQL", "ERRO INESPERADO: " + e.getMessage());
        }
        return null;
    }

    public static void inicializarBanco(Connection conn) {
        if (conn == null) return;

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS usuarios (id INT AUTO_INCREMENT PRIMARY KEY, nome VARCHAR(100) NOT NULL, email VARCHAR(100) NOT NULL UNIQUE, senha VARCHAR(100) NOT NULL)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS receitas (id INT AUTO_INCREMENT PRIMARY KEY, usuario_id INT NOT NULL, nome VARCHAR(100) NOT NULL, valor DECIMAL(10,2) NOT NULL, data DATE NOT NULL, FOREIGN KEY (usuario_id) REFERENCES usuarios(id))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS despesas (id INT AUTO_INCREMENT PRIMARY KEY, usuario_id INT NOT NULL, nome VARCHAR(100) NOT NULL, valor DECIMAL(10,2) NOT NULL, data DATE NOT NULL, descricao VARCHAR(255), FOREIGN KEY (usuario_id) REFERENCES usuarios(id))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS transacoes (id INT AUTO_INCREMENT PRIMARY KEY, usuario_id INT NOT NULL, tipo ENUM('receita','despesa') NOT NULL, categoria VARCHAR(50) NOT NULL, valor DECIMAL(10,2) NOT NULL, data DATE NOT NULL, descricao VARCHAR(255), FOREIGN KEY (usuario_id) REFERENCES usuarios(id))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS alertas (id INT AUTO_INCREMENT PRIMARY KEY, usuario_id INT NOT NULL, titulo VARCHAR(100) NOT NULL, valor_limite DECIMAL(10,2), data_vencimento DATE NOT NULL, status ENUM('pendente','pago') DEFAULT 'pendente', FOREIGN KEY (usuario_id) REFERENCES usuarios(id))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS metas (id INT AUTO_INCREMENT PRIMARY KEY, usuario_id INT NOT NULL, titulo VARCHAR(100) NOT NULL, valor_alvo DECIMAL(10,2) NOT NULL, valor_atual DECIMAL(10,2) DEFAULT 0, prazo DATE, FOREIGN KEY (usuario_id) REFERENCES usuarios(id))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS humor_financeiro (id INT AUTO_INCREMENT PRIMARY KEY, usuario_id INT NOT NULL, data DATE NOT NULL, humor ENUM('ansioso','tranquilo','no_controle','preocupado') NOT NULL, observacao VARCHAR(255), FOREIGN KEY (usuario_id) REFERENCES usuarios(id))");
            stmt.executeUpdate("INSERT IGNORE INTO usuarios (nome, email, senha) VALUES ('Claudia', 'claudia@gmail.com', '123')");
            Log.d("MySQL", "TABELAS VERIFICADAS!");
        } catch (SQLException e) {
            Log.e("MySQL", "ERRO AO INICIALIZAR TABELAS: " + e.getMessage(), e);
        }
    }

    public static void fecharConexao(Connection conexao) {
        try {
            if (conexao != null && !conexao.isClosed()) {
                conexao.close();
                Log.d("MySQL", "CONEXÃO FECHADA.");
            }
        } catch (SQLException e) {
            Log.e("MySQL", "ERRO AO FECHAR CONEXÃO: " + e.getMessage());
        }
    }
}