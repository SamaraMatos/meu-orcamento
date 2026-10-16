package com.meuorcamento;

import java.sql.SQLException;

import com.meuorcamento.dao.PlanejamentoDAO;

public class PlanejamentoDAOTeste {

    public static void main(String[] args) {
        String enderecoBanco = System.getenv("DATABASE_URL");

        if (enderecoBanco != null && !enderecoBanco.isBlank()) {
            throw new IllegalStateException(
                    "Teste interrompido: existe uma DATABASE_URL configurada.");
        }

        PlanejamentoDAO dao = new PlanejamentoDAO();

        try {
            dao.buscarAtivos();
        } catch (SQLException e) {
            System.out.println(
                    "TESTE PASSOU: a falta de conexão gerou uma SQLException.");
            return;
        }

        throw new AssertionError(
                "TESTE FALHOU: o método deveria ter lançado uma SQLException.");
    }
}