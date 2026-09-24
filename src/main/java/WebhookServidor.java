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

public class WebhookServidor {

private static final String TOKEN_VERIFICACAO =
        "meuorcamento123";

private static final String WHATSAPP_TOKEN =
        System.getenv("WHATSAPP_TOKEN");

private static final String PHONE_NUMBER_ID =
        "1226291153911101";

public static void main(String[] args) throws IOException {

        if (WHATSAPP_TOKEN == null || WHATSAPP_TOKEN.isBlank()) {

        System.out.println(
                "Token do WhatsApp não encontrado."
        );

        return;
        }

        System.out.println(
                "Token do WhatsApp carregado com sucesso."
        );
                BancoDeDados.criarTabela();

        ControleMesDAO controleMesDAO =
                new ControleMesDAO();

        String mesAtual =
                controleMesDAO.buscar();

        System.out.println(
                "Mês atual no banco: " + mesAtual
        );

        if (mesAtual == null) {

                controleMesDAO.salvar("2026-09");
        }

        String portaEnv =
                System.getenv("PORT");

        int porta =
                portaEnv != null
                        ? Integer.parseInt(portaEnv)
                        : 8080;

        HttpServer servidor =
                HttpServer.create(
                        new InetSocketAddress(porta),
                        0
                );

        servidor.createContext(
                "/webhook",
                WebhookServidor::receberRequisicao
        );

        servidor.start();

        System.out.println(
                "Webhook iniciado na porta "
                + porta
                + "/webhook"
        );
}

private static void salvarMesAtualNoHistorico() {

        ControleMesDAO controleMesDAO =
                new ControleMesDAO();

        String mesAtual =
                controleMesDAO.buscar();

        if (mesAtual == null) {

                System.out.println(
                        "Nenhum mês atual cadastrado."
                );

                return;
        }

        OrcamentoDAO orcamentoDAO =
                new OrcamentoDAO();

        Double valorOrcamento =
                orcamentoDAO.buscar();

        if (valorOrcamento == null) {

                System.out.println(
                        "Nenhum orçamento encontrado."
                );

                return;
        }

        GastoDAO gastoDAO =
                new GastoDAO();

        HistoricoGastoDAO historicoGastoDAO =
                new HistoricoGastoDAO();

        HistoricoOrcamentoDAO historicoOrcamentoDAO =
                new HistoricoOrcamentoDAO();

        historicoGastoDAO.excluirPorMes(
                mesAtual
        );

        for (Gasto gasto :
                gastoDAO.buscarTodos()) {

                historicoGastoDAO.salvar(
                        mesAtual,
                        gasto
                );
        }

        historicoOrcamentoDAO.salvar(
                mesAtual,
                valorOrcamento
        );

        System.out.println(
                "Mês " + mesAtual
                + " salvo no histórico com sucesso!"
        );
        }

private static void receberRequisicao(
        HttpExchange exchange
) throws IOException {

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
                "Método não permitido"
        );
}

