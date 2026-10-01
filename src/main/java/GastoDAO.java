import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class GastoDAO {

public boolean salvar(Gasto gasto) {

    String sql = "INSERT INTO gastos (descricao, valor, categoria_id) VALUES (?, ?, ?)";

    try (Connection conexao = BancoDeDados.conectar();
        PreparedStatement comando = conexao.prepareStatement(sql)) {

        comando.setString(1, gasto.getDescricao());
        comando.setDouble(2, gasto.getValor());
        comando.setInt(3, gasto.getCategoria().getId());

        comando.executeUpdate();

        System.out.println("Gasto salvo no banco!");

        return true;

    } catch (Exception e) {

        System.out.println("Erro ao salvar gasto.");
        e.printStackTrace();

        return false;
    }
}

    public ArrayList<Gasto> buscarTodos() {

        ArrayList<Gasto> gastos = new ArrayList<>();

        String sql = """
        SELECT g.id,
                g.descricao,
                g.valor,
                c.id AS categoria_id,
                c.nome AS categoria_nome
            FROM gastos g
            JOIN categorias c
            ON g.categoria_id = c.id
            """;

        try (Connection conexao = BancoDeDados.conectar();
            PreparedStatement comando = conexao.prepareStatement(sql);
            ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {

                int id = resultado.getInt("id");
                String descricao = resultado.getString("descricao");
                double valor = resultado.getDouble("valor");
                
                int categoriaId = resultado.getInt("categoria_id");
                String categoriaNome = resultado.getString("categoria_nome");

                Categoria categoria = new Categoria(categoriaId, categoriaNome);

                Gasto gasto = new Gasto(id, descricao, valor, categoria);

                gastos.add(gasto);
            }

        } catch (Exception e) {

            System.out.println("Erro ao buscar gastos.");
            e.printStackTrace();
        }

        return gastos;
    }

public Gasto buscarPorId(int id) {

    String sql = """
            SELECT g.id,
                    g.descricao,
                    g.valor,
                    c.id AS categoria_id,
                    c.nome AS categoria_nome
                FROM gastos g
                JOIN categorias c
                ON g.categoria_id = c.id
                WHERE g.id = ?
                """;

                try (Connection conexao = BancoDeDados.conectar();
                    PreparedStatement comando = conexao.prepareStatement(sql)) {

                    comando.setInt(1, id);
                    ResultSet resultado = comando.executeQuery();

                    if (resultado.next()) {

                        String descricao = resultado.getString("descricao");
                        double valor = resultado.getDouble("valor");

                        int categoriaId = resultado.getInt("categoria_id");
                        String categoriaNome = resultado.getString("categoria_nome");

                        Categoria categoria =
                                new Categoria(categoriaId, categoriaNome);

                        Gasto gasto =
                                new Gasto(id, descricao, valor, categoria);

                        return gasto;
                    }

                    } catch (Exception e) {

    System.out.println(
            "Erro ao buscar gasto por ID."
    );

    e.printStackTrace();
}

return null;
}


public boolean atualizar(Gasto gasto) {

    String sql =
            "UPDATE gastos SET descricao = ?, valor = ? WHERE id = ?";

    try (Connection conexao = BancoDeDados.conectar();
        PreparedStatement comando = conexao.prepareStatement(sql)) {

        comando.setString(
                1,
                gasto.getDescricao()
        );

        comando.setDouble(
                2,
                gasto.getValor()
        );

        comando.setInt(
                3,
                gasto.getId()
        );

        int linhasAfetadas =
                comando.executeUpdate();

        if (linhasAfetadas > 0) {

            System.out.println(
                    "Gasto atualizado no banco!"
            );

            return true;
        }

        System.out.println(
                "Nenhum gasto encontrado com esse ID."
        );

        return false;

    } catch (Exception e) {

        System.out.println(
                "Erro ao atualizar gasto."
        );

        e.printStackTrace();

        return false;
    }
}

public boolean excluir(int id) {

    String sql = "DELETE FROM gastos WHERE id = ?";

    try (Connection conexao = BancoDeDados.conectar();
        PreparedStatement comando = conexao.prepareStatement(sql)) {

        comando.setInt(1, id);

        int linhasAfetadas =
                comando.executeUpdate();

        if (linhasAfetadas > 0) {

            System.out.println(
                    "Gasto excluído do banco!"
            );

            return true;
        }

        System.out.println(
                "Nenhum gasto encontrado com esse ID."
        );

        return false;

    } catch (Exception e) {

        System.out.println(
                "Erro ao excluir gasto."
        );

        e.printStackTrace();

        return false;
    }
}
public boolean excluirTodos() {

    String sql = "DELETE FROM gastos";

    try (Connection conexao = BancoDeDados.conectar();
        PreparedStatement comando = conexao.prepareStatement(sql)) {

        comando.executeUpdate();

        System.out.println(
                "Gastos atuais removidos com sucesso!"
        );

        return true;

    } catch (Exception e) {

        System.out.println(
                "Erro ao remover gastos atuais."
        );

        e.printStackTrace();

        return false;
    }
}
}