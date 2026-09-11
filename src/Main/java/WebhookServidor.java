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

                HttpServer servidor =
                        HttpServer.create(
                                new InetSocketAddress(8080),
                                0
                        );

                servidor.createContext(
                        "/webhook",
                        WebhookServidor::receberRequisicao
                );

                servidor.start();

                System.out.println(
                        "Webhook iniciado em http://localhost:8080/webhook"
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

                System.out.println();
                System.out.println("Mensagem recebida:");
                System.out.println(mensagem);

                if (numeroRemetente != null) {

                System.out.println(
                        "Remetente: " + numeroRemetente
                );
                }

                if (mensagem.equalsIgnoreCase("saldo")) {

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

                return corpo.substring(
                        inicio,
                        fim
                );
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