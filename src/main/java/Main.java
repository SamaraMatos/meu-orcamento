import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        

        Scanner scanner = new Scanner(System.in);

        GastoDAO gastoDAO = new GastoDAO();
        OrcamentoDAO orcamentoDAO = new OrcamentoDAO();

        BancoDeDados.criarTabela();

        System.out.println("===== MEU ORÇAMENTO =====");

        Double valorSalvo = orcamentoDAO.buscar();

        double valorOrcamento;

        if (valorSalvo == null) {

            do {

                System.out.print(
                    "Digite quanto você tem disponível para este mês: R$ "
                );

                valorOrcamento = scanner.nextDouble();

                if (valorOrcamento <= 0) {

                    System.out.println(
                        "O orçamento deve ser maior que zero."
                    );
                }

            } while (valorOrcamento <= 0);

            orcamentoDAO.salvar(valorOrcamento);

        } else {

            valorOrcamento = valorSalvo;

            System.out.println(
                "Orçamento carregado: R$ "
                + valorOrcamento
            );
        }

        Orcamento meuOrcamento =
            new Orcamento(valorOrcamento);

        ArrayList<Gasto> gastosDoBanco =
            gastoDAO.buscarTodos();

        for (Gasto gasto : gastosDoBanco) {

            meuOrcamento.adicionarGasto(gasto);
        }

        int opcao = 0;

        while (opcao != 7) {

            System.out.println();
            System.out.println("===== MENU =====");
            System.out.println("1 - Adicionar gasto");
            System.out.println("2 - Ver gastos");
            System.out.println("3 - Ver saldo");
            System.out.println("4 - Editar gasto");
            System.out.println("5 - Excluir gasto");
            System.out.println("6 - Alterar orçamento");
            System.out.println("7 - Sair");

            System.out.print("Escolha uma opção: ");

            opcao = scanner.nextInt();

            switch (opcao) {

                case 1:

                    scanner.nextLine();

                    System.out.print(
                        "Digite a descrição do gasto: "
                    );

                    String descricao =
                        scanner.nextLine();

                    if (descricao.trim().isEmpty()) {

                        System.out.println(
                            "A descrição não pode ficar vazia."
                        );

                    } else {

                        System.out.print(
                            "Digite o valor do gasto: R$ "
                        );

                        double valor =
                            scanner.nextDouble();

                        if (valor <= 0) {

                            System.out.println(
                                "O valor deve ser maior que zero."
                            );

                        } else {

                            Gasto gasto =
                                new Gasto(
                                    descricao,
                                    valor
                                );

                            boolean salvo =
                                gastoDAO.salvar(gasto);

                            if (salvo) {

                                System.out.println(
                                    "Gasto adicionado com sucesso!"
                                );

                            } else {

                                System.out.println(
                                    "Não foi possível adicionar o gasto."
                                );
                            }
                        }
                    }

                    break;

                case 2:

                    System.out.println();

                    System.out.println(
                        "===== GASTOS CADASTRADOS ====="
                    );

                    ArrayList<Gasto> gastos =
                        gastoDAO.buscarTodos();

                    if (gastos.isEmpty()) {

                        System.out.println(
                            "Nenhum gasto cadastrado."
                        );

                    } else {

                        for (Gasto gastoAtual : gastos) {

                            System.out.println(
                                "ID: "
                                + gastoAtual.getId()
                                + " - "
                                + gastoAtual.getDescricao()
                                + " - R$ "
                                + gastoAtual.getValor()
                            );
                        }
                    }

                    break;

                case 3:

                    ArrayList<Gasto> gastosAtualizados =
                        gastoDAO.buscarTodos();

                    Orcamento orcamentoAtualizado =
                        new Orcamento(
                            valorOrcamento
                        );

                    for (Gasto gastoAtual :
                            gastosAtualizados) {

                        orcamentoAtualizado
                            .adicionarGasto(
                                gastoAtual
                            );
                    }

                    double saldo =
                        orcamentoAtualizado
                            .calcularSaldo();

                    System.out.println();

                    System.out.println(
                        "===== SALDO ====="
                    );

                    System.out.println(
                        "Orçamento: R$ "
                        + orcamentoAtualizado
                            .getValor()
                    );

                    System.out.println(
                        "Total gasto: R$ "
                        + orcamentoAtualizado
                            .calcularTotalGastos()
                    );

                    System.out.println(
                        "Saldo disponível: R$ "
                        + saldo
                    );

                    if (saldo < 0) {

                        System.out.println();
                        System.out.println(
                            "⚠️ ATENÇÃO!"
                        );

                        System.out.println(
                            "Você ultrapassou seu orçamento."
                        );

                        System.out.println(
                            "Valor ultrapassado: R$ "
                            + Math.abs(saldo)
                        );

                    } else {

                        System.out.println();

                        System.out.println(
                            "✅ Você ainda possui saldo disponível."
                        );
                    }

                    break;

                case 4:

                    System.out.println();

                    System.out.println(
                        "===== EDITAR GASTO ====="
                    );

                    ArrayList<Gasto> gastosEditar =
                        gastoDAO.buscarTodos();

                    if (gastosEditar.isEmpty()) {

                        System.out.println(
                            "Nenhum gasto cadastrado."
                        );

                    } else {

                        for (Gasto gastoAtual :
                                gastosEditar) {

                            System.out.println(
                                "ID: "
                                + gastoAtual.getId()
                                + " - "
                                + gastoAtual.getDescricao()
                                + " - R$ "
                                + gastoAtual.getValor()
                            );
                        }

                        System.out.print(
                            "Digite o ID do gasto que deseja editar: "
                        );

                        int idEditar =
                            scanner.nextInt();

                        scanner.nextLine();

                        Gasto gastoSelecionado =
                            null;

                        for (Gasto gastoAtual :
                                gastosEditar) {

                            if (gastoAtual.getId()
                                    == idEditar) {

                                gastoSelecionado =
                                    gastoAtual;

                                break;
                            }
                        }

                        if (gastoSelecionado == null) {

                            System.out.println(
                                "Gasto não encontrado."
                            );

                        } else {

                            System.out.print(
                                "Digite a nova descrição: "
                            );

                            String novaDescricao =
                                scanner.nextLine();

                            if (novaDescricao
                                    .trim()
                                    .isEmpty()) {

                                System.out.println(
                                    "A descrição não pode ficar vazia."
                                );

                            } else {

                                System.out.print(
                                    "Digite o novo valor: R$ "
                                );

                                double novoValor =
                                    scanner.nextDouble();

                                if (novoValor <= 0) {

                                    System.out.println(
                                        "O valor deve ser maior que zero."
                                    );

                                } else {

                                    Gasto gastoEditado =
                                        new Gasto(
                                            idEditar,
                                            novaDescricao,
                                            novoValor
                                        );

                                    gastoDAO.atualizar(
                                        gastoEditado
                                    );

                                    System.out.println(
                                        "Gasto editado com sucesso!"
                                    );
                                }
                            }
                        }
                    }

                    break;

                case 5:

                    System.out.println();

                    System.out.println(
                        "===== EXCLUIR GASTO ====="
                    );

                    ArrayList<Gasto> gastosExcluir =
                        gastoDAO.buscarTodos();

                    if (gastosExcluir.isEmpty()) {

                        System.out.println(
                            "Nenhum gasto cadastrado."
                        );

                    } else {

                        for (Gasto gastoAtual :
                                gastosExcluir) {

                            System.out.println(
                                "ID: "
                                + gastoAtual.getId()
                                + " - "
                                + gastoAtual.getDescricao()
                                + " - R$ "
                                + gastoAtual.getValor()
                            );
                        }

                        System.out.print(
                            "Digite o ID do gasto que deseja excluir: "
                        );

                        int idExcluir =
                            scanner.nextInt();

                        boolean gastoExiste =
                            false;

                        for (Gasto gastoAtual :
                                gastosExcluir) {

                            if (gastoAtual.getId()
                                    == idExcluir) {

                                gastoExiste = true;
                                break;
                            }
                        }

                        if (gastoExiste) {

                            gastoDAO.excluir(
                                idExcluir
                            );

                            System.out.println(
                                "Gasto excluído com sucesso!"
                            );

                        } else {

                            System.out.println(
                                "Gasto não encontrado."
                            );
                        }
                    }

                    break;

                case 6:

                    System.out.println();

                    System.out.println(
                        "===== ALTERAR ORÇAMENTO ====="
                    );

                    double novoOrcamento;

                    do {

                        System.out.print(
                            "Digite o novo valor do orçamento: R$ "
                        );

                        novoOrcamento =
                            scanner.nextDouble();

                        if (novoOrcamento <= 0) {

                            System.out.println(
                                "O orçamento deve ser maior que zero."
                            );
                        }

                    } while (novoOrcamento <= 0);

                    orcamentoDAO.salvar(
                        novoOrcamento
                    );

                    valorOrcamento =
                        novoOrcamento;

                    meuOrcamento =
                        new Orcamento(
                            valorOrcamento
                        );

                    System.out.println(
                        "Orçamento alterado com sucesso!"
                    );

                    break;

                case 7:

                    System.out.println();

                    System.out.println(
                        "Encerrando o programa..."
                    );

                    break;

                default:

                    System.out.println(
                        "Opção inválida."
                    );
            }
        }

        scanner.close();
    }
}