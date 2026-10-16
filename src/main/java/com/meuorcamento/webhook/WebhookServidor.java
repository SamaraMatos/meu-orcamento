package com.meuorcamento.webhook;

import com.meuorcamento.dao.CategoriaDAO;
import com.meuorcamento.dao.ControleMesDAO;
import com.meuorcamento.dao.GastoDAO;
import com.meuorcamento.dao.HistoricoGastoDAO;
import com.meuorcamento.dao.HistoricoOrcamentoDAO;
import com.meuorcamento.dao.MensagemProcessadaDAO;
import com.meuorcamento.dao.OrcamentoDAO;
import com.meuorcamento.dao.PlanejamentoDAO;
import com.meuorcamento.infraestrutura.BancoDeDados;
import com.meuorcamento.integracao.AssistenteFinanceiro;
import com.meuorcamento.modelo.Categoria;
import com.meuorcamento.modelo.Gasto;
import com.meuorcamento.modelo.Planejamento;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebhookServidor {

        private static final String TOKEN_VERIFICACAO = System.getenv("WHATSAPP_VERIFY_TOKEN");

        private static final String WHATSAPP_TOKEN = System.getenv("WHATSAPP_TOKEN");

        private static final String NUMEROS_AUTORIZADOS = System.getenv("WHATSAPP_NUMEROS_AUTORIZADOS");

        private static final String PHONE_NUMBER_ID = "1226291153911101";

        public static void main(String[] args) throws IOException {

                if (WHATSAPP_TOKEN == null || WHATSAPP_TOKEN.isBlank()) {

                        System.out.println(
                                        "Token do WhatsApp não encontrado.");

                        return;
                }

                System.out.println(
                                "Token do WhatsApp carregado com sucesso.");
                BancoDeDados.criarTabela();

                ControleMesDAO controleMesDAO = new ControleMesDAO();

                String mesAtual = controleMesDAO.buscar();

                System.out.println(
                                "Mês atual no banco: " + mesAtual);

                if (mesAtual == null) {

                        controleMesDAO.salvar("2026-09");
                }

                String portaEnv = System.getenv("PORT");

                int porta = portaEnv != null
                                ? Integer.parseInt(portaEnv)
                                : 8080;

                HttpServer servidor = HttpServer.create(
                                new InetSocketAddress(porta),
                                0);

                servidor.createContext(
                                "/webhook",
                                WebhookServidor::receberRequisicao);

                servidor.start();

                System.out.println(
                                "Webhook iniciado na porta "
                                                + porta
                                                + "/webhook");
        }

        private static void salvarMesAtualNoHistorico() {

                ControleMesDAO controleMesDAO = new ControleMesDAO();

                String mesAtual = controleMesDAO.buscar();

                if (mesAtual == null) {

                        System.out.println(
                                        "Nenhum mês atual cadastrado.");

                        return;
                }

                OrcamentoDAO orcamentoDAO = new OrcamentoDAO();

                Double valorOrcamento = orcamentoDAO.buscar();

                if (valorOrcamento == null) {

                        System.out.println(
                                        "Nenhum orçamento encontrado.");

                        return;
                }

                GastoDAO gastoDAO = new GastoDAO();

                HistoricoGastoDAO historicoGastoDAO = new HistoricoGastoDAO();

                HistoricoOrcamentoDAO historicoOrcamentoDAO = new HistoricoOrcamentoDAO();

                historicoGastoDAO.excluirPorMes(
                                mesAtual);

                for (Gasto gasto : gastoDAO.buscarTodos()) {

                        historicoGastoDAO.salvar(
                                        mesAtual,
                                        gasto);
                }

                historicoOrcamentoDAO.salvar(
                                mesAtual,
                                valorOrcamento);

                System.out.println(
                                "Mês " + mesAtual
                                                + " salvo no histórico com sucesso!");
        }

        private static void receberRequisicao(
                        HttpExchange exchange) throws IOException {

                if (exchange.getRequestMethod()
                                .equalsIgnoreCase("GET")) {

                        verificarWebhook(exchange);
                        return;
                }

                if (exchange.getRequestMethod()
                                .equalsIgnoreCase("POST")) {

                        processarMensagem(exchange);
                        return;
                }

                enviarResposta(
                                exchange,
                                405,
                                "Método não permitido");
        }

        private static void processarMensagem(
                        HttpExchange exchange) throws IOException {

                String corpo = new String(
                                exchange.getRequestBody().readAllBytes(),
                                StandardCharsets.UTF_8);

                System.out.println();
                System.out.println(
                                "===== EVENTO RECEBIDO DA META =====");
                System.out.println(corpo);

                String mensagem = extrairMensagem(corpo);

                String numeroRemetente = extrairRemetente(corpo);

                String idMensagem = extrairIdMensagem(corpo);

                if (mensagem == null || mensagem.isBlank()) {

                        System.out.println();
                        System.out.println(
                                        "Nenhuma mensagem de texto encontrada.");

                        System.out.println(
                                        "==================================");

                        enviarResposta(
                                        exchange,
                                        200,
                                        "EVENT_RECEIVED");

                        return;
                }

                if (!remetenteAutorizado(numeroRemetente, NUMEROS_AUTORIZADOS)) {

                        System.out.println("Mensagem ignorada: remetente não autorizado.");

                        enviarResposta(
                                        exchange,
                                        200,
                                        "EVENT_RECEIVED");

                        return;
                }

                String mensagemNormalizada = normalizarComando(mensagem);

                if (idMensagem != null) {

                        MensagemProcessadaDAO mensagemDAO = new MensagemProcessadaDAO();

                        boolean mensagemNova = mensagemDAO.registrarSeNova(idMensagem);

                        if (!mensagemNova) {

                                System.out.println(
                                                "Mensagem já processada. Ignorando duplicata: "
                                                                + idMensagem);

                                enviarResposta(
                                                exchange,
                                                200,
                                                "EVENT_RECEIVED");

                                return;
                        }
                }

                System.out.println();
                System.out.println("Mensagem recebida:");
                System.out.println(mensagem);

                if (numeroRemetente != null) {

                        System.out.println(
                                        "Remetente: " + numeroRemetente);
                }

                if (mensagemNormalizada.equals("saldo")) {

                        String respostaSaldo = obterSaldo();

                        System.out.println();
                        System.out.println(respostaSaldo);

                        if (numeroRemetente != null) {

                                enviarMensagemWhatsApp(
                                                numeroRemetente,
                                                respostaSaldo);

                        } else {

                                System.out.println();
                                System.out.println(
                                                "Não foi possível identificar o número do remetente.");
                        }

                } else if (mensagemNormalizada.equals("gastos")) {

                        String respostaGastos = listarGastos();

                        System.out.println();
                        System.out.println(respostaGastos);

                        if (numeroRemetente != null) {

                                enviarMensagemWhatsApp(
                                                numeroRemetente,
                                                respostaGastos);
                        }
                } else if (mensagemNormalizada.equals("resumo")) {

                        String respostaResumo;

                        try {
                                respostaResumo = obterResumo();
                        } catch (java.sql.SQLException e) {
                                respostaResumo = "⚠️ Não consegui obter todos os dados para montar seu resumo agora. Tente novamente em instantes.";
                        }

                        if (numeroRemetente != null) {

                                enviarMensagemWhatsApp(
                                                numeroRemetente,
                                                respostaResumo);
                        }

                } else if (mensagemNormalizada.equals("menu")) {
                        String respostaMenu = obterMenu();
                        if (numeroRemetente != null) {

                                enviarMensagemWhatsApp(
                                                numeroRemetente,
                                                respostaMenu);
                        }
                } else if (mensagemNormalizada.startsWith("editar categoria ")) {

                        processarEdicaoCategoria(
                                        mensagem,
                                        numeroRemetente);
                } else if (mensagemNormalizada.startsWith("excluir ")) {

                        processarExclusao(
                                        mensagem,
                                        numeroRemetente);

                } else if (mensagemNormalizada.startsWith("editar ")) {

                        processarEdicao(
                                        mensagem,
                                        numeroRemetente);
                } else if (mensagemNormalizada.startsWith("novo mes ")) {

                        processarNovoMes(
                                        mensagem,
                                        numeroRemetente);
                } else if (mensagemNormalizada.startsWith("nova categoria ")) {

                        processarNovaCategoria(
                                        mensagem,
                                        numeroRemetente);

                } else if (mensagemNormalizada.startsWith("orcamento ")) {

                        processarOrcamento(
                                        mensagem,
                                        numeroRemetente);
                } else if (mensagemNormalizada.equals("categorias")) {

                        String respostaCategorias = listarCategorias();

                        if (numeroRemetente != null) {

                                enviarMensagemWhatsApp(
                                                numeroRemetente,
                                                respostaCategorias);
                        }
                } else if (mensagemNormalizada.startsWith("consultar ")) {

                        String pergunta = mensagem.substring(9).trim();

                        String resposta = AssistenteFinanceiro.consultar(pergunta);

                        if (numeroRemetente != null) {
                                enviarMensagemWhatsApp(
                                                numeroRemetente,
                                                resposta);
                        }
                } else if (mensagemNormalizada.startsWith("planejar ")) {

                        processarPlanejamento(
                                        mensagem,
                                        numeroRemetente);
                } else if (mensagemNormalizada.startsWith("finalizar ")) {

                        processarFinalizacao(
                                        mensagem,
                                        numeroRemetente);
                } else if (mensagemNormalizada.equals("planejamentos")) {

                        String respostaPlanejamentos = listarPlanejamentos();

                        if (numeroRemetente != null) {
                                enviarMensagemWhatsApp(
                                                numeroRemetente,
                                                respostaPlanejamentos);
                        }
                } else {

                        processarGasto(
                                        mensagem,
                                        numeroRemetente);
                }

                System.out.println(
                                "==================================");

                enviarResposta(
                                exchange,
                                200,
                                "EVENT_RECEIVED");
        }

        private static void processarNovoMes(
                        String mensagem,
                        String numeroRemetente) {

                String[] partes = mensagem.trim().split("\\s+");

                if (partes.length != 3) {

                        System.out.println(
                                        "Formato inválido para novo mês.");

                        return;
                }

                try {

                        double novoOrcamento = Double.parseDouble(
                                        partes[2].replace(",", "."));

                        if (novoOrcamento <= 0) {

                                System.out.println(
                                                "O novo orçamento deve ser maior que zero.");

                                return;
                        }

                        ControleMesDAO controleMesDAO = new ControleMesDAO();

                        String mesAtual = controleMesDAO.buscar();

                        if (mesAtual == null) {

                                System.out.println(
                                                "Nenhum mês atual cadastrado.");

                                return;
                        }

                        // Guarda o mês atual antes de limpar
                        salvarMesAtualNoHistorico();

                        GastoDAO gastoDAO = new GastoDAO();

                        boolean gastosExcluidos = gastoDAO.excluirTodos();

                        if (!gastosExcluidos) {

                                System.out.println(
                                                "Não foi possível iniciar o novo mês.");

                                return;
                        }

                        OrcamentoDAO orcamentoDAO = new OrcamentoDAO();

                        orcamentoDAO.salvar(
                                        novoOrcamento);

                        String novoMes = calcularProximoMes(
                                        mesAtual);

                        controleMesDAO.salvar(
                                        novoMes);

                        String resposta = "Novo mês iniciado com sucesso!\n\n"
                                        + obterSaldo();

                        System.out.println();
                        System.out.println(resposta);

                        if (numeroRemetente != null) {

                                enviarMensagemWhatsApp(
                                                numeroRemetente,
                                                resposta);
                        }

                } catch (NumberFormatException e) {

                        System.out.println(
                                        "O valor do orçamento é inválido.");
                }
        }

        private static String listarPlanejamentos() {

                PlanejamentoDAO planejamentoDAO = new PlanejamentoDAO();

                List<Planejamento> planejamentos;

                try {
                        planejamentos = planejamentoDAO.buscarAtivos();
                } catch (java.sql.SQLException e) {
                        return "⚠️ Não consegui consultar seus planejamentos agora. Tente novamente em instantes.";
                }

                if (planejamentos.isEmpty()) {
                        return "📋 Você não possui planejamentos ativos.";
                }

                StringBuilder resposta = new StringBuilder();

                resposta.append("📋 *Planejamentos ativos:*\n\n");

                for (Planejamento planejamento : planejamentos) {

                        resposta.append(planejamento.getId())
                                        .append(" - ")
                                        .append(planejamento.getDescricao())
                                        .append(": R$ ")
                                        .append(String.format("%.2f", planejamento.getValorEstimado()))
                                        .append("\n");
                }

                return resposta.toString();
        }

        private static String calcularProximoMes(
                        String mesAtual) {

                String[] partes = mesAtual.split("-");

                int ano = Integer.parseInt(partes[0]);

                int mes = Integer.parseInt(partes[1]);

                mes++;

                if (mes > 12) {

                        mes = 1;
                        ano++;
                }

                return String.format(
                                "%04d-%02d",
                                ano,
                                mes);
        }

        private static String extrairMensagem(
                        String corpo) {

                String marcador = "\"body\":\"";

                int inicio = corpo.indexOf(marcador);

                if (inicio == -1) {
                        return null;
                }

                inicio += marcador.length();

                int fim = corpo.indexOf("\"", inicio);

                if (fim == -1) {
                        return null;
                }

                String mensagem = corpo.substring(
                                inicio,
                                fim);

                return converterUnicode(mensagem);
        }

        private static void processarNovaCategoria(
                        String mensagem,
                        String numeroRemetente) {

                String nomeCategoria = mensagem.substring("nova categoria ".length()).trim();

                Categoria categoria = new Categoria(nomeCategoria);

                CategoriaDAO categoriaDAO = new CategoriaDAO();

                boolean salvou = categoriaDAO.salvar(categoria);

                if (salvou) {

                        String resposta = "Categoria '" + nomeCategoria
                                        + "' adicionada com sucesso!";

                        System.out.println(resposta);

                        if (numeroRemetente != null) {
                                enviarMensagemWhatsApp(
                                                numeroRemetente,
                                                resposta);
                        }

                } else {

                        String resposta = "Não foi possível adicionar a categoria '"
                                        + nomeCategoria + "'.";

                        System.out.println(resposta);

                        if (numeroRemetente != null) {

                                enviarMensagemWhatsApp(
                                                numeroRemetente,
                                                resposta);

                        } else {

                                System.out.println(
                                                "Não foi possível identificar o número do remetente.");
                        }
                }
        }

        private static void processarPlanejamento(
                        String mensagem,
                        String numeroRemetente) {
                String dadosPlanejamento = mensagem.substring(8).trim();
                int ultimoEspaco = dadosPlanejamento.lastIndexOf(" ");

                if (ultimoEspaco == -1) {
                        enviarMensagemWhatsApp(
                                        numeroRemetente,
                                        "Formato correto: planejar Descrição Valor");
                        return;
                }

                String descricao = dadosPlanejamento.substring(0, ultimoEspaco).trim();

                String valorTexto = dadosPlanejamento.substring(ultimoEspaco + 1).trim();

                double valorEstimado;

                try {
                        valorEstimado = Double.parseDouble(
                                        valorTexto.replace(",", "."));

                } catch (NumberFormatException e) {
                        enviarMensagemWhatsApp(
                                        numeroRemetente,
                                        "Valor inválido. Exemplo: planejar Jantar 150,50");
                        return;
                }

                if (!Double.isFinite(valorEstimado) || valorEstimado <= 0) {
                        enviarMensagemWhatsApp(
                                        numeroRemetente,
                                        "O valor do planejamento deve ser maior que zero.");
                        return;
                }

                Planejamento planejamento = new Planejamento(
                                0,
                                descricao,
                                valorEstimado,
                                "ativo");

                PlanejamentoDAO planejamentoDAO = new PlanejamentoDAO();

                boolean salvou = planejamentoDAO.salvar(planejamento);

                if (salvou) {
                        enviarMensagemWhatsApp(
                                        numeroRemetente,
                                        "✅ Planejamento cadastrado com sucesso!");
                } else {
                        enviarMensagemWhatsApp(
                                        numeroRemetente,
                                        "❌ Não foi possível cadastrar o planejamento.");
                }

                return;
        }

        private static void processarFinalizacao(
                        String mensagem,
                        String numeroRemetente) {
                String idTexto = mensagem.substring(9).trim();

                int id;

                try {
                        id = Integer.parseInt(idTexto);

                } catch (NumberFormatException e) {
                        enviarMensagemWhatsApp(
                                        numeroRemetente,
                                        "ID inválido. Exemplo: finalizar 3");
                        return;
                }
                if (id <= 0) {
                        enviarMensagemWhatsApp(
                                        numeroRemetente,
                                        "O ID do planejamento deve ser maior que zero.");
                        return;
                }

                PlanejamentoDAO planejamentoDAO = new PlanejamentoDAO();

                boolean encerrou = planejamentoDAO.encerrar(id);

                if (encerrou) {
                        enviarMensagemWhatsApp(
                                        numeroRemetente,
                                        "✅ Planejamento finalizado com sucesso!");
                } else {
                        enviarMensagemWhatsApp(
                                        numeroRemetente,
                                        "❌ Não foi possível finalizar. Verifique se o ID existe e está ativo.");
                }
        }

        private static String extrairIdMensagem(String corpo) {

                int inicioMessages = corpo.indexOf("\"messages\":[");

                if (inicioMessages == -1) {
                        return null;
                }

                String marcador = "\"id\":\"";

                int inicio = corpo.indexOf(
                                marcador,
                                inicioMessages);

                if (inicio == -1) {
                        return null;
                }

                inicio += marcador.length();

                int fim = corpo.indexOf("\"", inicio);

                if (fim == -1) {
                        return null;
                }

                return corpo.substring(inicio, fim);
        }

        private static String normalizarComando(String texto) {

                return Normalizer.normalize(
                                texto,
                                Normalizer.Form.NFD)
                                .replaceAll("\\p{M}", "")
                                .toLowerCase()
                                .trim();
        }

        private static String converterUnicode(
                        String texto) {

                StringBuilder resultado = new StringBuilder();

                for (int i = 0; i < texto.length(); i++) {

                        if (texto.charAt(i) == '\\'
                                        && i + 5 < texto.length()
                                        && texto.charAt(i + 1) == 'u') {

                                String codigo = texto.substring(
                                                i + 2,
                                                i + 6);

                                try {

                                        char caractere = (char) Integer.parseInt(
                                                        codigo,
                                                        16);

                                        resultado.append(caractere);

                                        i += 5;

                                } catch (NumberFormatException e) {

                                        resultado.append(
                                                        texto.charAt(i));
                                }

                        } else {

                                resultado.append(
                                                texto.charAt(i));
                        }
                }

                return resultado.toString();
        }

        static boolean remetenteAutorizado(
                        String numeroRemetente,
                        String numerosAutorizados) {

                if (numeroRemetente == null || numeroRemetente.isBlank()) {
                        return false;
                }

                if (numerosAutorizados == null || numerosAutorizados.isBlank()) {
                        return false;
                }

                String[] numeros = numerosAutorizados.split(",");

                for (String numero : numeros) {
                        if (numero.trim().equals(numeroRemetente)) {
                                return true;
                        }
                }

                return false;
        }

        private static String extrairRemetente(
                        String corpo) {

                String marcador = "\"from\":\"";

                int inicio = corpo.indexOf(marcador);

                if (inicio == -1) {
                        return null;
                }

                inicio += marcador.length();

                int fim = corpo.indexOf("\"", inicio);

                if (fim == -1) {
                        return null;
                }

                return corpo.substring(
                                inicio,
                                fim);
        }

        private static void processarEdicaoCategoria(
                        String mensagem,
                        String numeroRemetente) {
                String dados = mensagem.substring(
                                "editar categoria ".length()).trim();

                String[] partes = dados.split("\\s+", 2);

                if (partes.length < 2) {

                        enviarMensagemWhatsApp(
                                        numeroRemetente,
                                        "⚠️ Formato inválido. Use: editar categoria <ID> <novo nome>");

                        return;
                }

                int idCategoria;

                try {

                        idCategoria = Integer.parseInt(partes[0]);

                } catch (NumberFormatException e) {

                        enviarMensagemWhatsApp(
                                        numeroRemetente,
                                        "⚠️ ID da categoria inválido.");
                        return;
                }

                String novoNome = partes[1].trim();

                if (novoNome.isEmpty()) {

                        enviarMensagemWhatsApp(
                                        numeroRemetente,
                                        "⚠️ Informe o novo nome da categoria.");

                        return;
                }

                CategoriaDAO categoriaDAO = new CategoriaDAO();

                Categoria categoriaExistente = categoriaDAO.buscarPorId(idCategoria);

                if (categoriaExistente == null) {
                        enviarMensagemWhatsApp(
                                        numeroRemetente,
                                        "⚠️ Categoria não encontrada.");
                        return;

                }

                if (categoriaExistente.getNome().equalsIgnoreCase("Outros")) {

                        enviarMensagemWhatsApp(
                                        numeroRemetente,
                                        "⚠️ A categoria Outros é padrão do sistema e não pode ser renomeada.");

                        return;
                }

                Categoria categoria = new Categoria(idCategoria, novoNome);

                boolean atualizou = categoriaDAO.atualizar(categoria);

                if (atualizou) {

                        enviarMensagemWhatsApp(
                                        numeroRemetente,
                                        "✅ Categoria atualizada com sucesso!");

                } else {

                        enviarMensagemWhatsApp(
                                        numeroRemetente,
                                        "⚠️ Não foi possível atualizar a categoria.");
                }
        }

        private static void processarGasto(
                        String mensagem,
                        String numeroRemetente) {
                CategoriaDAO categoriaDAO = new CategoriaDAO();
                List<Categoria> categorias = categoriaDAO.buscarTodos();
                categorias.sort(
                                (a, b) -> Integer.compare(
                                                b.getNome().length(),
                                                a.getNome().length()));

                String mensagemParaProcessar = mensagem.trim();

                String[] partesOriginais = mensagemParaProcessar.split("\\s+");

                String ultimoItemOriginal = partesOriginais[partesOriginais.length - 1];

                boolean ultimoItemEhNumero = false;

                try {
                        Double.parseDouble(ultimoItemOriginal.replace(",", "."));
                        ultimoItemEhNumero = true;
                } catch (NumberFormatException e) {
                }

                Categoria categoriaEncontrada = null;

                if (ultimoItemEhNumero) {

                        categoriaEncontrada = categoriaDAO.buscarPorNome("Outros");
                }

                if (!ultimoItemEhNumero) {

                        for (Categoria categoria : categorias) {

                                String nomeCategoria = categoria.getNome().toLowerCase();

                                if (mensagemParaProcessar.toLowerCase().endsWith(" " + nomeCategoria)) {
                                        mensagemParaProcessar = mensagemParaProcessar.substring(
                                                        0,
                                                        mensagemParaProcessar.length()
                                                                        - nomeCategoria.length())
                                                        .trim();

                                        categoriaEncontrada = categoria;

                                        break;

                                }
                        }
                }

                if (categoriaEncontrada == null) {

                        String resposta = "⚠️ Categoria não encontrada. "
                                        + "Use 'categorias' para ver a lista de categorias disponíveis.";

                        if (numeroRemetente != null) {

                                enviarMensagemWhatsApp(
                                                numeroRemetente,
                                                resposta);
                        }

                        return;
                }

                String[] partesMensagem = mensagemParaProcessar.trim().split("\\s+");

                if (partesMensagem.length < 2) {

                        System.out.println();
                        System.out.println(
                                        "A mensagem não está no formato esperado.");

                        if (numeroRemetente != null) {
                                enviarMensagemWhatsApp(
                                                numeroRemetente,
                                                "⚠️ Informe a descrição e o valor do gasto.\n"
                                                                + "Exemplo: Mercado 150 Casa");
                        }

                        return;
                }

                String ultimoItem = partesMensagem[partesMensagem.length - 1];

                try {

                        double valor = Double.parseDouble(
                                        ultimoItem.replace(",", "."));

                        if (valor <= 0) {

                                System.out.println();
                                System.out.println(
                                                "O valor do gasto deve ser maior que zero.");

                                if (numeroRemetente != null) {

                                        enviarMensagemWhatsApp(
                                                        numeroRemetente,
                                                        "⚠️ O valor do gasto deve ser maior que zero.\n"
                                                                        + "Exemplo: Mercado 150 Casa");
                                }

                                return;
                        }

                        int posicaoValor = mensagemParaProcessar.lastIndexOf(
                                        ultimoItem);

                        String descricao = mensagemParaProcessar.substring(
                                        0,
                                        posicaoValor).trim();

                        if (descricao.isBlank()) {

                                System.out.println();
                                System.out.println(
                                                "A descrição do gasto não pode ficar vazia.");

                                return;
                        }

                        System.out.println();
                        System.out.println(
                                        "Descrição: " + descricao);

                        System.out.println(
                                        "Valor: R$ " + valor);

                        Gasto gasto = new Gasto(
                                        descricao,
                                        valor,
                                        categoriaEncontrada);

                        GastoDAO gastoDAO = new GastoDAO();

                        boolean salvo = gastoDAO.salvar(gasto);

                        System.out.println();

                        if (salvo) {

                                System.out.println(
                                                "Gasto recebido pelo WhatsApp e salvo no banco!");

                                String resposta = "Gasto adicionado com sucesso!\n"
                                                + descricao
                                                + " - R$ "
                                                + String.format("%.2f", valor)
                                                + "\n"
                                                + obterSaldo();

                                if (numeroRemetente != null) {

                                        enviarMensagemWhatsApp(
                                                        numeroRemetente,
                                                        resposta);
                                }

                        } else {

                                System.out.println(
                                                "Não foi possível salvar o gasto no banco.");
                        }

                } catch (NumberFormatException e) {

                        System.out.println();
                        System.out.println(
                                        "A mensagem não está no formato esperado.");
                }
        }

        private static void processarExclusao(
                        String mensagem,
                        String numeroRemetente) {

                String[] partes = mensagem.trim().split("\\s+");

                if (partes.length != 2) {

                        System.out.println(
                                        "Formato inválido para exclusão.");

                        return;
                }

                try {

                        int id = Integer.parseInt(partes[1]);

                        GastoDAO gastoDAO = new GastoDAO();

                        boolean excluido = gastoDAO.excluir(id);

                        String resposta;

                        if (excluido) {

                                resposta = "Gasto excluído com sucesso!\n"
                                                + "ID: "
                                                + id
                                                + "\n"
                                                + obterSaldo();

                        } else {

                                resposta = "Não existe nenhum gasto com o ID "
                                                + id
                                                + ".";
                        }

                        System.out.println();
                        System.out.println(resposta);

                        if (numeroRemetente != null) {

                                enviarMensagemWhatsApp(
                                                numeroRemetente,
                                                resposta);
                        }

                } catch (NumberFormatException e) {

                        System.out.println(
                                        "O ID precisa ser um número.");
                }
        }

        private static void processarEdicao(
                        String mensagem,
                        String numeroRemetente) {

                String[] partes = mensagem.trim().split("\\s+");

                if (partes.length < 4) {

                        System.out.println(
                                        "Formato inválido para edição.");

                        return;
                }

                try {

                        int id = Integer.parseInt(partes[1]);

                        String ultimoItem = partes[partes.length - 1];

                        double valor = Double.parseDouble(
                                        ultimoItem.replace(",", "."));

                        if (valor <= 0) {

                                System.out.println(
                                                "O valor do gasto deve ser maior que zero.");

                                return;
                        }

                        int inicioDescricao = mensagem.indexOf(partes[2]);

                        int posicaoValor = mensagem.lastIndexOf(ultimoItem);

                        String descricao = mensagem.substring(
                                        inicioDescricao,
                                        posicaoValor).trim();

                        if (descricao.isBlank()) {

                                System.out.println(
                                                "A descrição não pode ficar vazia.");

                                return;
                        }

                        GastoDAO gastoDAO = new GastoDAO();

                        Gasto gastoAntigo = gastoDAO.buscarPorId(id);

                        if (gastoAntigo == null) {

                                String resposta = "Não existe nenhum gasto com o ID "
                                                + id
                                                + ".";

                                if (numeroRemetente != null) {

                                        enviarMensagemWhatsApp(
                                                        numeroRemetente,
                                                        resposta);
                                }

                                return;
                        }
                        Categoria categoria = gastoAntigo.getCategoria();

                        Gasto gasto = new Gasto(
                                        id,
                                        descricao,
                                        valor,
                                        categoria);

                        boolean atualizado = gastoDAO.atualizar(gasto);

                        String resposta;

                        if (atualizado) {

                                resposta = "Gasto atualizado com sucesso!\n"
                                                + "ID: "
                                                + id
                                                + "\n"
                                                + descricao
                                                + " - R$ "
                                                + String.format("%.2f", valor)
                                                + "\n"
                                                + obterSaldo();

                        } else {

                                resposta = "Não existe nenhum gasto com o ID "
                                                + id
                                                + ".";
                        }

                        System.out.println();
                        System.out.println(resposta);

                        if (numeroRemetente != null) {

                                enviarMensagemWhatsApp(
                                                numeroRemetente,
                                                resposta);
                        }

                } catch (NumberFormatException e) {

                        System.out.println(
                                        "ID ou valor inválido.");
                }
        }

        private static void processarOrcamento(
                        String mensagem,
                        String numeroRemetente) {

                String[] partes = mensagem.trim().split("\\s+");

                if (partes.length != 2) {

                        System.out.println(
                                        "Formato inválido para orçamento.");

                        return;
                }

                try {

                        double valor = Double.parseDouble(
                                        partes[1].replace(",", "."));

                        if (valor <= 0) {

                                System.out.println(
                                                "O orçamento deve ser maior que zero.");

                                return;
                        }

                        OrcamentoDAO orcamentoDAO = new OrcamentoDAO();

                        orcamentoDAO.salvar(valor);

                        String resposta = "Orçamento alterado com sucesso!\n\n"
                                        + obterSaldo();

                        System.out.println();
                        System.out.println(resposta);

                        if (numeroRemetente != null) {

                                enviarMensagemWhatsApp(
                                                numeroRemetente,
                                                resposta);
                        }

                } catch (NumberFormatException e) {

                        System.out.println(
                                        "O valor do orçamento é inválido.");
                }
        }

        private static String listarGastos() {

                GastoDAO gastoDAO = new GastoDAO();

                if (gastoDAO.buscarTodos().isEmpty()) {

                        return "Nenhum gasto cadastrado.";
                }

                String resposta = "GASTOS CADASTRADOS\n\n";

                for (Gasto gasto : gastoDAO.buscarTodos()) {

                        resposta += "ID: "
                                        + gasto.getId()
                                        + " - "
                                        + gasto.getDescricao()
                                        + " - R$ "
                                        + String.format(
                                                        "%.2f",
                                                        gasto.getValor())
                                        + "\n";
                }

                return resposta;
        }

        private static String listarCategorias() {
                CategoriaDAO categoriaDAO = new CategoriaDAO();
                List<Categoria> categorias = categoriaDAO.buscarTodos();
                String resposta = "CATEGORIAS\n\n";
                for (Categoria categoria : categorias) {
                        resposta += "ID: "
                                        + categoria.getId()
                                        + " - "
                                        + categoria.getNome()
                                        + "\n";

                }
                return resposta;

        }

        public static String obterResumo() throws java.sql.SQLException {
                OrcamentoDAO orcamentoDAO = new OrcamentoDAO();

                Double valorOrcamento = orcamentoDAO.buscar();

                GastoDAO gastoDAO = new GastoDAO();

                List<Gasto> gastos = gastoDAO.buscarTodos();

                int quantidadeGastos = gastos.size();

                double totalGastos = 0;

                Map<String, Double> totaisPorCategoria = new HashMap<>();

                for (Gasto gasto : gastos) {
                        totalGastos += gasto.getValor();

                        String nomeCategoria = gasto.getCategoria().getNome();

                        double totalCategoria = totaisPorCategoria.getOrDefault(
                                        nomeCategoria,
                                        0.0);
                        totaisPorCategoria.put(
                                        nomeCategoria,
                                        totalCategoria + gasto.getValor());
                }

                double mediaGastos;
                if (quantidadeGastos > 0) {
                        mediaGastos = totalGastos / quantidadeGastos;
                } else {
                        mediaGastos = 0;
                }

                Gasto maiorGasto = null;
                for (Gasto gasto : gastos) {
                        if (maiorGasto == null || gasto.getValor() > maiorGasto.getValor()) {
                                maiorGasto = gasto;
                        }
                }

                double orcamentoAtual;
                if (valorOrcamento != null) {
                        orcamentoAtual = valorOrcamento;
                } else {
                        orcamentoAtual = 0;
                }

                double saldo = orcamentoAtual - totalGastos;

                PlanejamentoDAO planejamentoDAO = new PlanejamentoDAO();

                List<Planejamento> planejamentos = planejamentoDAO.buscarAtivos();

                double totalPlanejado = 0;

                for (Planejamento planejamento : planejamentos) {
                        totalPlanejado += planejamento.getValorEstimado();
                }

                double saldoProjetado = saldo - totalPlanejado;

                String resposta = ("Resumo do mês\n\n");
                resposta += String.format("Orçamento: R$ %.2f\n",
                                orcamentoAtual);
                resposta += String.format("Total de gastos: R$ %.2f\n",
                                totalGastos);
                resposta += String.format("Quantidade de gastos: %d\n",
                                quantidadeGastos);
                resposta += String.format("Média de gastos: R$ %.2f\n",
                                mediaGastos);
                if (maiorGasto != null) {
                        resposta += String.format(
                                        "Maior gasto: ID %d - %s - R$ %.2f\n",
                                        maiorGasto.getId(),
                                        maiorGasto.getDescricao(),
                                        maiorGasto.getValor());
                } else {
                        resposta += "Maior gasto: Nenhum gasto registrado.\n";
                }
                resposta += "\n🏷️ Gastos por categoria:\n";

                for (String categoria : totaisPorCategoria.keySet()) {
                        resposta += String.format(
                                        "%s: R$ %.2f\n",
                                        categoria,
                                        totaisPorCategoria.get(categoria));

                }
                resposta += "\n";
                if (saldo >= 0) {
                        resposta += "Saldo disponível: R$ "
                                        + String.format("%.2f", saldo);
                } else {
                        resposta += "⚠️ Orçamento ultrapassado em R$ "
                                        + String.format("%.2f", Math.abs(saldo));
                }

                if (!planejamentos.isEmpty()) {
                        resposta += "\n\n📅 *PLANEJAMENTOS ATIVOS*\n";

                        for (Planejamento planejamento : planejamentos) {
                                resposta += String.format(
                                                "%d - %s: R$ %.2f\n",
                                                planejamento.getId(),
                                                planejamento.getDescricao(),
                                                planejamento.getValorEstimado());
                        }
                        resposta += String.format(
                                        "\nTotal planejado: R$ %.2f\n",
                                        totalPlanejado);

                        resposta += String.format(
                                        "Saldo projetado: R$ %.2f\n",
                                        saldoProjetado);

                        resposta += "\n💡 Valores planejados não são gastos realizados.";
                }

                return resposta;
        }

        private static String obterMenu() {

                String resposta = "💰 *MEU ORÇAMENTO*\n\n";

                resposta += "💸 *ADICIONAR GASTO*\n";
                resposta += "Envie a descrição, o valor e, se quiser, a categoria.\n";
                resposta += "Ex: Mercado 150 Casa\n";
                resposta += "Ex: Mercado 150\n\n";

                resposta += "📊 *CONSULTAS*\n";
                resposta += "💵 saldo — Ver saldo disponível\n";
                resposta += "🧾 gastos — Ver gastos cadastrados\n";
                resposta += "📈 resumo — Ver resumo do mês\n";
                resposta += "🏷️ categorias — Ver categorias\n\n";

                resposta += "📅 *PLANEJAMENTOS*\n";
                resposta += "➕ planejar <descrição> <valor>\n";
                resposta += "Ex: planejar Jantar 150\n\n";

                resposta += "📋 planejamentos — Ver planejamentos ativos\n\n";

                resposta += "✅ finalizar <ID>\n";
                resposta += "Ex: finalizar 1\n\n";

                resposta += "🏷️ *GERENCIAR CATEGORIAS*\n";
                resposta += "➕ nova categoria <nome>\n";
                resposta += "Ex: nova categoria Animais\n\n";

                resposta += "✏️ editar categoria <ID> <novo nome>\n";
                resposta += "Ex: editar categoria 31 Animais\n\n";

                resposta += "✏️ *GERENCIAR GASTOS*\n";
                resposta += "🗑️ excluir <ID>\n";
                resposta += "Ex: excluir 26\n\n";

                resposta += "✏️ editar <ID> <descrição> <valor>\n";
                resposta += "Ex: editar 26 Mercado 150\n\n";

                resposta += "💰 *ORÇAMENTO*\n";
                resposta += "🔄 orcamento <valor>\n";
                resposta += "Ex: orcamento 2000\n\n";

                resposta += "📅 *NOVO MÊS*\n";
                resposta += "🗓️ novo mes <orçamento>\n";
                resposta += "Ex: novo mes 2000\n\n";

                resposta += "❓ Digite *menu* para ver estas opções novamente.";

                return resposta;
        }

        public static String obterSaldo() {

                OrcamentoDAO orcamentoDAO = new OrcamentoDAO();

                GastoDAO gastoDAO = new GastoDAO();

                Double valorOrcamento = orcamentoDAO.buscar();

                if (valorOrcamento == null) {

                        return "Nenhum orçamento cadastrado.";
                }

                double totalGastos = 0;

                for (Gasto gasto : gastoDAO.buscarTodos()) {

                        totalGastos += gasto.getValor();
                }

                double saldo = valorOrcamento - totalGastos;

                String resposta = "MEU ORÇAMENTO\n"
                                + "Orçamento: R$ "
                                + String.format("%.2f", valorOrcamento)
                                + "\n"
                                + "Total gasto: R$ "
                                + String.format("%.2f", totalGastos)
                                + "\n"
                                + "Saldo disponível: R$ "
                                + String.format("%.2f", saldo);

                if (saldo < 0) {

                        resposta += "\n\nAtenção! Você ultrapassou "
                                        + "o orçamento em R$ "
                                        + String.format(
                                                        "%.2f",
                                                        Math.abs(saldo));
                }

                return resposta;
        }

        private static void enviarMensagemWhatsApp(
                        String numero,
                        String mensagem) {

                try {

                        String url = "https://graph.facebook.com/v26.0/"
                                        + PHONE_NUMBER_ID
                                        + "/messages";

                        String mensagemJson = escaparJson(mensagem);

                        String json = "{"
                                        + "\"messaging_product\":\"whatsapp\","
                                        + "\"to\":\"" + numero + "\","
                                        + "\"type\":\"text\","
                                        + "\"text\":{"
                                        + "\"body\":\"" + mensagemJson + "\""
                                        + "}"
                                        + "}";

                        HttpClient cliente = HttpClient.newHttpClient();

                        HttpRequest requisicao = HttpRequest.newBuilder()
                                        .uri(URI.create(url))
                                        .header(
                                                        "Authorization",
                                                        "Bearer "
                                                                        + WHATSAPP_TOKEN)
                                        .header(
                                                        "Content-Type",
                                                        "application/json")
                                        .POST(
                                                        HttpRequest.BodyPublishers
                                                                        .ofString(json))
                                        .build();

                        HttpResponse<String> resposta = cliente.send(
                                        requisicao,
                                        HttpResponse.BodyHandlers
                                                        .ofString());

                        System.out.println();
                        System.out.println(
                                        "===== ENVIO PARA O WHATSAPP =====");

                        System.out.println(
                                        "Status HTTP: "
                                                        + resposta.statusCode());

                        System.out.println(
                                        "Resposta da Meta: "
                                                        + resposta.body());

                        System.out.println(
                                        "================================");

                } catch (Exception e) {

                        System.out.println();
                        System.out.println(
                                        "Erro ao enviar mensagem pelo WhatsApp.");

                        e.printStackTrace();
                }
        }

        private static String escaparJson(
                        String texto) {

                return texto
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\"")
                                .replace("\n", "\\n")
                                .replace("\r", "\\r");
        }

        private static void verificarWebhook(
                        HttpExchange exchange) throws IOException {

                String query = exchange.getRequestURI().getQuery();

                if (query == null) {

                        enviarResposta(
                                        exchange,
                                        200,
                                        "MeuOrcamento webhook funcionando");

                        return;
                }

                String modo = null;
                String token = null;
                String desafio = null;

                String[] parametros = query.split("&");

                for (String parametro : parametros) {

                        String[] partes = parametro.split("=", 2);

                        if (partes.length != 2) {
                                continue;
                        }

                        switch (partes[0]) {

                                case "hub.mode":
                                        modo = partes[1];
                                        break;

                                case "hub.verify_token":
                                        token = partes[1];
                                        break;

                                case "hub.challenge":
                                        desafio = partes[1];
                                        break;
                        }
                }

                if ("subscribe".equals(modo)
                                && TOKEN_VERIFICACAO.equals(token)
                                && desafio != null) {

                        System.out.println(
                                        "Webhook verificado pela Meta!");

                        enviarResposta(
                                        exchange,
                                        200,
                                        desafio);

                } else {

                        System.out.println(
                                        "Falha na verificação do webhook.");

                        enviarResposta(
                                        exchange,
                                        403,
                                        "Token de verificação inválido");
                }
        }

        private static void enviarResposta(
                        HttpExchange exchange,
                        int status,
                        String resposta) throws IOException {

                byte[] dados = resposta.getBytes(
                                StandardCharsets.UTF_8);

                exchange.sendResponseHeaders(
                                status,
                                dados.length);

                try (OutputStream output = exchange.getResponseBody()) {

                        output.write(dados);
                }
        }
}