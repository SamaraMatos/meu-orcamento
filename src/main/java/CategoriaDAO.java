import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

public Categoria buscarPorNome(String nome) {

    String sql =
            "SELECT id, nome FROM categorias WHERE nome = ?";

            try (Connection conexao = BancoDeDados.conectar();
                PreparedStatement comando = conexao.prepareStatement(sql)) {
            
                comando.setString(1, nome);
            
                ResultSet resultado = comando.executeQuery();

                if (resultado.next()) {
                    
                    int id = resultado.getInt("id");
                    String nomeCategoria = resultado.getString("nome");

                    Categoria categoria = new Categoria(id, nomeCategoria);

                    return categoria;
                }
                                } catch (Exception e) {

                    System.out.println("Erro ao buscar categoria.");
                    e.printStackTrace();
                }

                return null;

}
public List<Categoria> buscarTodos() {

    List<Categoria> categorias = new ArrayList<>();

    String sql = "SELECT id, nome FROM categorias";

    try (Connection conexao = BancoDeDados.conectar();
        PreparedStatement comando = conexao.prepareStatement(sql);
        ResultSet resultado = comando.executeQuery()) {

        while (resultado.next()) {

            int id = resultado.getInt("id");
            String nome = resultado.getString("nome");

            Categoria categoria = new Categoria(id, nome);

            categorias.add(categoria);
        }

    } catch (Exception e) {

        System.out.println(
                "Erro ao buscar categorias."
        );

        e.printStackTrace();
    }

    return categorias;
}

public boolean salvar(Categoria categoria) {

    String sql = "INSERT INTO categorias (nome) VALUES (?)";

    try (Connection conexao = BancoDeDados.conectar();
        PreparedStatement comando = conexao.prepareStatement(sql)) {

        comando.setString(1, categoria.getNome());

        comando.executeUpdate();

        System.out.println("Categoria salva no banco!");

        return true;

    } catch (Exception e) {

        System.out.println("Erro ao salvar categoria.");
        e.printStackTrace();

        return false;
    }
}
public boolean atualizar(Categoria categoria) {

    String sql = "UPDATE categorias SET nome = ? WHERE id = ?";

    try (Connection conexao = BancoDeDados.conectar();
        PreparedStatement comando = conexao.prepareStatement(sql)) {

        comando.setString(1, categoria.getNome());
        comando.setInt(2, categoria.getId());

    int linhasAfetadas = comando.executeUpdate();

    if (linhasAfetadas > 0) {
        System.out.println("Categoria atualizada no banco!");
        return true;
    }

    return false;

    } catch (Exception e) {

        System.out.println("Erro ao atualizar categoria.");
        e.printStackTrace();

        return false;
    }
}
public Categoria buscarPorId(int id) {

    String sql = "SELECT id, nome FROM categorias WHERE id = ?";

    try (Connection conexao = BancoDeDados.conectar();
        PreparedStatement comando = conexao.prepareStatement(sql)) {

        comando.setInt(1, id);

        try (ResultSet resultado = comando.executeQuery()) {

            if (resultado.next()) {

                int categoryId = resultado.getInt("id");
                String categoryName = resultado.getString("nome");

                return new Categoria(categoryId, categoryName);
            }
        }
    } catch (Exception e) {

        System.out.println("Erro ao buscar categoria por ID.");
        e.printStackTrace();
    }

    return null;
}
}

