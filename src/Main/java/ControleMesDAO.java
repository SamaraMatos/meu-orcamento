import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ControleMesDAO {

    public void salvar(String mesAno) {

        String sql = """
                INSERT INTO controle_mes (id, mes_ano)
                VALUES (1, ?)
                ON CONFLICT(id)
                DO UPDATE SET mes_ano = excluded.mes_ano
                """;

        try (Connection conexao = BancoDeDados.conectar();
            PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, mesAno);

            comando.executeUpdate();

            System.out.println(
                    "Mês atual salvo no banco!"
            );

        } catch (Exception e) {

            System.out.println(
                    "Erro ao salvar mês atual."
            );

            e.printStackTrace();
        }
    }

    public String buscar() {

        String sql =
                "SELECT mes_ano FROM controle_mes WHERE id = 1";

        try (Connection conexao = BancoDeDados.conectar();
            PreparedStatement comando = conexao.prepareStatement(sql);
            ResultSet resultado = comando.executeQuery()) {

            if (resultado.next()) {

                return resultado.getString(
                        "mes_ano"
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "Erro ao buscar mês atual."
            );

            e.printStackTrace();
        }

        return null;
    }
}