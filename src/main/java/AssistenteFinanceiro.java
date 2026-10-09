import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import com.google.gson.Gson;
import java.net.http.HttpResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.LocalDate;
import java.time.ZoneId;

public class AssistenteFinanceiro {

    public static String consultar(String pergunta) {

    String dadosFinanceiros = WebhookServidor.obterSaldo();
    LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
    int diasRestantes = hoje.lengthOfMonth() - hoje.getDayOfMonth();
    String contexto =
        "Você é um assistente de educação financeira. "
        + "Analise os dados financeiros apresentados e "
        + "responda à pergunta do usuário com clareza. "
        + "Considere o saldo disponível e possíveis despesas futuras. "
        + "Se faltarem informações, faça perguntas. "
        + "Não invente valores e não afirme ter registrado ou alterado gastos. "
        + "Você apenas aconselha.\n\n"
        + "DATA ATUAL:\n"
        + hoje
        + "\nDias restantes até o fim do mês: "
        + diasRestantes
        + "\n\nDADOS FINANCEIROS:\n"
        + dadosFinanceiros
        + "\n\nPERGUNTA DO USUÁRIO:\n"
        + pergunta;

    String chaveApi = System.getenv("GEMINI_API_KEY");

    if (chaveApi == null || chaveApi.isBlank()) {
        return "A chave do Gemini não está configurada.";
    }

    Gson gson = new Gson();

    String corpoJson = gson.toJson(
        java.util.Map.of(
            "contents", java.util.List.of(
                java.util.Map.of(
                    "parts", java.util.List.of(
                        java.util.Map.of("text", contexto)
                    )
                )
            )
        )
    );

    HttpClient cliente = HttpClient.newHttpClient();

    String enderecoApi =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent";

        HttpRequest requisicao = HttpRequest.newBuilder()
        .uri(URI.create(enderecoApi))
        .header("Content-Type", "application/json")
        .header("x-goog-api-key", chaveApi)
        .POST(HttpRequest.BodyPublishers.ofString(corpoJson))
        .build();

        try {

            HttpResponse<String> resposta = cliente.send(
                    requisicao,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (resposta.statusCode() != 200) {

                System.err.println(
                        "Erro na API Gemini. Status: " + resposta.statusCode()
                );

                System.err.println(
                        "Detalhes do erro Gemini: " + resposta.body()
                );

                return "Não consegui consultar o Gemini agora. Tente novamente mais tarde.";
            }

            JsonObject json = JsonParser.parseString(resposta.body())
                    .getAsJsonObject();

            String texto = json
                    .getAsJsonArray("candidates")
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0)
                    .getAsJsonObject()
                    .get("text")
                    .getAsString();

            return texto;

        } catch (Exception e) {

            System.err.println("Erro ao consultar o Gemini: " + e.getMessage());
            return "Não consegui consultar o Gemini agora.";
        }

}
    
}
