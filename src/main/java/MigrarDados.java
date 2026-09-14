import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class MigrarDados {

    public static void main(String[] args) {

        String sqliteUrl =
                "jdbc:sqlite:meu_orcamento_backup.db";

        try (
                Connection sqlite =
                        DriverManager.getConnection(sqliteUrl);

                Connection postgres =
                        BancoDeDados.conectar()
        ) {

            if (postgres == null) {
                System.out.println(
                        "Não foi possível conectar ao Neon."
                );
                return;
            }

            System.out.println(
                    "Iniciando migração..."
            );

            migrarOrcamento(
                    sqlite,
                    postgres
            );

            migrarGastos(
                    sqlite,
                    postgres
            );

            migrarHistoricoOrcamentos(
                    sqlite,
                    postgres
            );

            migrarHistoricoGastos(
                    sqlite,
                    postgres
            );

            migrarControleMes(
                    sqlite,
                    postgres
            );

            ajustarSequencias(
                    postgres
            );

            System.out.println();
            System.out.println(
                    "Migração concluída com sucesso!"
            );

        } catch (Exception e) {

            System.out.println(
                    "Erro durante a migração."
            );

            e.printStackTrace();
        }
    }


    private static void migrarOrcamento(
            Connection sqlite,
            Connection postgres
    ) throws Exception {

        String buscar =
                "SELECT id, valor FROM orcamento";

        String salvar = """
                INSERT INTO orcamento (id, valor)
                VALUES (?, ?)
                ON CONFLICT(id)
                DO UPDATE SET valor = excluded.valor
                """;

        try (
                Statement consulta =
                        sqlite.createStatement();

                ResultSet resultado =
                        consulta.executeQuery(buscar);

                PreparedStatement inserir =
                        postgres.prepareStatement(salvar)
        ) {

            while (resultado.next()) {

                inserir.setInt(
                        1,
                        resultado.getInt("id")
                );

                inserir.setDouble(
                        2,
                        resultado.getDouble("valor")
                );

                inserir.executeUpdate();
            }
        }

        System.out.println(
                "Orçamento migrado."
        );
    }


    private static void migrarGastos(
            Connection sqlite,
            Connection postgres
    ) throws Exception {

        String buscar =
                "SELECT id, descricao, valor FROM gastos";

        String salvar = """
                INSERT INTO gastos
                (id, descricao, valor)
                VALUES (?, ?, ?)
                ON CONFLICT(id)
                DO UPDATE SET
                    descricao = excluded.descricao,
                    valor = excluded.valor
                """;

        try (
                Statement consulta =
                        sqlite.createStatement();

                ResultSet resultado =
                        consulta.executeQuery(buscar);

                PreparedStatement inserir =
                        postgres.prepareStatement(salvar)
        ) {

            while (resultado.next()) {

                inserir.setInt(
                        1,
                        resultado.getInt("id")
                );

                inserir.setString(
                        2,
                        resultado.getString("descricao")
                );

                inserir.setDouble(
                        3,
                        resultado.getDouble("valor")
                );

                inserir.executeUpdate();
            }
        }

        System.out.println(
                "Gastos migrados."
        );
    }


    private static void migrarHistoricoOrcamentos(
            Connection sqlite,
            Connection postgres
    ) throws Exception {

        String buscar = """
                SELECT mes_ano, valor
                FROM historico_orcamentos
                """;

        String salvar = """
                INSERT INTO historico_orcamentos
                (mes_ano, valor)
                VALUES (?, ?)
                ON CONFLICT(mes_ano)
                DO UPDATE SET valor = excluded.valor
                """;

        try (
                Statement consulta =
                        sqlite.createStatement();

                ResultSet resultado =
                        consulta.executeQuery(buscar);

                PreparedStatement inserir =
                        postgres.prepareStatement(salvar)
        ) {

            while (resultado.next()) {

                inserir.setString(
                        1,
                        resultado.getString("mes_ano")
                );

                inserir.setDouble(
                        2,
                        resultado.getDouble("valor")
                );

                inserir.executeUpdate();
            }
        }

        System.out.println(
                "Histórico de orçamentos migrado."
        );
    }


    private static void migrarHistoricoGastos(
            Connection sqlite,
            Connection postgres
    ) throws Exception {

        String buscar = """
                SELECT id, mes_ano, descricao, valor
                FROM historico_gastos
                """;

        String salvar = """
                INSERT INTO historico_gastos
                (id, mes_ano, descricao, valor)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(id)
                DO UPDATE SET
                    mes_ano = excluded.mes_ano,
                    descricao = excluded.descricao,
                    valor = excluded.valor
                """;

        try (
                Statement consulta =
                        sqlite.createStatement();

                ResultSet resultado =
                        consulta.executeQuery(buscar);

                PreparedStatement inserir =
                        postgres.prepareStatement(salvar)
        ) {

            while (resultado.next()) {

                inserir.setInt(
                        1,
                        resultado.getInt("id")
                );

                inserir.setString(
                        2,
                        resultado.getString("mes_ano")
                );

                inserir.setString(
                        3,
                        resultado.getString("descricao")
                );

                inserir.setDouble(
                        4,
                        resultado.getDouble("valor")
                );

                inserir.executeUpdate();
            }
        }

        System.out.println(
                "Histórico de gastos migrado."
        );
    }


    private static void migrarControleMes(
            Connection sqlite,
            Connection postgres
    ) throws Exception {

        String buscar =
                "SELECT id, mes_ano FROM controle_mes";

        String salvar = """
                INSERT INTO controle_mes
                (id, mes_ano)
                VALUES (?, ?)
                ON CONFLICT(id)
                DO UPDATE SET mes_ano = excluded.mes_ano
                """;

        try (
                Statement consulta =
                        sqlite.createStatement();

                ResultSet resultado =
                        consulta.executeQuery(buscar);

                PreparedStatement inserir =
                        postgres.prepareStatement(salvar)
        ) {

            while (resultado.next()) {

                inserir.setInt(
                        1,
                        resultado.getInt("id")
                );

                inserir.setString(
                        2,
                        resultado.getString("mes_ano")
                );

                inserir.executeUpdate();
            }
        }

        System.out.println(
                "Controle do mês migrado."
        );
    }


    private static void ajustarSequencias(
            Connection postgres
    ) throws Exception {

        try (Statement comando =
                    postgres.createStatement()) {

            comando.execute("""
                    SELECT setval(
                        pg_get_serial_sequence(
                            'gastos',
                            'id'
                        ),
                        COALESCE(
                            (SELECT MAX(id) FROM gastos),
                            1
                        )
                    )
                    """);

            comando.execute("""
                    SELECT setval(
                        pg_get_serial_sequence(
                            'historico_gastos',
                            'id'
                        ),
                        COALESCE(
                            (
                                SELECT MAX(id)
                                FROM historico_gastos
                            ),
                            1
                        )
                    )
                    """);
        }

        System.out.println(
                "Sequências ajustadas."
        );
    }
}