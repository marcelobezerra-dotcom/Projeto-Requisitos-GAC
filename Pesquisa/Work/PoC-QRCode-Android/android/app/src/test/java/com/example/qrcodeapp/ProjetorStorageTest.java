package com.example.qrcodeapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

/**
 * Persistencia em SharedPreferences: round-trip do JSON, deduplicacao e a
 * garantia de que um numero desconhecido nao cria projetor fantasma.
 */
@RunWith(RobolectricTestRunner.class)
public class ProjetorStorageTest {

    private Context ctx;

    @Before
    public void setUp() {
        ctx = ApplicationProvider.getApplicationContext();
        ctx.getSharedPreferences("projetores_prefs", Context.MODE_PRIVATE)
                .edit().clear().commit();
    }

    @Test
    public void primeiraExecucaoCadastraOsExemplos() {
        List<Projetor> lista = ProjetorStorage.carregarTodos(ctx);
        assertEquals(3, lista.size());
        assertEquals("1", lista.get(0).numero);
        assertEquals("2", lista.get(1).numero);
        assertEquals("3", lista.get(2).numero);
    }

    @Test
    public void adicionaEPersiste() {
        assertTrue(ProjetorStorage.adicionar(ctx, "7"));
        assertNotNull(ProjetorStorage.buscarPorNumero(ctx, "7"));
        // Recarrega do disco para provar que persistiu de verdade.
        assertNotNull(ProjetorStorage.buscarPorNumero(ctx, "7"));
    }

    @Test
    public void quatroEOQuatroSaoOMesmoProjetor() {
        assertTrue(ProjetorStorage.adicionar(ctx, "04"));
        assertFalse(ProjetorStorage.adicionar(ctx, "4"));
        assertFalse(ProjetorStorage.buscarPorNumero(ctx, "4") == null);
        assertNotNull(ProjetorStorage.buscarPorNumero(ctx, "4"));
        assertNotNull(ProjetorStorage.buscarPorNumero(ctx, " 04 "));

        int total = 0;
        for (Projetor p : ProjetorStorage.carregarTodos(ctx)) {
            if ("4".equals(p.numero)) {
                total++;
            }
        }
        assertEquals("'4' e '04' nao podem virar dois projetores", 1, total);
    }

    @Test
    public void naoAceitaNumeroInvalido() {
        assertFalse(ProjetorStorage.adicionar(ctx, ""));
        assertFalse(ProjetorStorage.adicionar(ctx, "   "));
        assertFalse(ProjetorStorage.adicionar(ctx, null));
        assertFalse(ProjetorStorage.adicionar(ctx, "4 5"));
        assertFalse(ProjetorStorage.adicionar(ctx, "pro/4"));
        assertNull(ProjetorStorage.buscarPorNumero(ctx, "!!"));
    }

    /**
     * Regressão do QR Code impostor: varrer um QR de um projetor desconhecido
     * nao pode cadastrar equipamento por conta propria.
     */
    @Test
    public void salvarProjetorNaoCriaNumeroDesconhecido() {
        int antes = ProjetorStorage.carregarTodos(ctx).size();
        assertFalse(ProjetorStorage.salvarProjetor(ctx, new Projetor("999")));
        assertEquals(antes, ProjetorStorage.carregarTodos(ctx).size());
        assertNull(ProjetorStorage.buscarPorNumero(ctx, "999"));
    }

    @Test
    public void salvarProjetorAtualizaExistente() {
        Projetor p = new Projetor("1");
        p.status = Projetor.STATUS_ALUGADO;
        p.locatario = "Maria";
        p.telefone = "11999998888";
        p.dataInicio = "26/09/2026";
        p.dataDevolucao = "30/09/2026";

        assertTrue(ProjetorStorage.salvarProjetor(ctx, p));
        assertEquals(3, ProjetorStorage.carregarTodos(ctx).size());

        Projetor lido = ProjetorStorage.buscarPorNumero(ctx, "1");
        assertNotNull(lido);
        assertTrue(lido.isAlugado());
        assertEquals("Maria", lido.locatario);
        assertEquals("11999998888", lido.telefone);
        assertEquals("26/09/2026", lido.dataInicio);
        assertEquals("30/09/2026", lido.dataDevolucao);
    }

    @Test
    public void devolucaoLimpaOsDadosDoAluguel() {
        Projetor p = new Projetor("2");
        p.status = Projetor.STATUS_ALUGADO;
        p.locatario = "Joao";
        ProjetorStorage.salvarProjetor(ctx, p);

        Projetor lido = ProjetorStorage.buscarPorNumero(ctx, "2");
        lido.status = Projetor.STATUS_DISPONIVEL;
        lido.locatario = "";
        lido.telefone = "";
        lido.dataInicio = "";
        lido.dataDevolucao = "";
        assertTrue(ProjetorStorage.salvarProjetor(ctx, lido));

        Projetor depois = ProjetorStorage.buscarPorNumero(ctx, "2");
        assertFalse(depois.isAlugado());
        assertEquals("", depois.locatario);
    }

    @Test
    public void salvaERecarregaComAcentosEEscape() {
        assertTrue(ProjetorStorage.adicionar(ctx, "P-04"));
        Projetor p = ProjetorStorage.buscarPorNumero(ctx, "P-04");
        p.locatario = "José da Silva — \"rápido\"";
        p.telefone = "+55 (11) 91234-5678";
        assertTrue(ProjetorStorage.salvarProjetor(ctx, p));

        Projetor lido = ProjetorStorage.buscarPorNumero(ctx, "P-04");
        assertEquals("José da Silva — \"rápido\"", lido.locatario);
        assertEquals("+55 (11) 91234-5678", lido.telefone);
    }

    @Test
    public void jsonCorrompidoNaoQuebraOApp() {
        ctx.getSharedPreferences("projetores_prefs", Context.MODE_PRIVATE)
                .edit().putString("lista_projetores", "isso nao e json").commit();

        List<Projetor> lista = ProjetorStorage.carregarTodos(ctx);
        assertNotNull(lista);
        assertTrue(lista.isEmpty());
    }

    @Test
    public void deduplicaDadosAntigosComZerosAEsquerda() {
        // Simula o pre-fix, que gravava "4" e "04" como dois projetores.
        String json = "[{\"numero\":\"4\",\"status\":\"disponivel\",\"locatario\":\"\","
                + "\"telefone\":\"\",\"dataInicio\":\"\",\"dataDevolucao\":\"\"},"
                + "{\"numero\":\"04\",\"status\":\"disponivel\",\"locatario\":\"\","
                + "\"telefone\":\"\",\"dataInicio\":\"\",\"dataDevolucao\":\"\"}]";
        ctx.getSharedPreferences("projetores_prefs", Context.MODE_PRIVATE)
                .edit().putString("lista_projetores", json).commit();

        List<Projetor> lista = ProjetorStorage.carregarTodos(ctx);
        assertEquals(1, lista.size());
        assertEquals("4", lista.get(0).numero);
    }

    @Test
    public void removeProjeto() {
        assertNotNull(ProjetorStorage.buscarPorNumero(ctx, "2"));
        ProjetorStorage.remover(ctx, "2");
        assertNull(ProjetorStorage.buscarPorNumero(ctx, "2"));
    }

    @Test
    public void conteudoDoQrCodeTemPrefixoFixo() {
        assertEquals("projetor:4", new Projetor("04").conteudoQrCode());
    }
}
