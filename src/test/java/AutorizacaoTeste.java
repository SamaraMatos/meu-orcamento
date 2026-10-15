public class AutorizacaoTeste {

    public static void main(String[] args) {
        String lista = "5500000000001, 5500000000002";

        if (!WebhookServidor.remetenteAutorizado("5500000000001", lista)) {
            throw new AssertionError("Deveria permitir o primeiro número.");
        }

        if (!WebhookServidor.remetenteAutorizado("5500000000002", lista)) {
            throw new AssertionError("Deveria permitir o segundo número.");
        }

        if (WebhookServidor.remetenteAutorizado("5500000000003", lista)) {
            throw new AssertionError("Deveria bloquear um número fora da lista.");
        }

        if (WebhookServidor.remetenteAutorizado("0001", lista)) {
            throw new AssertionError("Não deveria aceitar apenas parte do número.");
        }

        if (WebhookServidor.remetenteAutorizado(null, lista)) {
            throw new AssertionError("Deveria bloquear remetente nulo.");
        }

        if (WebhookServidor.remetenteAutorizado("   ", lista)) {
            throw new AssertionError("Deveria bloquear remetente vazio.");
        }

        if (WebhookServidor.remetenteAutorizado("5500000000001", null)) {
            throw new AssertionError("Deveria bloquear quando falta a configuração.");
        }

        if (WebhookServidor.remetenteAutorizado("5500000000001", "   ")) {
            throw new AssertionError("Deveria bloquear quando a configuração está vazia.");
        }

        System.out.println("TESTES PASSARAM: autorização verificada.");
    }
}