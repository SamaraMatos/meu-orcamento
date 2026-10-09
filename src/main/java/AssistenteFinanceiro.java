import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import com.google.gson.Gson;
import java.net.http.HttpResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class AssistenteFinanceiro {

    public static String consultar(String pergunta) {

    String dadosFinanceiros = WebhookServidor.obterResumo();

    GastoDAO gastoDAO = new GastoDAO();

    List<Gasto> gastos = gastoDAO.buscarTodos();

    StringBuilder detalhesGastos = new StringBuilder();

    for (Gasto gasto : gastos) {
        detalhesGastos.append(gasto.getDescricao())
                .append(" - R$ ")
                .append(String.format("%.2f", gasto.getValor()))
                .append(" - ")
                .append(gasto.getCategoria().getNome())
                .append("\n");
    }

    LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
    int diasRestantes = hoje.lengthOfMonth() - hoje.getDayOfMonth();
    
    String contexto =
        "Você é um assistente de educação financeira. "
        + "Não classifique um gasto como desnecessário apenas pelo valor ou descrição. "
        + "Considere que despesas com saúde, animais, alimentação e outras necessidades podem ser essenciais. "
        + "Ao sugerir economia, apresente recomendações específicas baseadas nos gastos registrados. "
        + "Não invente motivos para as compras nem afirme que um gasto poderia ter sido evitado sem evidências. "
        + "Não termine a resposta com perguntas por hábito. "
        + "Se houver informações suficientes, responda e encerre naturalmente. "
        + "Analise os gastos por categoria para identificar onde o dinheiro está sendo mais utilizado. "
        + "Considere essas informações ao avaliar novas despesas e sugerir melhorias. "
        + "Não considere automaticamente a categoria com maior gasto como desperdício, "
        + "pois despesas essenciais podem representar a maior parte do orçamento. "
        + "Mencione as categorias apenas quando forem relevantes para a pergunta. "
        + "Responda de forma curta, direta e natural, como uma conversa no WhatsApp. "
        + "Prefira de 3 a 5 frases curtas, sem apresentações ou textos longos. "
        + "Dê primeiro sua recomendação e depois explique brevemente o motivo. "
        + "Mostre apenas os números mais importantes para a decisão. "
        + "Não use títulos com #, nem formatação Markdown complexa. "
        + "Faça perguntas somente quando forem indispensáveis. "
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
        + "\n\nGASTOS DETALHADOS:\n"
        + detalhesGastos.toString()
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
