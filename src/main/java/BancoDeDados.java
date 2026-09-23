import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class BancoDeDados {

private static final String DATABASE_URL =
        System.getenv("DATABASE_URL");

public static Connection conectar() {

        try {

        if (DATABASE_URL == null
                || DATABASE_URL.isBlank()) {

        System.out.println(
                "DATABASE_URL não encontrada."
        );

        return null;
        }

        java.net.URI uri =
                new java.net.URI(DATABASE_URL);

        String[] usuarioSenha =
                uri.getUserInfo().split(":", 2);

        String usuario =
                usuarioSenha[0];

        String senha =
                usuarioSenha[1];

        String host =
                uri.getHost();

        int porta =
                uri.getPort() == -1
                        ? 5432
                        : uri.getPort();

        String banco =
                uri.getPath().substring(1);

        String jdbcUrl =
                "jdbc:postgresql://"
                + host
                + ":"
                + porta
                + "/"
                + banco
                + "?sslmode=require";

        Class.forName(
                "org.postgresql.Driver"
        );

        Connection conexao =
                DriverManager.getConnection(
                        jdbcUrl,
                        usuario,
                        senha
                );

        System.out.println(
                "Banco de dados conectado!"
        );

        return conexao;

    } catch (Exception e) {

        System.out.println(
                "Erro ao conectar ao banco de dados."
        );

        e.printStackTrace();

        return null;
    }
}

public static void criarTabela() {

        String sqlGastos = """
                CREATE TABLE IF NOT EXISTS gastos (
                    id SERIAL PRIMARY KEY,
                    descricao TEXT NOT NULL,
                    valor DOUBLE PRECISION NOT NULL
                )
                """;

        String sqlOrcamento = """
                CREATE TABLE IF NOT EXISTS orcamento (
                    id INTEGER PRIMARY KEY,
                    valor DOUBLE PRECISION NOT NULL
                )
                """;

        String sqlHistoricoGastos = """
                CREATE TABLE IF NOT EXISTS historico_gastos (
                    id SERIAL PRIMARY KEY,
                    mes_ano TEXT NOT NULL,
                    descricao TEXT NOT NULL,
                    valor DOUBLE PRECISION NOT NULL
                )
                """;

        String sqlHistoricoOrcamentos = """
                CREATE TABLE IF NOT EXISTS historico_orcamentos (
                    mes_ano TEXT PRIMARY KEY,
                    valor DOUBLE PRECISION NOT NULL
                )
                """;

        String sqlControleMes = """
                CREATE TABLE IF NOT EXISTS controle_mes (
                    id INTEGER PRIMARY KEY,
                    mes_ano TEXT NOT NULL
                )
                """;

        String sqlMensagensProcessadas = """
        CREATE TABLE IF NOT EXISTS mensagens_processadas (
            message_id TEXT PRIMARY KEY,
            processada_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
        """;

        try (Connection conexao = conectar();
            Statement statement =
                    conexao.createStatement()) {

            statement.execute(sqlGastos);
            statement.execute(sqlOrcamento);
            statement.execute(sqlHistoricoGastos);
            statement.execute(sqlHistoricoOrcamentos);
            statement.execute(sqlControleMes);
            statement.execute(sqlMensagensProcessadas);
            System.out.println(
                    "Tabelas criadas com sucesso!"
            );

        } catch (Exception e) {

            System.out.println(
                    "Erro ao criar tabelas."
            );

            e.printStackTrace();
        }
    }
}