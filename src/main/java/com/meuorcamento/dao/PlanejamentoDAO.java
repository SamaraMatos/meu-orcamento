package com.meuorcamento.dao;

import com.meuorcamento.BancoDeDados;
import com.meuorcamento.modelo.Planejamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class PlanejamentoDAO {

    public boolean salvar(Planejamento planejamento) {

        String sql = "INSERT INTO public.planejamentos "
                + "(descricao, valor_estimado) VALUES (?, ?)";

        try (Connection conexao = BancoDeDados.conectar();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, planejamento.getDescricao());
            stmt.setDouble(2, planejamento.getValorEstimado());

            int linhasInseridas = stmt.executeUpdate();

            return linhasInseridas > 0;

        } catch (SQLException e) {
            System.out.println("Erro ao salvar planejamento: " + e.getMessage());
        }
        return false;
    }

    public List<Planejamento> buscarAtivos() throws SQLException {

        List<Planejamento> planejamentos = new ArrayList<>();

        String sql = "SELECT id, descricao, valor_estimado, status "
                + "FROM public.planejamentos "
                + "WHERE status = 'ativo' ORDER BY id";

        try (Connection conexao = BancoDeDados.conectar()) {

            if (conexao == null) {
                throw new SQLException("Não foi possível conectar ao banco de dados.");
            }

            try (PreparedStatement stmt = conexao.prepareStatement(sql);
                    ResultSet resultado = stmt.executeQuery()) {

                while (resultado.next()) {
                    int id = resultado.getInt("id");
                    String descricao = resultado.getString("descricao");
                    double valorEstimado = resultado.getDouble("valor_estimado");
                    String status = resultado.getString("status");

                    Planejamento planejamento = new Planejamento(id, descricao, valorEstimado, status);
                    planejamentos.add(planejamento);
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar planejamentos: " + e.getMessage());
            throw e;
        }

        return planejamentos;
    }

    public boolean encerrar(int id) {

        String sql = "UPDATE public.planejamentos "
                + "SET status = 'encerrado' "
                + "WHERE id = ? AND status = 'ativo'";

        try (Connection conexao = BancoDeDados.conectar();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int linhasAlteradas = stmt.executeUpdate();

            return linhasAlteradas > 0;

        } catch (SQLException e) {
            System.out.println("Erro ao encerrar planejamento: " + e.getMessage());
        }

        return false;
    }

}