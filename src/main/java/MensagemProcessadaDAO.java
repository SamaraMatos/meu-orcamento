import java.sql.Connection;
import java.sql.PreparedStatement;

public class MensagemProcessadaDAO {

    public boolean registrarSeNova(String messageId) {

        String sql = """
                INSERT INTO mensagens_processadas (message_id)
                VALUES (?)
                ON CONFLICT (message_id) DO NOTHING
                """;

        try (Connection conexao = BancoDeDados.conectar();
            PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, messageId);

            int linhasAfetadas = comando.executeUpdate();

            return linhasAfetadas > 0;

        } catch (Exception e) {

            System.out.println(
                    "Erro ao registrar mensagem processada."
            );

            e.printStackTrace();

            return false;
        }
    }
}
