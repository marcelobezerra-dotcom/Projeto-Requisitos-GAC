package com.example.qrcodeapp;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * Tela de configuração e status do serviço HTTP.
 *
 * <p>É a tela de onde o serviço é ligado/desligado, e também o "hospedeiro" da
 * câmera: quando chega {@code POST /v1/qr/read}, a thread HTTP enfileira um
 * pedido e esta Activity o executa — o ZXing só funciona com uma Activity na
 * frente, então a câmera precisa de contexto de UI.
 *
 * <p>Se o app estiver em segundo plano, o Android 10+ pode impedir que a tela
 * volte para o primeiro plano sozinha. O botão de leitura manual existe para
 * esse caso: o pedido segue na fila e é atendido assim que a tela é aberta.
 */
public class ServicoActivity extends AppCompatActivity {

    private static final long INTERVALO_VERIFICACAO_MS = 500;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private FilaLeituraQr.PedidoQr pedidoEmAndamento;

    private TextView txtStatus;
    private TextView txtEndereco;
    private TextView txtExemploPowershell;
    private EditText campoPorta;
    private EditText campoToken;
    private Button btnLerQr;
    private Button btnIniciar;
    private Runnable verificarPedidos;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                String conteudo = result.getContents();
                concluirPedido(conteudo);
                if (conteudo != null) {
                    Toast.makeText(this, getString(R.string.servico_leitura_recebida, conteudo),
                            Toast.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    abrirLeitor();
                } else {
                    Toast.makeText(this, R.string.msg_camera_obrigatoria, Toast.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<String> notificacaoPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                // A permissão é pedida primeiro justamente para o serviço poder
                // subir logo depois; iniciar aqui mantém a ordem e garante que o
                // botão não fique esperando uma permissão que nunca foi concedida.
                iniciarServico();
                if (!granted) {
                    // Recusar não impede o serviço: a porta abre normalmente e
                    // só a notificação persistente não vai aparecer.
                    Toast.makeText(this, R.string.servico_notificacao_sem_permissao,
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servico);

        txtStatus = findViewById(R.id.txtStatusServico);
        txtEndereco = findViewById(R.id.txtEnderecoServico);
        txtExemploPowershell = findViewById(R.id.txtExemploPowershell);
        campoPorta = findViewById(R.id.campoPorta);
        campoToken = findViewById(R.id.campoToken);
        btnLerQr = findViewById(R.id.btnLerQrServico);
        btnIniciar = findViewById(R.id.btnIniciarServico);

        findViewById(R.id.btnCopiarToken).setOnClickListener(v -> copiarToken());
        btnIniciar.setOnClickListener(v -> alternarServico());
        btnLerQr.setOnClickListener(v -> verificarPermissaoEAbrirLeitor());

        campoPorta.setText(String.valueOf(ServicoConfig.getPorta(this)));
        campoToken.setText(ServicoConfig.getToken(this));
        campoToken.setOnFocusChangeListener((v, temFoco) -> {
            // Grava ao sair do campo, e não a cada tecla: a cada tecla o
            // serviço cairia fora do token e o script receberia 401.
            if (!temFoco) {
                ServicoConfig.setToken(this, campoToken.getText().toString());
            }
        });
        campoPorta.setOnFocusChangeListener((v, temFoco) -> {
            if (!temFoco) {
                aplicarPorta();
            }
        });

        txtEndereco.setText(getString(R.string.servico_endereco,
                obterIpWifi(), ServicoConfig.getPorta(this)));
        atualizarExemploPowershell();

        verificarPedidos = new Runnable() {
            @Override
            public void run() {
                atenderPedidoPendente();
                handler.postDelayed(this, INTERVALO_VERIFICACAO_MS);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        // O serviço pode ter sido parado de outra tela (ou pelo sistema), então o
        // botão reflete o estado real, não o que foi apertado por último.
        boolean ativo = ServicoQrCode.isRunning();
        btnIniciar.setText(ativo ? R.string.btn_parar_servico : R.string.btn_iniciar_servico);
        atualizarStatus(ativo);
        handler.post(verificarPedidos);
    }

    @Override
    protected void onStop() {
        // Para de varrer a fila fora da tela: sem Activity na frente o ZXing não
        // tem onde abrir, e o pedido ficaria esperando até o timeout.
        handler.removeCallbacks(verificarPedidos);
        super.onStop();
    }

    /**
     * Se há pedido esperando e o leitor não está aberto, abre a câmera. Guardar
     * o pedido impede que os 500 ms do ciclo abram o scanner várias vezes.
     */
    private void atenderPedidoPendente() {
        if (pedidoEmAndamento != null || !FilaLeituraQr.temPendente()) {
            return;
        }
        FilaLeituraQr.PedidoQr pedido = FilaLeituraQr.proximoPedido();
        if (pedido == null) {
            return;
        }
        pedidoEmAndamento = pedido;
        txtStatus.setText(R.string.servico_status_aguardando_qr);
        verificarPermissaoEAbrirLeitor();
    }

    private void concluirPedido(String conteudo) {
        FilaLeituraQr.PedidoQr pedido = pedidoEmAndamento;
        pedidoEmAndamento = null;
        if (pedido != null) {
            pedido.concluir(conteudo,
                    conteudo == null ? getString(R.string.servico_leitura_cancelada) : null);
        }
    }

    private void verificarPermissaoEAbrirLeitor() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            abrirLeitor();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirLeitor() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt(getString(R.string.servico_prompt_camera));
        options.setBeepEnabled(true);
        options.setOrientationLocked(false);
        barcodeLauncher.launch(options);
    }

    private void alternarServico() {
        if (ServicoQrCode.isRunning()) {
            ServicoQrCode.parar(this);
            btnIniciar.setText(R.string.btn_iniciar_servico);
            atualizarStatus(false);
            return;
        }
        // No Android 13+ a permissão de notificação precisa vir ANTES de
        // startForeground: subir o serviço sem ela derruba a Activity com
        // SecurityException, e o usuário só vê o app fechar ao clicar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificacaoPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            iniciarServico();
        }
    }

    private void iniciarServico() {
        aplicarPorta();
        ServicoQrCode.iniciar(this, ServicoConfig.getPorta(this));
        btnIniciar.setText(R.string.btn_parar_servico);
    }

    private void aplicarPorta() {
        String texto = campoPorta.getText().toString().trim();
        int porta;
        try {
            porta = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            porta = ServicoConfig.PORTA_PADRAO;
        }
        // Fora da faixa, o bind falha com uma exceção pouco explicativa; vale
        // avisar antes de derrubar o serviço.
        if (porta < 1024 || porta > 65535) {
            Toast.makeText(this, R.string.servico_porta_invalida, Toast.LENGTH_LONG).show();
            campoPorta.setText(String.valueOf(ServicoConfig.getPorta(this)));
            return;
        }
        ServicoConfig.setPorta(this, porta);
        campoPorta.setText(String.valueOf(porta));
        atualizarExemploPowershell();
    }

    private void atualizarStatus(boolean ativo) {
        int porta = ServicoConfig.getPorta(this);
        if (ativo) {
            txtStatus.setText(getString(R.string.servico_status_ativo, porta));
            return;
        }
        // Falha ao subir é o caso que interessa: sem ela a tela mostra só
        // "Parado" e não há como saber se a porta está ocupada ou negada.
        String falha = ServicoQrCode.getUltimaFalha();
        txtStatus.setText(falha == null
                ? getString(R.string.servico_status_inativo)
                : getString(R.string.servico_status_falha, falha));
    }

    private void copiarToken() {
        ServicoConfig.setToken(this, campoToken.getText().toString());
        ClipData dados = ClipData.newPlainText(
                getString(R.string.servico_token), campoToken.getText().toString());
        ClipboardManager areaTransferencia =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (areaTransferencia != null) {
            areaTransferencia.setPrimaryClip(dados);
            Toast.makeText(this, R.string.servico_token_copiado, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * IP da interface Wi-Fi, que é o que o professor digita no
     * {@code -Ip do test-api.ps1}. Sem Wi-Fi ligado não há IP de rede, e aí o
     * endereço útil é o loopback.
     */
    private String obterIpWifi() {
        try {
            WifiManager wifi = (WifiManager) getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            if (wifi == null || wifi.getConnectionInfo() == null) {
                return "127.0.0.1";
            }
            int ip = wifi.getConnectionInfo().getIpAddress();
            return String.format("%d.%d.%d.%d",
                    ip & 255, (ip >> 8) & 255, (ip >> 16) & 255, (ip >> 24) & 255);
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    private void atualizarExemploPowershell() {
        String ip = obterIpWifi();
        int porta = ServicoConfig.getPorta(this);
        txtExemploPowershell.setText(String.format(
                ".\\test-api.ps1 -Ip %s -Port %d -TokenPath .\\appnfc-test-token.txt -ReadQr",
                ip, porta));
    }
}
