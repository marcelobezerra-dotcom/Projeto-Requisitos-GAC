package com.example.qrcodeapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Normalização do identificador do projetor. Precisa ser estável porque é ela
 * que impede "4" e "04" de virarem dois equipamentos na lista.
 */
public class ProjetorNormalizacaoTest {

    @Test
    public void removeEspacosDasPontas() {
        assertEquals("4", Projetor.normalizarNumero(" 4 "));
        assertEquals("P-04", Projetor.normalizarNumero("  P-04  "));
    }

    @Test
    public void removeZerosAEsquerda() {
        assertEquals("4", Projetor.normalizarNumero("04"));
        assertEquals("4", Projetor.normalizarNumero("004"));
        assertEquals("10", Projetor.normalizarNumero("010"));
    }

    @Test
    public void mantemPeloMenosUmDigito() {
        assertEquals("0", Projetor.normalizarNumero("0"));
        assertEquals("0", Projetor.normalizarNumero("000"));
    }

    @Test
    public void ignoraMaiusculasMinusculas() {
        assertEquals("P-04", Projetor.normalizarNumero("p-04"));
        assertEquals(Projetor.normalizarNumero("abc"), Projetor.normalizarNumero("ABC"));
    }

    @Test
    public void recusaEspacoInternoEmVezDeSumiComEle() {
        // "4 5" nao pode virar o projetor "45" sem ninguem perceber.
        assertEquals("", Projetor.normalizarNumero("4 5"));
        assertFalse(Projetor.isNumeroValido(Projetor.normalizarNumero("4 5")));
    }

    @Test
    public void recusaCaracteresForaDoPermitido() {
        assertEquals("", Projetor.normalizarNumero(null));
        assertEquals("", Projetor.normalizarNumero(""));
        assertEquals("", Projetor.normalizarNumero("   "));
        assertEquals("", Projetor.normalizarNumero("pro/4"));
        assertEquals("", Projetor.normalizarNumero("4;DROP"));
        assertEquals("", Projetor.normalizarNumero("projetor:4"));
    }

    @Test
    public void aceitaLetrasDigitosESeparadores() {
        assertEquals("A_B.C-1", Projetor.normalizarNumero("a_b.c-1"));
        assertTrue(Projetor.isNumeroValido(Projetor.normalizarNumero("XY9")));
    }

    @Test
    public void apenasDigitos() {
        assertTrue(Projetor.isSomenteDigitos("104"));
        assertFalse(Projetor.isSomenteDigitos("10A"));
        assertFalse(Projetor.isSomenteDigitos(""));
        assertFalse(Projetor.isSomenteDigitos(null));
    }

    @Test
    public void construtorNormalizaONumero() {
        assertEquals("4", new Projetor("04").numero);
    }

    @Test
    public void igualdadePeloNumeroNormalizado() {
        assertEquals(new Projetor("4"), new Projetor("04"));
        assertEquals(new Projetor("4").hashCode(), new Projetor("04").hashCode());
        assertFalse(new Projetor("4").equals(new Projetor("5")));
    }
}
