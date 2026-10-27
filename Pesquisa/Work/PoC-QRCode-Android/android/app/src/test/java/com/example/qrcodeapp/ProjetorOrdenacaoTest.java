package com.example.qrcodeapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Regressão do crash {@code IllegalArgumentException: Comparison method
 * violates its general contract!}: o comparator antigo misturava critério
 * numérico e lexicográfico, o que quebrava o TimSort do {@code Collections.sort}.
 *
 * <p>Exemplo que quebrava o comparador antigo: "9" &lt; "10" (numérico),
 * mas "10" &lt; "2A" &lt; "9" (lexicográfico) e "9" &gt; "2A" (lexicográfico).
 */
public class ProjetorOrdenacaoTest {

    private static final String[] UNIVERSO = {
            "0", "00", "1", "2", "3", "4", "9", "10", "20", "100", "007", "999999999999",
            "A", "B", "Z", "a", "b", "P-04", "p-04", "XY9", "9A", "A9",
            "2A", "3B", "10C", "9D", "1a", "9z", "2a", "20b", "8x",
    };

    private static final Comparator<Projetor> ORDEM =
            (a, b) -> ProjetorStorage.compararNumeros(a.numero, b.numero);

    @Test
    public void numericosVemPrimeiroEmOrdemNumerica() {
        assertTrue(ProjetorStorage.compararNumeros("2", "10") < 0);
        assertTrue(ProjetorStorage.compararNumeros("9", "100") < 0);
        assertTrue(ProjetorStorage.compararNumeros("100", "A") < 0);
        assertTrue(ProjetorStorage.compararNumeros("2", "9") < 0);
    }

    @Test
    public void alfanumericosVemOrdenadosAlfabeticamente() {
        assertTrue(ProjetorStorage.compararNumeros("A", "B") < 0);
        assertTrue(ProjetorStorage.compararNumeros("P-04", "XY9") < 0);
        assertTrue(ProjetorStorage.compararNumeros("a", "b") < 0);
    }

    @Test
    public void antisimetria() {
        for (String a : UNIVERSO) {
            for (String b : UNIVERSO) {
                int ab = ProjetorStorage.compararNumeros(a, b);
                int ba = ProjetorStorage.compararNumeros(b, a);
                assertTrue("assimetria em (" + a + "," + b + "): " + ab + " vs " + ba,
                        ab == -ba || (ab == 0 && ba == 0));
            }
        }
    }

    @Test
    public void transitividade() {
        for (String a : UNIVERSO) {
            for (String b : UNIVERSO) {
                for (String c : UNIVERSO) {
                    int ab = ProjetorStorage.compararNumeros(a, b);
                    int bc = ProjetorStorage.compararNumeros(b, c);
                    int ac = ProjetorStorage.compararNumeros(a, c);
                    // Se a<=b e b<=c, entao a<=c.
                    assertTrue("transitividade quebrada em (" + a + "," + b + "," + c + "): "
                                    + ab + "," + bc + " => " + ac,
                            !(ab <= 0 && bc <= 0) || ac <= 0);
                }
            }
        }
    }

    @Test
    public void compareZeroCoincideComEquals() {
        for (String a : UNIVERSO) {
            for (String b : UNIVERSO) {
                boolean iguais = new Projetor(a).equals(new Projetor(b));
                int cmp = ProjetorStorage.compararNumeros(a, b);
                assertTrue("compare e equals discordam em (" + a + "," + b + ")",
                        iguais == (cmp == 0));
            }
        }
    }

    /**
     * O proprio crash: o TimSort do Collections.sort estoura quando o comparator
     * nao e uma ordem total valida.
     */
    @Test
    public void timSortNaoEstouraComListasEmbaralhadas() {
        Random r = new Random(42);
        for (int rodada = 0; rodada < 3000; rodada++) {
            int n = 2 + r.nextInt(40);
            List<Projetor> lista = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                lista.add(new Projetor(UNIVERSO[r.nextInt(UNIVERSO.length)]));
            }
            try {
                Collections.sort(lista, ORDEM);
            } catch (IllegalArgumentException e) {
                fail("TimSort estourou na rodada " + rodada + ": " + e.getMessage());
            }
            for (int i = 1; i < lista.size(); i++) {
                assertTrue("ordem crescente violada",
                        ProjetorStorage.compararNumeros(
                                lista.get(i - 1).numero, lista.get(i).numero) <= 0);
            }
        }
    }

    @Test
    public void listaMistaSaiNaOrdemEsperada() {
        List<Projetor> lista = new ArrayList<>();
        for (String s : new String[]{"100", "9", "A", "10", "b", "2", "P-04"}) {
            lista.add(new Projetor(s));
        }
        Collections.sort(lista, ORDEM);
        assertEquals("[2, 9, 10, 100, A, B, P-04]", numeros(lista));
    }

    @Test
    public void numerosGrandesNaoEstouramInt() {
        // Um parseInt ingênuo quebraria aqui.
        String grande = "99999999999999999999";
        assertTrue(ProjetorStorage.compararNumeros("1", grande) < 0);
        assertTrue(ProjetorStorage.compararNumeros(grande, "1") > 0);
    }

    private static String numeros(List<Projetor> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(lista.get(i).numero);
        }
        return sb.append("]").toString();
    }
}
