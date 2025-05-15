package com.example;

import java.sql.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String url = "jdbc:sqlite:clientes.db";

        // Loop principal do programa
        while (true) {
            try (Connection conn = DriverManager.getConnection(url)) {
                // --- FASE 1: Entrada de dados com validação ---
                String nome;
                String cpf;
                boolean cpfValido = false;

                // Loop até receber um nome válido
                do {
                    System.out.println("\nDigite o nome do cliente (ou 'sair' para encerrar):");
                    nome = scanner.nextLine().trim();
                    
                    if (nome.equalsIgnoreCase("sair")) {
                        System.out.println("Encerrando programa...");
                        scanner.close();
                        return;
                    }
                    
                } while (nome.isEmpty());

                // Loop até receber um CPF válido e não duplicado
                do {
                    System.out.println("Digite o CPF do cliente (formato: 123.456.789-09):");
                    cpf = scanner.nextLine().trim();

                    // Valida formato
                    if (!validarFormatoCPF(cpf)) {
                        System.out.println("Formato inválido! Use: 123.456.789-09");
                        continue;
                    }

                    // Verifica duplicata
                    if (cpfJaCadastrado(conn, cpf)) {
                        System.out.println("\nATENÇÃO: CPF já cadastrado!");
                        listarClientePorCPF(conn, cpf);
                        
                        System.out.println("\nO que deseja fazer?");
                        System.out.println("1 - Tentar outro CPF");
                        System.out.println("2 - Cadastrar novo cliente");
                        System.out.println("3 - Sair");
                        System.out.print("Opção: ");
                        
                        String opcao = scanner.nextLine();
                        switch (opcao) {
                            case "1":
                                continue; // Reinicia o loop do CPF
                            case "2":
                                break; // Sai do loop do CPF para reiniciar o cadastro
                            case "3":
                                scanner.close();
                                return;
                            default:
                                System.out.println("Opção inválida!");
                                continue;
                        }
                        break; // Sai do loop para novo cadastro
                    }
                    
                    cpfValido = true;
                } while (!cpfValido);

                // --- FASE 2: Cadastro no banco de dados ---
                cadastrarCliente(conn, nome, cpf);
                System.out.println("\nCliente cadastrado com sucesso!");

            } catch (SQLException e) {
                System.out.println("Erro no banco de dados: " + e.getMessage());
            }
        }
    }

    // ============ MÉTODOS AUXILIARES ============

    /** Valida se o CPF tem formato correto (com ou sem pontuação) */
    public static boolean validarFormatoCPF(String cpf) {
        return cpf.matches("^\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}$");
    }

    /** Verifica se CPF já existe no banco */
    public static boolean cpfJaCadastrado(Connection conn, String cpf) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
            "SELECT 1 FROM cliente WHERE cpf = ?"
        );
        pstmt.setString(1, cpf.replaceAll("[^0-9]", ""));
        return pstmt.executeQuery().next();
    }

    /** Mostra os dados de um cliente específico */
    public static void listarClientePorCPF(Connection conn, String cpf) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
            "SELECT id, nome, cpf FROM cliente WHERE cpf = ?"
        );
        pstmt.setString(1, cpf.replaceAll("[^0-9]", ""));
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            System.out.println(
                "\nCliente existente:\n" +
                "ID: " + rs.getInt("id") + "\n" +
                "Nome: " + rs.getString("nome") + "\n" +
                "CPF: " + rs.getString("cpf")
            );
        }
    }

    /** Efetua o cadastro no banco de dados */
    public static void cadastrarCliente(Connection conn, String nome, String cpf) throws SQLException {
        // Cria tabela se não existir
        conn.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS cliente (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "nome TEXT NOT NULL," +
            "cpf TEXT NOT NULL UNIQUE);"
        );

        // Insere os dados
        PreparedStatement pstmt = conn.prepareStatement(
            "INSERT INTO cliente(nome, cpf) VALUES (?, ?)"
        );
        pstmt.setString(1, nome);
        pstmt.setString(2, cpf.replaceAll("[^0-9]", "")); // Armazena só números
        pstmt.executeUpdate();
    }
}