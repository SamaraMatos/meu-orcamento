public class Categoria {

    private int id;
    private String nome;

    public Categoria(String categoria){
        this.nome = categoria;
}

    public Categoria(int id, String categoria){
        this.id = id;
        this.nome = categoria;
}
    
    public int getId(){
        return id;
    }

    public String getCategoria(){
        return nome;
    }

    
}
