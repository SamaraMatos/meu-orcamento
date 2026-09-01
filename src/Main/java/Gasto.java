public class Gasto {

    private int id;
    private String descricao;
    private double valor;

    public Gasto(String descricao, double valor) {

        this.descricao = descricao;
        this.valor = valor;
    }

    public Gasto(int id, String descricao, double valor) {

        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
    }

    public int getId() {

        return id;
    }

    public String getDescricao() {

        return descricao;
    }

    public double getValor() {

        return valor;
    }
}