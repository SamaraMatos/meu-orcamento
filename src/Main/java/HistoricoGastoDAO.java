import java.sql.Connection;
import java.sql.PreparedStatement;

public class HistoricoGastoDAO {

public void salvar(
        String mesAno,
        Gasto gasto
) {

String sql = """
        INSERT INTO historico_gastos
        (mes_ano, descricao, valor)
        VALUES (?, ?, ?)
        """;

try (Connection conexao = BancoDeDados.conectar();
        PreparedStatement comando = conexao.prepareStatement(sql)) {

        comando.setString(
                1,
                mesAno
        );

        comando.setString(
                2,
                gasto.getDescricao()
        );

        comando.setDouble(
                3,
                gasto.getValor()
        );

        comando.executeUpdate();

        System.out.println(
                "Gasto salvo no histórico!"
        );

} catch (Exception e) {

        System.out.println(
                "Erro ao salvar gasto no histórico."
        );

        e.printStackTrace();
}
}
public boolean excluirPorMes(
        String mesAno
) {

        String sql =
        "DELETE FROM historico_gastos WHERE mes_ano = ?";

        try (Connection conexao = BancoDeDados.conectar();
        PreparedStatement comando = conexao.prepareStatement(sql)) {

        comando.setString(
                1,
                mesAno
        );

        comando.executeUpdate();

        System.out.println(
                "Histórico antigo do mês removido!"
        );

        return true;

        } catch (Exception e) {

        System.out.println(
                "Erro ao limpar histórico do mês."
        );

        e.printStackTrace();

        return false;
        }
}
}