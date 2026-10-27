package com.example.qrcodeapp;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Ponte entre a thread do servidor HTTP e a câmera, que só funciona na thread
 * principal e precisa de uma Activity.
 *
 * <p>Fluxo de {@code POST /v1/qr/read}:
 * <ol>
 *   <li>a thread HTTP cria um {@link PedidoQr} e o coloca na fila;</li>
 *   <li>a Activity do serviço, na thread principal, retira o pedido e abre o
 *       leitor de QR Code já existente do app;</li>
 *   <li>o resultado é devolvido ao pedido, que destrava a thread HTTP;</li>
 *   <li>a thread HTTP devolve o JSON, ou um erro de timeout.</li>
 * </ol>
 *
 * <p>O prazo é menor que os 40 s do script de propósito: responder antes do
 * cliente desistir é melhor do que o cliente desistir sem receber nada.
 */
public final class FilaLeituraQr {

    /** Margem sobre o timeout do cliente (40 s no test-api.ps1). */
    public static final long TIMEOUT_MS = 35_000;

    private static final BlockingQueue<PedidoQr> PEDIDOS = new LinkedBlockingQueue<>();

    private FilaLeituraQr() {
    }

    /** Um pedido de leitura em andamento, do ponto de vista da thread HTTP. */
    public static final class PedidoQr {

        private final long criadoEm = System.currentTimeMillis();
        private final CountDownLatch destravado = new CountDownLatch(1);
        private volatile boolean atendido;
        private volatile String conteudo;
        private volatile String erro;

        public long getCriadoEm() {
            return criadoEm;
        }

        public boolean isAtendido() {
            return atendido;
        }

        String getConteudo() {
            return conteudo;
        }

        String getErro() {
            return erro;
        }

        /** Chamado pela thread principal quando a leitura terminou (ou falhou). */
        public void concluir(String conteudo, String erro) {
            if (atendido) {
                return;
            }
            this.conteudo = conteudo;
            this.erro = erro;
            this.atendido = true;
            destravado.countDown();
        }

        boolean aguardar(long ms) throws InterruptedException {
            return destravado.await(ms, TimeUnit.MILLISECONDS);
        }
    }

    /** Enfileira um pedido; a thread do servidor fica esperando o resultado. */
    public static String solicitarLeitura(long timeoutMs) throws InterruptedException {
        PedidoQr pedido = new PedidoQr();
        PEDIDOS.put(pedido);
        try {
            if (!pedido.aguardar(timeoutMs)) {
                return null;
            }
            if (pedido.erro != null) {
                throw new IllegalStateException(pedido.erro);
            }
            return pedido.conteudo;
        } finally {
            PEDIDOS.remove(pedido);
        }
    }

    /**
     * Retira o próximo pedido a atender, ou {@code null} se não houver nenhum
     * novo. Chamado pela Activity na thread principal.
     */
    public static PedidoQr proximoPedido() {
        for (PedidoQr p : PEDIDOS) {
            if (!p.atendido && p.criadoEm == primeiroPendente()) {
                return p;
            }
        }
        return null;
    }

    private static long primeiroPendente() {
        for (PedidoQr p : PEDIDOS) {
            if (!p.atendido) {
                return p.criadoEm;
            }
        }
        return -1;
    }

    /** Indica se há pedido esperando, para a Activity decidir se mostra algo. */
    public static boolean temPendente() {
        for (PedidoQr p : PEDIDOS) {
            if (!p.atendido) {
                return true;
            }
        }
        return false;
    }
}