private static void processarMensagem(
        HttpExchange exchange
) throws IOException {

        String corpo = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        System.out.println();
        System.out.println(
                "===== EVENTO RECEBIDO DA META ====="
        );
        System.out.println(corpo);

        String mensagem =
                extrairMensagem(corpo);

        String numeroRemetente =
                extrairRemetente(corpo);

        String idMensagem =
        extrairIdMensagem(corpo);

        if (mensagem == null || mensagem.isBlank()) {

        System.out.println();
        System.out.println(
                "Nenhuma mensagem de texto encontrada."
        );

        System.out.println(
                "=================================="
        );

        enviarResposta(
                exchange,
                200,
                "EVENT_RECEIVED"
        );

        return;
        }

        String mensagemNormalizada =
        normalizarComando(mensagem);

        if (idMensagem != null) {

        MensagemProcessadaDAO mensagemDAO =
                new MensagemProcessadaDAO();

        boolean mensagemNova =
                mensagemDAO.registrarSeNova(idMensagem);

        if (!mensagemNova) {

                System.out.println(
                        "Mensagem já processada. Ignorando duplicata: "
                        + idMensagem
                );

                enviarResposta(
                        exchange,
                        200,
                        "EVENT_RECEIVED"
                );

                return;
        }
        }

        System.out.println();
        System.out.println("Mensagem recebida:");
        System.out.println(mensagem);

        if (numeroRemetente != null) {

        System.out.println(
                "Remetente: " + numeroRemetente
        );
        }

        if (mensagemNormalizada.equals("saldo")) { 

        String respostaSaldo =
                obterSaldo();

        System.out.println();
        System.out.println(respostaSaldo);

        if (numeroRemetente != null) {

                enviarMensagemWhatsApp(
                        numeroRemetente,
                        respostaSaldo
                );

        } else {

                System.out.println();
                System.out.println(
                        "Não foi possível identificar o número do remetente."
                );
        }

        } else if (mensagemNormalizada.equals("gastos")) {

        String respostaGastos =
                listarGastos();

        System.out.println();
        System.out.println(respostaGastos);

        if (numeroRemetente != null) {

                enviarMensagemWhatsApp(
                        numeroRemetente,
                        respostaGastos
                );
        }

        } else if (mensagemNormalizada.startsWith("excluir ")) {

        processarExclusao(
                mensagem,
                numeroRemetente
        );

        } else if (mensagemNormalizada.startsWith("editar ")) {

        processarEdicao(
                mensagem,
                numeroRemetente
        );
        } else if (mensagemNormalizada.startsWith("novo mes ")) {

        processarNovoMes(
                mensagem,
                numeroRemetente
        );

        } else if (mensagemNormalizada.startsWith("orcamento ")) {

        processarOrcamento(
                mensagem,
                numeroRemetente
        );

        } else {

        processarGasto(
                mensagem,
                numeroRemetente
        );
        }

        System.out.println(
                "=================================="
        );

        enviarResposta(
                exchange,
                200,
                "EVENT_RECEIVED"
        );
}
private static void processarNovoMes(
                String mensagem,
                String numeroRemetente
        ) {

        String[] partes =
                mensagem.trim().split("\\s+");

        if (partes.length != 3) {

                System.out.println(
                        "Formato inválido para novo mês."
                );

                return;
        }

        try {

                double novoOrcamento =
                        Double.parseDouble(
                                partes[2].replace(",", ".")
                        );

                if (novoOrcamento <= 0) {

                System.out.println(
                        "O novo orçamento deve ser maior que zero."
                );

                return;
                }

                ControleMesDAO controleMesDAO =
                        new ControleMesDAO();

                String mesAtual =
                        controleMesDAO.buscar();

                if (mesAtual == null) {

                System.out.println(
                        "Nenhum mês atual cadastrado."
                );

                return;
                }

                // Guarda o mês atual antes de limpar
                salvarMesAtualNoHistorico();

                GastoDAO gastoDAO =
                        new GastoDAO();

                boolean gastosExcluidos =
                        gastoDAO.excluirTodos();

                if (!gastosExcluidos) {

                System.out.println(
                        "Não foi possível iniciar o novo mês."
                );

                return;
                }

                OrcamentoDAO orcamentoDAO =
                        new OrcamentoDAO();

                orcamentoDAO.salvar(
                        novoOrcamento
                );

                String novoMes =
                        calcularProximoMes(
                                mesAtual
                        );

                controleMesDAO.salvar(
                        novoMes
                );

                String resposta =
                        "Novo mês iniciado com sucesso!\n\n"
                        + obterSaldo();

                System.out.println();
                System.out.println(resposta);

                if (numeroRemetente != null) {

                enviarMensagemWhatsApp(
                        numeroRemetente,
                        resposta
                );
                }

        } catch (NumberFormatException e) {

                System.out.println(
                        "O valor do orçamento é inválido."
                );
        }
        }

private static String calcularProximoMes(
                String mesAtual
        ) {

        String[] partes =
                mesAtual.split("-");

        int ano =
                Integer.parseInt(partes[0]);

        int mes =
                Integer.parseInt(partes[1]);

        mes++;

        if (mes > 12) {

                mes = 1;
                ano++;
        }

        return String.format(
                "%04d-%02d",
                ano,
                mes
        );
        }

