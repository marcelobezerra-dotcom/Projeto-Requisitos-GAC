package com.example.qrcodeapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

/**
 * Mantém a porta 8787 aberta enquanto o app não estiver sendo usado.
 *
 * <p>Precisa ser foreground service porque, a partir do Android 8, o sistema
 * mata processos em background depois de poucos minutos — e com a porta fechada
 * o {@code test-api.ps1} receberia "não foi possível conectar ao celular" sem
 * nenhum pedido jamais ter chegado ao app.
 *
 * <p>Com {@code targetSdk 34} o Android exige declarar o tipo do serviço
 * (aqui {@code dataSync}) e a permissão correspondente, senão o
 * {@code startForeground} lança {@link SecurityException}.
 */
public class ServicoQrCode extends Service {

    private static final String TAG = "ServicoQrCode";
    private static final String CHANNEL_ID = "servico_qrcode";
    private static final int NOTIFICACAO_ID = 42;

    public static final String EXTRA_INICIAR = "iniciar";
    public static final String EXTRA_PORTA = "porta";

    private ServicoHttp servidor;
    private int portaAtual = ServicoConfig.PORTA_PADRAO;

    /**
     * Estado consultado pela {@link ServicoActivity}. Um {@code Service} não é
     * "instanciável" pela Activity, então o serviço publica o próprio status
     * aqui em vez de se ligar por bind.
     */
    private static volatile ServicoHttp servidorAtivo;
    private static volatile int portaAtiva = ServicoConfig.PORTA_PADRAO;

    /** Motivo da última falha ao subir, para a tela mostrar em vez de sumir. */
    private static volatile String ultimaFalha;

    public static boolean isRunning() {
        return servidorAtivo != null;
    }

    public static int getPortaAtiva() {
        return portaAtiva;
    }

    public static String getUltimaFalha() {
        return ultimaFalha;
    }

    /**
     * Sobe o serviço em foreground. Seguro para chamar várias vezes: o
     * {@code startForegroundService} só pode ser seguido de um
     * {@code startForeground} em até 5 s, e chamar aqui garante isso mesmo
     * quando a Activity chama repeatedly.
     */
    public static void iniciar(Context context, int porta) {
        Intent intent = new Intent(context, ServicoQrCode.class);
        intent.putExtra(EXTRA_INICIAR, true);
        intent.putExtra(EXTRA_PORTA, porta);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void parar(Context context) {
        context.stopService(new Intent(context, ServicoQrCode.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        criarCanal();
        // showWhen=false: notificação persistente não deve puxar a sombra para
        // baixo toda vez que o app é aberto.
        startForeground(NOTIFICACAO_ID, montarNotificacao(portaAtual, false));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int porta = intent != null && intent.getIntExtra(EXTRA_PORTA, 0) > 0
                ? intent.getIntExtra(EXTRA_PORTA, ServicoConfig.PORTA_PADRAO)
                : ServicoConfig.getPorta(this);

        if (porta != portaAtual || servidor == null) {
            reiniciar(porta);
        } else {
            atualizarNotificacao(porta, true);
        }
        // START_STICKY: se o sistema matar o serviço sob pressão de memória,
        // ele volta sozinho. Sem isso a porta fecharia silenciosamente.
        return START_STICKY;
    }

    private void reiniciar(int porta) {
        if (servidor != null) {
            try {
                servidor.stop();
            } catch (Exception e) {
                Log.w(TAG, "Falha ao parar o servidor anterior: " + e.getMessage());
            }
        }
        portaAtual = porta;
        ServicoHttp novo = new ServicoHttp(getApplicationContext(), porta);
        try {
            novo.start(1500, true);
            servidor = novo;
            servidorAtivo = novo;
            portaAtiva = porta;
            ultimaFalha = null;
            ServicoConfig.setPorta(this, porta);
            atualizarNotificacao(porta, true);
        } catch (IOException e) {
            // "Address already in use" acontece se o start foi repetido com a
            // tela ainda aberta; "Permission denied" se a porta for privilegiada.
            // Nos dois casos o certo é registrar o motivo e deixar o app vivo —
            // stopSelf() aqui derrubava a Activity junto, sem mensagem útil.
            Log.e(TAG, "Nao foi possivel abrir a porta " + porta, e);
            ultimaFalha = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            atualizarNotificacao(porta, false);
            stopSelf();
        } catch (SecurityException e) {
            // startForeground sem a permissão de notificaçãoGrantada cai aqui em
            // algumas ROMs; tratar igual a falha de socket evita o crash.
            Log.e(TAG, "startForeground negado", e);
            ultimaFalha = getString(R.string.servico_erro_sem_permissao);
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        if (servidor != null) {
            try {
                servidor.stop();
            } catch (Exception e) {
                Log.w(TAG, "Falha ao parar o servidor: " + e.getMessage());
            }
            servidor = null;
            servidorAtivo = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void criarCanal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager gerente =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (gerente == null || gerente.getNotificationChannel(CHANNEL_ID) != null) {
            return;
        }
        NotificationChannel canal = new NotificationChannel(CHANNEL_ID,
                getString(R.string.servico_canal_nome), NotificationManager.IMPORTANCE_LOW);
        canal.setDescription(getString(R.string.servico_canal_descricao));
        canal.setShowBadge(false);
        gerente.createNotificationChannel(canal);
    }

    private void atualizarNotificacao(int porta, boolean ativo) {
        // A partir do Android 13, notificar exige POST_NOTIFICATIONS em runtime.
        // Sem a permissão o notify é apenas ignorado — o serviço continua
        // funcionando e a porta fica aberta, que é o que interessa ao script.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManager gerente =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (gerente != null) {
            gerente.notify(NOTIFICACAO_ID, montarNotificacao(porta, ativo));
        }
    }

    private Notification montarNotificacao(int porta, boolean ativo) {
        Intent abrir = new Intent(this, ServicoActivity.class);
        PendingIntent abrirApp = PendingIntent.getActivity(this, 0, abrir,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.servico_notificacao_titulo))
                .setContentText(ativo
                        ? getString(R.string.servico_notificacao_ativa, porta)
                        : getString(R.string.servico_notificacao_falha, porta))
                .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                .setContentIntent(abrirApp)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setShowWhen(false)
                .build();
    }
}
