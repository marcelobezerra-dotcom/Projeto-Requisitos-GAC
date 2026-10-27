package com.example.qrcodeapp;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Guarda a lista de projetores no armazenamento local do aparelho (SharedPreferences),
 * no formato JSON. Não depende de internet nem de servidor.
 */
public class ProjetorStorage {

    private static final String PREFS_NAME = "projetores_prefs";
    private static final String KEY_LISTA = "lista_projetores";

    /**
     * Ordena projetores numéricos em ordem numérica e os alfanuméricos em ordem
     * alfabética, sempre depois dos numéricos.
     *
     * <p>É uma ordem total: para qualquer trio de números, a comparação devolve
     * sempre o mesmo resultado. Isso importa porque o {@link Collections#sort} usa
     * TimSort, que estoura com comparadores que misturam critério numérico e
     * alfabético na mesma lista.
     */
    private static final Comparator<Projetor> ORDENACAO = new Comparator<Projetor>() {
        @Override
        public int compare(Projetor a, Projetor b) {
            return compararNumeros(a.numero, b.numero);
        }
    };

    /**
     * Compara dois identificadores já normalizados. A normalização é repetida
     * aqui de propósito: o resultado não pode depender de o chamador ter
     * lembrado de normalizar antes.
     */
    static int compararNumeros(String a, String b) {
        String x = normalizarOuVazio(a);
        String y = normalizarOuVazio(b);

        boolean xNum = Projetor.isSomenteDigitos(x);
        boolean yNum = Projetor.isSomenteDigitos(y);

        if (xNum && yNum) {
            return compararSequenciaDeDigitos(x, y);
        }
        if (xNum != yNum) {
            return xNum ? -1 : 1;
        }
        int r = x.compareToIgnoreCase(y);
        return r != 0 ? r : x.compareTo(y);
    }

    private static String normalizarOuVazio(String s) {
        return Projetor.normalizarNumero(s);
    }

    /**
     * Compara dois números apenas com dígitos pelo tamanho e depois pelos
     * caracteres, o que evita overflow de {@code Integer.parseInt} em
     * identificadores grandes.
     */
    private static int compararSequenciaDeDigitos(String a, String b) {
        if (a.length() != b.length()) {
            return Integer.compare(a.length(), b.length());
        }
        return a.compareTo(b);
    }

    public static List<Projetor> carregarTodos(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_LISTA, null);
        Map<String, Projetor> porNumero = new LinkedHashMap<>();

        if (json == null) {
            // Primeira vez usando o app: já cadastra os projetores 1, 2 e 3 como exemplo.
            porNumero.put("1", new Projetor("1"));
            porNumero.put("2", new Projetor("2"));
            porNumero.put("3", new Projetor("3"));
            List<Projetor> exemplos = ordenar(new ArrayList<>(porNumero.values()));
            salvarTodos(context, exemplos);
            return exemplos;
        }

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                Projetor p = Projetor.fromJson(array.getJSONObject(i));
                if (Projetor.isNumeroValido(p.numero) && !porNumero.containsKey(p.numero)) {
                    porNumero.put(p.numero, p);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ordenar(new ArrayList<>(porNumero.values()));
    }

    private static List<Projetor> ordenar(List<Projetor> lista) {
        Collections.sort(lista, ORDENACAO);
        return lista;
    }

    public static void salvarTodos(Context context, List<Projetor> lista) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray array = new JSONArray();
        try {
            for (Projetor p : lista) {
                if (Projetor.isNumeroValido(p.numero)) {
                    array.put(p.toJson());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        prefs.edit().putString(KEY_LISTA, array.toString()).apply();
    }

    public static Projetor buscarPorNumero(Context context, String numero) {
        String alvo = Projetor.normalizarNumero(numero);
        if (!Projetor.isNumeroValido(alvo)) {
            return null;
        }
        for (Projetor p : carregarTodos(context)) {
            if (alvo.equals(p.numero)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Insere um projetor novo (se o número já existir, não faz nada).
     * Retorna true se inseriu.
     */
    public static boolean adicionar(Context context, String numero) {
        String normalizado = Projetor.normalizarNumero(numero);
        if (!Projetor.isNumeroValido(normalizado)) {
            return false;
        }
        List<Projetor> lista = carregarTodos(context);
        for (Projetor p : lista) {
            if (normalizado.equals(p.numero)) {
                return false;
            }
        }
        lista.add(new Projetor(normalizado));
        salvarTodos(context, lista);
        return true;
    }

    /**
     * Atualiza um projetor já cadastrado.
     *
     * <p>Não cria mais projetores automaticamente: um número desconhecido é
     * simplesmente ignorado, para que escanear um QR Code alheio não cadastre
     * equipamentos que não existem. Use {@link #adicionar} para cadastrar.
     *
     * @return true se o projetor foi encontrado e salvo.
     */
    public static boolean salvarProjetor(Context context, Projetor projetor) {
        if (projetor == null || !Projetor.isNumeroValido(projetor.numero)) {
            return false;
        }
        List<Projetor> lista = carregarTodos(context);
        for (int i = 0; i < lista.size(); i++) {
            if (projetor.numero.equals(lista.get(i).numero)) {
                lista.set(i, projetor);
                salvarTodos(context, lista);
                return true;
            }
        }
        return false;
    }

    public static void remover(Context context, String numero) {
        String normalizado = Projetor.normalizarNumero(numero);
        List<Projetor> lista = carregarTodos(context);
        for (int i = 0; i < lista.size(); i++) {
            if (normalizado.equals(lista.get(i).numero)) {
                lista.remove(i);
                break;
            }
        }
        salvarTodos(context, lista);
    }
}