private static String extrairMensagem(
        String corpo
) {

        String marcador = "\"body\":\"";

        int inicio =
                corpo.indexOf(marcador);

        if (inicio == -1) {
        return null;
        }

        inicio += marcador.length();

        int fim =
                corpo.indexOf("\"", inicio);

        if (fim == -1) {
        return null;
        }

        String mensagem =
                corpo.substring(
                        inicio,
                        fim
                );

        return converterUnicode(mensagem);
}

private static String extrairIdMensagem(String corpo) {

        int inicioMessages =
                corpo.indexOf("\"messages\":[");

        if (inicioMessages == -1) {
                return null;
        }

        String marcador = "\"id\":\"";

        int inicio =
                corpo.indexOf(
                        marcador,
                        inicioMessages
                );

        if (inicio == -1) {
                return null;
        }

        inicio += marcador.length();

        int fim =
                corpo.indexOf("\"", inicio);

        if (fim == -1) {
                return null;
        }

        return corpo.substring(inicio, fim);
        }

private static String normalizarComando(String texto) {

        return Normalizer.normalize(
                texto,
                Normalizer.Form.NFD
        )
        .replaceAll("\\p{M}", "")
        .toLowerCase()
        .trim();
        }

private static String converterUnicode(
                String texto
        ) {

        StringBuilder resultado =
                new StringBuilder();

        for (int i = 0; i < texto.length(); i++) {

                if (texto.charAt(i) == '\\'
                        && i + 5 < texto.length()
                        && texto.charAt(i + 1) == 'u') {

                String codigo =
                        texto.substring(
                                i + 2,
                                i + 6
                        );

                try {

                        char caractere =
                                (char) Integer.parseInt(
                                        codigo,
                                        16
                                );

                        resultado.append(caractere);

                        i += 5;

                } catch (NumberFormatException e) {

                        resultado.append(
                                texto.charAt(i)
                        );
                }

                } else {

                resultado.append(
                        texto.charAt(i)
                );
                }
        }

        return resultado.toString();
        }

private static String extrairRemetente(
        String corpo
) {

        String marcador = "\"from\":\"";

        int inicio =
                corpo.indexOf(marcador);

        if (inicio == -1) {
        return null;
        }

        inicio += marcador.length();

        int fim =
                corpo.indexOf("\"", inicio);

        if (fim == -1) {
        return null;
        }

        return corpo.substring(
                inicio,
                fim
        );
}

private static void processarGasto(
        String mensagem,
        String numeroRemetente
) {

        String[] partesMensagem =
                mensagem.trim().split("\\s+");

        if (partesMensagem.length < 2) {

        System.out.println();
        System.out.println(
                "A mensagem não está no formato esperado."
        );

        return;
        }

        String ultimoItem =
                partesMensagem[
                        partesMensagem.length - 1
                ];

        try {

        double valor =
                Double.parseDouble(
                        ultimoItem.replace(",", ".")
                );

        if (valor <= 0) {

                System.out.println();
                System.out.println(
                        "O valor do gasto deve ser maior que zero."
                );

                return;
        }

        int posicaoValor =
                mensagem.lastIndexOf(
                        ultimoItem
                );

        String descricao =
                mensagem.substring(
                        0,
                        posicaoValor
                ).trim();

        if (descricao.isBlank()) {

                System.out.println();
                System.out.println(
                        "A descrição do gasto não pode ficar vazia."
                );

                return;
        }

        System.out.println();
        System.out.println(
                "Descrição: " + descricao
        );

        System.out.println(
                "Valor: R$ " + valor
        );

        Gasto gasto =
                new Gasto(
                        descricao,
                        valor
                );

        GastoDAO gastoDAO =
                new GastoDAO();

        boolean salvo =
                gastoDAO.salvar(gasto);

        System.out.println();

        if (salvo) {

                System.out.println(
                        "Gasto recebido pelo WhatsApp e salvo no banco!"
                );

                String resposta =
                        "Gasto adicionado com sucesso!\n"
                        + descricao
                        + " - R$ "
                        + String.format("%.2f", valor)
                        + "\n"
                        + obterSaldo();

                if (numeroRemetente != null) {

                enviarMensagemWhatsApp(
                        numeroRemetente,
                        resposta
                );
                }

        } else {

                System.out.println(
                        "Não foi possível salvar o gasto no banco."
                );
        }

        } catch (NumberFormatException e) {

        System.out.println();
        System.out.println(
                "A mensagem não está no formato esperado."
        );
        }
}
private static void processarExclusao(
                String mensagem,
                String numeroRemetente
        ) {

        String[] partes =
                mensagem.trim().split("\\s+");

        if (partes.length != 2) {

                System.out.println(
                        "Formato inválido para exclusão."
                );

                return;
        }

        try {

                int id =
                        Integer.parseInt(partes[1]);

                GastoDAO gastoDAO =
                        new GastoDAO();

        boolean excluido =
                gastoDAO.excluir(id);

        String resposta;

        if (excluido) {

        resposta =
                "Gasto excluído com sucesso!\n"
                + "ID: "
                + id
                + "\n"
                + obterSaldo();

        } else {

        resposta =
                "Não existe nenhum gasto com o ID "
                + id
                + ".";
        }

        System.out.println();
        System.out.println(resposta);

        if (numeroRemetente != null) {

        enviarMensagemWhatsApp(
                numeroRemetente,
                resposta
        );
        }

        } catch (NumberFormatException e) {

                System.out.println(
                        "O ID precisa ser um número."
                );
        }
        }

