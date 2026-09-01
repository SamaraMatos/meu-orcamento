import java.util.ArrayList;

public class Orcamento {

    private double valor;
    private ArrayList<Gasto> gastos;

    public Orcamento(double valor) {

        this.valor = valor;
        this.gastos = new ArrayList<>();

    }

    public void adicionarGasto(Gasto gasto) {

        gastos.add(gasto);

    }

    public double calcularTotalGastos() {

        double total = 0;

        for (Gasto gasto : gastos) {

            total = total + gasto.getValor();

        }

        return total;

    }

    public double calcularSaldo() {

        return valor - calcularTotalGastos();

    }

    public double getValor() {

        return valor;

    }

    public ArrayList<Gasto> getGastos() {

        return gastos;

    }
}