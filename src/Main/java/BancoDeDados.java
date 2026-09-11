import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class BancoDeDados {

    private static final String URL = "jdbc:sqlite:meu_orcamento.db";

    public static Connection conectar() {

    try {

        Class.forName("org.sqlite.JDBC");

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

        String sqlGastos = """
                CREATE TABLE IF NOT EXISTS gastos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    descricao TEXT NOT NULL,
                    valor REAL NOT NULL
                )
                """;

        String sqlOrcamento = """
                CREATE TABLE IF NOT EXISTS orcamento (
                    id INTEGER PRIMARY KEY,
                    valor REAL NOT NULL
                )
                """;

        try (Connection conexao = conectar();
            Statement statement = conexao.createStatement()) {

            statement.execute(sqlGastos);
            statement.execute(sqlOrcamento);

            System.out.println("Tabelas criadas com sucesso!");

        } catch (Exception e) {

            System.out.println("Erro ao criar tabelas.");
            e.printStackTrace();
        }
    }
}