private static void processarEdicao(
                String mensagem,
                String numeroRemetente
        ) {

        String[] partes =
                mensagem.trim().split("\\s+");

        if (partes.length < 4) {

                System.out.println(
                        "Formato inválido para edição."
                );

                return;
        }

        try {

                int id =
                        Integer.parseInt(partes[1]);

                String ultimoItem =
                        partes[partes.length - 1];

                double valor =
                        Double.parseDouble(
                                ultimoItem.replace(",", ".")
                        );

                if (valor <= 0) {

                System.out.println(
                        "O valor do gasto deve ser maior que zero."
                );

                return;
                }

                int inicioDescricao =
                        mensagem.indexOf(partes[2]);

                int posicaoValor =
                        mensagem.lastIndexOf(ultimoItem);

                String descricao =
                        mensagem.substring(
                                inicioDescricao,
                                posicaoValor
                        ).trim();

                if (descricao.isBlank()) {

                System.out.println(
                        "A descrição não pode ficar vazia."
                );

                return;
                }

                Gasto gasto =
                        new Gasto(
                                id,
                                descricao,
                                valor
                        );

                GastoDAO gastoDAO =
                        new GastoDAO();

        boolean atualizado =
                gastoDAO.atualizar(gasto);

        String resposta;

        if (atualizado) {

        resposta =
                "Gasto atualizado com sucesso!\n"
                + "ID: "
                + id
                + "\n"
                + descricao
                + " - R$ "
                + String.format("%.2f", valor)
                + "\n"
                + obterSaldo();

        } else {

        resposta =
                "Não existe nenhum gasto com o ID "
                + id
                + ".";
        }

        System.out.println();
        System.out.println(resposta);

        if (numeroRemetente != null) {

        enviarMensagemWhatsApp(
                numeroRemetente,
                resposta
        );
        }

        } catch (NumberFormatException e) {

                System.out.println(
                        "ID ou valor inválido."
                );
        }
        }

private static void processarOrcamento(
                String mensagem,
                String numeroRemetente
        ) {

        String[] partes =
                mensagem.trim().split("\\s+");

        if (partes.length != 2) {

                System.out.println(
                        "Formato inválido para orçamento."
                );

                return;
        }

        try {

                double valor =
                        Double.parseDouble(
                                partes[1].replace(",", ".")
                        );

                if (valor <= 0) {

                System.out.println(
                        "O orçamento deve ser maior que zero."
                );

                return;
                }

                OrcamentoDAO orcamentoDAO =
                        new OrcamentoDAO();

                orcamentoDAO.salvar(valor);

                String resposta =
                        "Orçamento alterado com sucesso!\n\n"
                        + obterSaldo();

                System.out.println();
                System.out.println(resposta);

                if (numeroRemetente != null) {

                enviarMensagemWhatsApp(
                        numeroRemetente,
                        resposta
                );
                }

        } catch (NumberFormatException e) {

                System.out.println(
                        "O valor do orçamento é inválido."
                );
        }
        }
