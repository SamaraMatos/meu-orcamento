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

        String sqlCategorias = """
                        CREATE TABLE IF NOT EXISTS categorias (
                        id SERIAL PRIMARY KEY,
                        nome TEXT NOT NULL UNIQUE
                        )
                        """;

        String sqlCategoriasPadrao = """
        INSERT INTO categorias (nome)
        VALUES ('Mercado'),
                ('Saúde'),
                ('Lazer'),
                ('Casa'),
                ('Transporte'),
                ('Outros')
        ON CONFLICT (nome) DO NOTHING
        """;

        String sqlAdicionarCategoriaGastos = """
        ALTER TABLE gastos
        ADD COLUMN IF NOT EXISTS categoria_id INTEGER
        """;

        String sqlCategoriaGastosAntigos = """
        UPDATE gastos
        SET categoria_id = (
        SELECT id
        FROM categorias
        WHERE nome = 'Outros'
        )
        WHERE categoria_id IS NULL
        """;

        String sqlCategoriaObrigatoria = """
        ALTER TABLE gastos
        ALTER COLUMN categoria_id SET NOT NULL
        """;

        String sqlRelacionarCategoriaGasto = """
        DO $$
        BEGIN
                IF NOT EXISTS (
                SELECT 1
                FROM pg_constraint
                WHERE conname = 'fk_gastos_categoria'
                ) THEN
                ALTER TABLE gastos
                ADD CONSTRAINT fk_gastos_categoria
                FOREIGN KEY (categoria_id)
                REFERENCES categorias(id);
                END IF;
        END $$;
        """;

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
        String sqlAdicionarCategoriaHistorico = """
        ALTER TABLE historico_gastos
        ADD COLUMN IF NOT EXISTS categoria_id INTEGER
        """;

        String sqlCategoriaHistoricoAntigos = """
        UPDATE historico_gastos
        SET categoria_id = (
        SELECT id
        FROM categorias
        WHERE nome = 'Outros'
        )
        WHERE categoria_id IS NULL
        """;

        String sqlCategoriaHistoricoObrigatoria = """
        ALTER TABLE historico_gastos
        ALTER COLUMN categoria_id SET NOT NULL
        """;

        String sqlRelacionarCategoriaHistorico = """
                DO $$
                BEGIN
                IF NOT EXISTS (
                SELECT 1
                FROM pg_constraint
                WHERE conname = 'fk_historico_gastos_categoria'
                ) THEN
                ALTER TABLE historico_gastos
                ADD CONSTRAINT fk_historico_gastos_categoria
                FOREIGN KEY (categoria_id)
                REFERENCES categorias(id);
                END IF;
                END $$;
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

                statement.execute(sqlCategorias);
                statement.execute(sqlCategoriasPadrao);
                statement.execute(sqlGastos);
                statement.execute(sqlAdicionarCategoriaGastos);
                statement.execute(sqlCategoriaGastosAntigos);
                statement.execute(sqlCategoriaObrigatoria);
                statement.execute(sqlRelacionarCategoriaGasto);
                statement.execute(sqlOrcamento);
                statement.execute(sqlHistoricoGastos);
                statement.execute(sqlAdicionarCategoriaHistorico);
                statement.execute(sqlCategoriaHistoricoAntigos);
                statement.execute(sqlCategoriaHistoricoObrigatoria);
                statement.execute(sqlRelacionarCategoriaHistorico);
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