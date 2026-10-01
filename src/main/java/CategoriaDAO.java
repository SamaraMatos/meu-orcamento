import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
}

