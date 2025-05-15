package com.example;

import org.junit.jupiter.api.*;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe de testes para a aplicação de cadastro de clientes.
 * Verifica as principais funcionalidades do sistema.
 */
class MainTest {
    // URL do banco de dados SQLite em memória (não persiste após os testes)
    private static final String URL = "jdbc:sqlite:file:test?mode=memory&cache=shared";
    private static Connection conn;

    /**
     * Configuração inicial ANTES de todos os testes.
     * - Cria a conexão com o banco
     * - Cria a tabela 'cliente'
     */
    @BeforeAll
    static void setUp() throws SQLException {
        // 1. Estabelece conexão com o banco em memória
        conn = DriverManager.getConnection(URL);
        
        // 2. Cria a tabela 'cliente' (igual à da aplicação real)
        conn.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS cliente (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +  // ID autoincrementável
            "nome TEXT NOT NULL," +                   // Nome é obrigatório
            "cpf TEXT NOT NULL UNIQUE" +              // CPF único e obrigatório
            ");"
        );
    }

    /**
     * Limpeza APÓS CADA teste.
     * - Remove todos os registros para evitar interferência entre testes
     */
    @AfterEach
    void cleanUp() throws SQLException {
        conn.createStatement().execute("DELETE FROM cliente");
    }

    /**
     * Finalização APÓS TODOS os testes.
     * - Fecha a conexão com o banco
     */
    @AfterAll
    static void tearDown() throws SQLException {
        conn.close();
    }

    // ============== CASOS DE TESTE ==============

    /**
     * Testa a validação de formato de CPF.
     * Verifica se o método identifica corretamente CPFs válidos e inválidos.
     */
    @Test
    void testValidarFormatoCPF() {
        // CPFs válidos (com e sem formatação)
        assertTrue(Main.validarFormatoCPF("123.456.789-09"), "CPF formatado deve ser válido");
        assertTrue(Main.validarFormatoCPF("12345678909"), "CPF sem formatação deve ser válido");

        // CPFs inválidos
        assertFalse(Main.validarFormatoCPF("123"), "CPF curto deve ser inválido");
        assertFalse(Main.validarFormatoCPF("123.abc.789-09"), "CPF com letras deve ser inválido");
    }

    /**
     * Testa o cadastro de um novo cliente.
     * Verifica se os dados são persistidos corretamente no banco.
     */
    @Test
    void testCadastrarCliente() throws SQLException {
        // 1. Executa o método de cadastro
        Main.cadastrarCliente(conn, "Fulano de Tal", "111.222.333-44");

        // 2. Consulta o banco para verificar se o cliente foi criado
        ResultSet rs = conn.createStatement().executeQuery(
            "SELECT * FROM cliente WHERE cpf = '11122233344'"
        );

        // 3. Verificações
        assertTrue(rs.next(), "Cliente deve existir no banco");
        assertEquals("Fulano de Tal", rs.getString("nome"), "Nome deve ser igual ao cadastrado");
        assertEquals("11122233344", rs.getString("cpf"), "CPF deve ser armazenado sem formatação");
    }

    /**
     * Testa a detecção de CPF duplicado.
     * Verifica se o sistema identifica corretamente CPFs já cadastrados.
     */
    @Test
    void testCPFDuplicado() throws SQLException {
        // 1. Cadastra um cliente inicial
        Main.cadastrarCliente(conn, "Beltrano Silva", "999.888.777-66");

        // 2. Verifica se o CPF é detectado como duplicado
        assertTrue(
            Main.cpfJaCadastrado(conn, "999.888.777-66"),
            "CPF já cadastrado deve ser detectado"
        );

        // 3. Verifica que um CPF não cadastrado retorna falso
        assertFalse(
            Main.cpfJaCadastrado(conn, "000.000.000-00"),
            "CPF não cadastrado não deve ser detectado como duplicado"
        );
    }

    /**
     * Testa a listagem de cliente por CPF.
     * Verifica se os dados retornados estão corretos.
     */
    @Test
    void testListarClientePorCPF() throws SQLException {
        // 1. Cadastra um cliente de teste
        Main.cadastrarCliente(conn, "Ciclano Santos", "555.444.333-22");

        // 2. Obtém os resultados (simulando o método real)
        ResultSet rs = conn.createStatement().executeQuery(
            "SELECT id, nome, cpf FROM cliente WHERE cpf = '55544433322'"
        );

        // 3. Verificações
        assertTrue(rs.next(), "Deveria retornar o cliente");
        assertEquals("Ciclano Santos", rs.getString("nome"));
        assertEquals("55544433322", rs.getString("cpf"));
    }
}