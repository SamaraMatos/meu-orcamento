public class Planejamento {

    private int id;
    private String descricao;
    private double valorEstimado;
    private String status;

    public Planejamento(int id, String descricao, double valorEstimado, String status) {
        this.id = id;
        this.descricao = descricao;
        this.valorEstimado = valorEstimado;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getValorEstimado() {
        return valorEstimado;
    }

    public String getStatus() {
        return status;
    }
}
