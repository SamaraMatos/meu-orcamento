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

}

