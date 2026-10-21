package com.meuorcamento.webhook;

import static org.junit.jupiter.api.Assertions.assertNull;

import com.meuorcamento.modelo.Categoria;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

class CategoriaEdicaoTest {

    @Test
    void deveIdentificarCategoriaNoFinal() {
        Categoria lanches = new Categoria(1, "Lanches");

        Categoria encontrada = WebhookServidor.identificarCategoriaNoFinal(
                "editar 102 hamburguer 61 Lanches",
                List.of(lanches));

        assertSame(lanches, encontrada);
    }

    @Test
    void deveEscolherCategoriaComNomeMaisComprido() {
        Categoria casa = new Categoria(1, "Casa");
        Categoria alimentacaoCasa = new Categoria(2, "Alimentação Casa");

        Categoria encontrada = WebhookServidor.identificarCategoriaNoFinal(
                "editar 102 mercado 150 Alimentação Casa",
                List.of(casa, alimentacaoCasa));

        assertSame(alimentacaoCasa, encontrada);
    }

    @Test
    void deveRetornarNuloParaCategoriaDesconhecida() {
        Categoria lanches = new Categoria(1, "Lanches");

        Categoria encontrada = WebhookServidor.identificarCategoriaNoFinal(
                "editar 102 hamburguer 61 Passeios",
                List.of(lanches));

        assertNull(encontrada);
    }

    @Test
    void naoDeveIdentificarCategoriaSomenteNaDescricao() {
        Categoria casa = new Categoria(1, "Casa");

        Categoria encontrada = WebhookServidor.identificarCategoriaNoFinal(
                "editar 102 compra para Casa 150",
                List.of(casa));

        assertNull(encontrada);
    }

    @Test
    void naoDeveAceitarCategoriaComoParteDeOutraPalavra() {
        Categoria casa = new Categoria(1, "Casa");

        Categoria encontrada = WebhookServidor.identificarCategoriaNoFinal(
                "editar 102 compra 150 MinhaCasa",
                List.of(casa));

        assertNull(encontrada);
    }
}