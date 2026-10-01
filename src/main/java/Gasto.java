public class Gasto {

    private int id;
    private String descricao;
    private double valor;
    private Categoria categoria;

    public Gasto(String descricao, double valor, Categoria categoria) {

        this.descricao = descricao;
        this.valor = valor;
        this.categoria = categoria;
    }

    public Gasto(int id, String descricao, double valor, Categoria categoria) {

        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.categoria = categoria;
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

    public Categoria getCategoria(){
        
        return categoria;
    }
}

