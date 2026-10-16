package com.meuorcamento;

import org.junit.jupiter.api.Test;

import com.meuorcamento.WebhookServidor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AutorizacaoTest {

    @Test
    void devePermitirNumeroAutorizado() {
        String lista = "5500000000001,5500000000002";

        boolean autorizado = WebhookServidor.remetenteAutorizado(
                "5500000000001",
                lista);

        assertTrue(autorizado);
    }

    @Test
    void deveBloquearNumeroNaoAutorizado() {
        String lista = "5500000000001,5500000000002";

        boolean autorizado = WebhookServidor.remetenteAutorizado(
                "5500000000003",
                lista);

        assertFalse(autorizado);
    }

    @Test
    void deveBloquearQuandoConfiguracaoEstiverAusente() {
        boolean autorizado = WebhookServidor.remetenteAutorizado(
                "5500000000001",
                null);

        assertFalse(autorizado);
    }

    @Test
    void devePermitirSegundoNumeroComEspacoNaLista() {
        String lista = "5500000000001, 5500000000002";

        boolean autorizado = WebhookServidor.remetenteAutorizado(
                "5500000000002",
                lista);

        assertTrue(autorizado);
    }

    @Test
    void deveBloquearNumeroParcial() {
        String lista = "5500000000001,5500000000002";

        boolean autorizado = WebhookServidor.remetenteAutorizado(
                "0001",
                lista);

        assertFalse(autorizado);
    }

    @Test
    void deveBloquearRemetenteNulo() {
        String lista = "5500000000001,5500000000002";

        boolean autorizado = WebhookServidor.remetenteAutorizado(
                null,
                lista);

        assertFalse(autorizado);
    }

    @Test
    void deveBloquearRemetenteEmBranco() {
        String lista = "5500000000001,5500000000002";

        boolean autorizado = WebhookServidor.remetenteAutorizado(
                "   ",
                lista);

        assertFalse(autorizado);
    }

    @Test
    void deveBloquearQuandoConfiguracaoEstiverEmBranco() {
        boolean autorizado = WebhookServidor.remetenteAutorizado(
                "5500000000001",
                "   ");

        assertFalse(autorizado);
    }
}