private static String listarGastos() {

        GastoDAO gastoDAO =
                new GastoDAO();

        if (gastoDAO.buscarTodos().isEmpty()) {

        return "Nenhum gasto cadastrado.";
        }

        String resposta =
                "GASTOS CADASTRADOS\n\n";

        for (Gasto gasto :
                gastoDAO.buscarTodos()) {

        resposta +=
                "ID: "
                + gasto.getId()
                + " - "
                + gasto.getDescricao()
                + " - R$ "
                + String.format(
                        "%.2f",
                        gasto.getValor()
                )
                + "\n";
        }

        return resposta;
}

private static String obterSaldo() {

        OrcamentoDAO orcamentoDAO =
                new OrcamentoDAO();

        GastoDAO gastoDAO =
                new GastoDAO();

        Double valorOrcamento =
                orcamentoDAO.buscar();

        if (valorOrcamento == null) {

        return "Nenhum orçamento cadastrado.";
        }

        double totalGastos = 0;

        for (Gasto gasto :
                gastoDAO.buscarTodos()) {

        totalGastos += gasto.getValor();
        }

        double saldo =
                valorOrcamento - totalGastos;

        String resposta =
                "MEU ORÇAMENTO\n"
                + "Orçamento: R$ "
                + String.format("%.2f", valorOrcamento)
                + "\n"
                + "Total gasto: R$ "
                + String.format("%.2f", totalGastos)
                + "\n"
                + "Saldo disponível: R$ "
                + String.format("%.2f", saldo);

        if (saldo < 0) {

        resposta +=
                "\n\nAtenção! Você ultrapassou "
                + "o orçamento em R$ "
                + String.format(
                        "%.2f",
                        Math.abs(saldo)
                );
        }

        return resposta;
}

private static void enviarMensagemWhatsApp(
        String numero,
        String mensagem
) {

        try {

        String url =
                "https://graph.facebook.com/v26.0/"
                + PHONE_NUMBER_ID
                + "/messages";

        String mensagemJson =
                escaparJson(mensagem);

        String json =
                "{"
                + "\"messaging_product\":\"whatsapp\","
                + "\"to\":\"" + numero + "\","
                + "\"type\":\"text\","
                + "\"text\":{"
                + "\"body\":\"" + mensagemJson + "\""
                + "}"
                + "}";

        HttpClient cliente =
                HttpClient.newHttpClient();

        HttpRequest requisicao =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header(
                                "Authorization",
                                "Bearer "
                                + WHATSAPP_TOKEN
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest.BodyPublishers
                                        .ofString(json)
                        )
                        .build();

        HttpResponse<String> resposta =
                cliente.send(
                        requisicao,
                        HttpResponse.BodyHandlers
                                .ofString()
                );

        System.out.println();
        System.out.println(
                "===== ENVIO PARA O WHATSAPP ====="
        );

        System.out.println(
                "Status HTTP: "
                + resposta.statusCode()
        );

        System.out.println(
                "Resposta da Meta: "
                + resposta.body()
        );

        System.out.println(
                "================================"
        );

        } catch (Exception e) {

        System.out.println();
        System.out.println(
                "Erro ao enviar mensagem pelo WhatsApp."
        );

        e.printStackTrace();
        }
}

private static String escaparJson(
        String texto
) {

        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
}

private static void verificarWebhook(
        HttpExchange exchange
) throws IOException {

        String query =
                exchange.getRequestURI().getQuery();

        if (query == null) {

        enviarResposta(
                exchange,
                200,
                "MeuOrcamento webhook funcionando"
        );

        return;
        }

        String modo = null;
        String token = null;
        String desafio = null;

        String[] parametros =
                query.split("&");

        for (String parametro :
                parametros) {

        String[] partes =
                parametro.split("=", 2);

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
                "Webhook verificado pela Meta!"
        );

        enviarResposta(
                exchange,
                200,
                desafio
        );

        } else {

        System.out.println(
                "Falha na verificação do webhook."
        );

        enviarResposta(
                exchange,
                403,
                "Token de verificação inválido"
        );
        }
}

private static void enviarResposta(
        HttpExchange exchange,
        int status,
        String resposta
) throws IOException {

        byte[] dados =
                resposta.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange.sendResponseHeaders(
                status,
                dados.length
        );

        try (OutputStream output =
                exchange.getResponseBody()) {

        output.write(dados);
        }
}
}