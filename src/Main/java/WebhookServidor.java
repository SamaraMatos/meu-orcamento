import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class WebhookServidor {

    private static final String TOKEN_VERIFICACAO = "meuorcamento123";

    public static void main(String[] args) throws IOException {

        HttpServer servidor =
            HttpServer.create(new InetSocketAddress(8080), 0);

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

        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {

            verificarWebhook(exchange);

        } else if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

            String corpo = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
            );

            System.out.println();
            System.out.println("===== EVENTO RECEBIDO DA META =====");
            System.out.println(corpo);
            System.out.println("==================================");

            enviarResposta(exchange, 200, "EVENT_RECEIVED");

        } else {

            enviarResposta(exchange, 405, "Método não permitido");
        }
            }

    private static void verificarWebhook(
        HttpExchange exchange
    ) throws IOException {

        String query = exchange.getRequestURI().getQuery();

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

        String[] parametros = query.split("&");

        for (String parametro : parametros) {

            String[] partes = parametro.split("=", 2);

            if (partes.length == 2) {

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

        byte[] dados = resposta.getBytes();

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