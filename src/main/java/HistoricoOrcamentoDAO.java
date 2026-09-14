import java.sql.Connection;
import java.sql.PreparedStatement;

public class HistoricoOrcamentoDAO {

public void salvar(
        String mesAno,
        double valor
) {

String sql = """
        INSERT INTO historico_orcamentos
        (mes_ano, valor)
        VALUES (?, ?)
        ON CONFLICT(mes_ano)
        DO UPDATE SET valor = excluded.valor
        """;

try (Connection conexao = BancoDeDados.conectar();
        PreparedStatement comando = conexao.prepareStatement(sql)) {

        comando.setString(
                1,
                mesAno
        );

        comando.setDouble(
                2,
                valor
        );

        comando.executeUpdate();

        System.out.println(
                "Orçamento salvo no histórico!"
        );

} catch (Exception e) {

        System.out.println(
                "Erro ao salvar orçamento no histórico."
        );

        e.printStackTrace();
}
}

public boolean existe(String mesAno) {

String sql =
        "SELECT mes_ano FROM historico_orcamentos WHERE mes_ano = ?";

try (Connection conexao = BancoDeDados.conectar();
        PreparedStatement comando = conexao.prepareStatement(sql)) {

        comando.setString(
                1,
                mesAno
        );

        try (var resultado = comando.executeQuery()) {

        return resultado.next();
        }

} catch (Exception e) {

        System.out.println(
                "Erro ao verificar histórico do orçamento."
        );

        e.printStackTrace();

        return false;
}
}
}