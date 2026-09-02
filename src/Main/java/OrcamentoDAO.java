import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class OrcamentoDAO {

    public void salvar(double valor) {

        String sql = """
                INSERT INTO orcamento (id, valor)
                VALUES (1, ?)
                ON CONFLICT(id)
                DO UPDATE SET valor = excluded.valor
                """;

        try (Connection conexao = BancoDeDados.conectar();
            PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setDouble(1, valor);

            comando.executeUpdate();

            System.out.println("Orçamento salvo no banco!");

        } catch (Exception e) {

            System.out.println("Erro ao salvar orçamento.");
            e.printStackTrace();
        }
    }

    public Double buscar() {

        String sql = "SELECT valor FROM orcamento WHERE id = 1";

        try (Connection conexao = BancoDeDados.conectar();
            PreparedStatement comando = conexao.prepareStatement(sql);
            ResultSet resultado = comando.executeQuery()) {

            if (resultado.next()) {

                return resultado.getDouble("valor");
            }

        } catch (Exception e) {

            System.out.println("Erro ao buscar orçamento.");
            e.printStackTrace();
        }

        return null;
    }
}