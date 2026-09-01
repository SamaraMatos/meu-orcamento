import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class BancoDeDados {

    private static final String URL = "jdbc:sqlite:meu_orcamento.db";

    public static Connection conectar() {

        try {

            Connection conexao = DriverManager.getConnection(URL);

            System.out.println("Banco de dados conectado!");

            return conexao;

        } catch (Exception e) {

            System.out.println("Erro ao conectar ao banco de dados.");
            e.printStackTrace();

            return null;
        }
    }

    public static void criarTabela() {

        String sql = """
                CREATE TABLE IF NOT EXISTS gastos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    descricao TEXT NOT NULL,
                    valor REAL NOT NULL
                )
                """;

        try (Connection conexao = conectar();
             Statement statement = conexao.createStatement()) {

            statement.execute(sql);

            System.out.println("Tabela gastos criada com sucesso!");

        } catch (Exception e) {

            System.out.println("Erro ao criar tabela.");
            e.printStackTrace();
        }
    }
}