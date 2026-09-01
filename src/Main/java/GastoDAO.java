import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class GastoDAO {

    public void salvar(Gasto gasto) {

        String sql = "INSERT INTO gastos (descricao, valor) VALUES (?, ?)";

        try (Connection conexao = BancoDeDados.conectar();
            PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, gasto.getDescricao());
            comando.setDouble(2, gasto.getValor());

            comando.executeUpdate();

            System.out.println("Gasto salvo no banco!");

        } catch (Exception e) {

            System.out.println("Erro ao salvar gasto.");
            e.printStackTrace();
        }
    }

    public ArrayList<Gasto> buscarTodos() {

        ArrayList<Gasto> gastos = new ArrayList<>();

        String sql = "SELECT id, descricao, valor FROM gastos";

        try (Connection conexao = BancoDeDados.conectar();
            PreparedStatement comando = conexao.prepareStatement(sql);
            ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {

                int id = resultado.getInt("id");
                String descricao = resultado.getString("descricao");
                double valor = resultado.getDouble("valor");

                Gasto gasto = new Gasto(id, descricao, valor);

                gastos.add(gasto);
            }

        } catch (Exception e) {

            System.out.println("Erro ao buscar gastos.");
            e.printStackTrace();
        }

        return gastos;
    }
public void atualizar(Gasto gasto) {

    String sql = "UPDATE gastos SET descricao = ?, valor = ? WHERE id = ?";

    try (Connection conexao = BancoDeDados.conectar();
        PreparedStatement comando = conexao.prepareStatement(sql)) {

        comando.setString(1, gasto.getDescricao());
        comando.setDouble(2, gasto.getValor());
        comando.setInt(3, gasto.getId());

        comando.executeUpdate();

        System.out.println("Gasto atualizado no banco!");

    } catch (Exception e) {

        System.out.println("Erro ao atualizar gasto.");
        e.printStackTrace();
    }
}
public void excluir(int id) {

    String sql = "DELETE FROM gastos WHERE id = ?";

    try (Connection conexao = BancoDeDados.conectar();
        PreparedStatement comando = conexao.prepareStatement(sql)) {

        comando.setInt(1, id);

        comando.executeUpdate();

        System.out.println("Gasto excluído do banco!");

    } catch (Exception e) {

        System.out.println("Erro ao excluir gasto.");
        e.printStackTrace();
    }
}}