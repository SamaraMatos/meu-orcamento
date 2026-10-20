package com.meuorcamento.webhook;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ValorGastoTest {

    @Test
    void deveAceitarValorPositivoFinito() {
        boolean valido = WebhookServidor.valorGastoValido(15.50);

        assertTrue(valido);
    }

    @Test
    void deveRejeitarNaN() {
        boolean valido = WebhookServidor.valorGastoValido(Double.NaN);

        assertFalse(valido);
    }

    @Test
    void deveRejeitarInfinitoPositivo() {
        boolean valido = WebhookServidor.valorGastoValido(
                Double.POSITIVE_INFINITY);

        assertFalse(valido);
    }

    @Test
    void deveRejeitarZero() {
        boolean valido = WebhookServidor.valorGastoValido(0);

        assertFalse(valido);
    }

    @Test
    void deveRejeitarValorNegativo() {
        boolean valido = WebhookServidor.valorGastoValido(-10);

        assertFalse(valido);
    }
}