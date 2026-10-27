package com.example.qrcodeapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String PREFIXO_QR = "projetor:";

    private RecyclerView recyclerView;
    private ProjetorAdapter adapter;

    /**
     * A decodificação de imagem roda fora da thread principal: numa foto grande
     * ela leva centenas de milissegundos, e fazer isso na UI dispararia ANR.
     */
    private final ExecutorService executorLeitura = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() == null) {
                    Toast.makeText(this, R.string.msg_leitura_cancelada, Toast.LENGTH_SHORT).show();
                    return;
                }
                tratarQrCode(result.getContents());
            });

    private final ActivityResultLauncher<String> escolherImagemLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) {
                    return;
                }
                lerQrCodeDeImagem(uri);
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    abrirLeitorQrCode();
                } else {
                    explicarCameraNegada();
                }
            });

    private final ActivityResultLauncher<Intent> appSettingsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (temPermissaoCamera()) {
                    abrirLeitorQrCode();
                } else {
                    Toast.makeText(this, R.string.msg_camera_obrigatoria, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewProjetores);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Adapter criado uma única vez: só os dados mudam, a rolagem é preservada.
        adapter = new ProjetorAdapter(new ArrayList<>(), projetor -> abrirDetalhe(projetor.numero));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdicionar);
        fab.setOnClickListener(v -> mostrarDialogoAdicionar());

        findViewById(R.id.btnLerQr).setOnClickListener(v -> verificarPermissaoEAbrirLeitor());
        findViewById(R.id.btnLerQrImagem).setOnClickListener(v -> escolherImagemLauncher.launch("image/*"));
        findViewById(R.id.btnServico).setOnClickListener(v ->
                startActivity(new Intent(this, ServicoActivity.class)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorLeitura.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarLista();
    }

    private void carregarLista() {
        List<Projetor> lista = ProjetorStorage.carregarTodos(this);
        adapter.atualizar(lista);
    }

    private void tratarQrCode(String conteudo) {
        if (!conteudo.startsWith(PREFIXO_QR)) {
            Toast.makeText(this, getString(R.string.msg_qr_nao_reconhecido, conteudo), Toast.LENGTH_LONG).show();
            return;
        }
        String numero = Projetor.normalizarNumero(conteudo.substring(PREFIXO_QR.length()));
        if (!Projetor.isNumeroValido(numero)) {
            Toast.makeText(this, R.string.msg_qr_sem_numero, Toast.LENGTH_LONG).show();
            return;
        }
        abrirDetalhe(numero);
    }

    /**
     * Lê o QR Code de uma imagem escolhida na galeria. O item 5 da Issue #4 pede
     * "a partir de imagem ou câmera"; os dois caminhos caem no mesmo
     * {@link #tratarQrCode(String)}, então a validação do conteúdo é idêntica.
     */
    private void lerQrCodeDeImagem(Uri uri) {
        Toast.makeText(this, R.string.msg_qr_imagem_analisando, Toast.LENGTH_SHORT).show();

        executorLeitura.execute(() -> {
            String conteudo = null;
            String erro = null;
            try {
                conteudo = LeitorQrImagem.ler(this, uri);
            } catch (IOException | RuntimeException e) {
                erro = e.getMessage();
            }
            final String conteudoFinal = conteudo;
            final String erroFinal = erro;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (erroFinal != null) {
                    Toast.makeText(this, getString(R.string.msg_qr_imagem_erro, erroFinal),
                            Toast.LENGTH_LONG).show();
                } else if (conteudoFinal == null) {
                    Toast.makeText(this, R.string.msg_qr_imagem_sem_qr, Toast.LENGTH_LONG).show();
                } else {
                    tratarQrCode(conteudoFinal);
                }
            });
        });
    }

    private void mostrarDialogoAdicionar() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        input.setHint(R.string.hint_numero_projetor);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_cadastrar_projetor)
                .setView(input)
                .setPositiveButton(R.string.acao_cadastrar, null)
                .setNegativeButton(R.string.acao_cancelar, null)
                .create();

        // O botão positivo só fecha o dialog depois de validar, para o usuário
        // ver o erro em vez de perder o que digitou.
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> cadastrar(input, dialog)));

        dialog.show();
    }

    private void cadastrar(EditText input, AlertDialog dialog) {
        String numero = Projetor.normalizarNumero(input.getText().toString());
        if (numero.isEmpty()) {
            String digitado = input.getText().toString().trim();
            input.setError(digitado.isEmpty()
                    ? getString(R.string.erro_numero_obrigatorio)
                    : getString(R.string.erro_numero_invalido));
            return;
        }
        if (!ProjetorStorage.adicionar(this, numero)) {
            input.setError(getString(R.string.erro_numero_duplicado));
            return;
        }
        Toast.makeText(this, getString(R.string.msg_projetor_cadastrado, numero), Toast.LENGTH_SHORT).show();
        dialog.dismiss();
        carregarLista();
    }

    private void abrirDetalhe(String numero) {
        Intent intent = new Intent(this, DetalheProjetorActivity.class);
        intent.putExtra(DetalheProjetorActivity.EXTRA_NUMERO, numero);
        startActivity(intent);
    }

    private boolean temPermissaoCamera() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void verificarPermissaoEAbrirLeitor() {
        if (temPermissaoCamera()) {
            abrirLeitorQrCode();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirLeitorQrCode() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Aponte a câmera para o QR Code do projetor");
        options.setBeepEnabled(true);
        options.setOrientationLocked(false);
        barcodeLauncher.launch(options);
    }

    /**
     * Depois de um "não", há dois casos: o usuário pode recusar de novo (nesse
     * caso mostramos o porquê) ou negatives em definitivo (só dá para reativar
     * nas configurações).
     *
     * <p>Antes do Android 6 a permissão de câmera não era concedida em tempo de
     * execução, então não existe "negar agora e tentar depois": o caminho é
     * direto para as configurações. E
     * {@code shouldShowRequestPermissionRationale} nem existe nessas versões.
     */
    private void explicarCameraNegada() {
        boolean podePedirDeNovo = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && shouldShowRequestPermissionRationale(Manifest.permission.CAMERA);


        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_camera_bloqueada)
                .setNegativeButton(R.string.acao_voltar_para_lista, null);

        if (podePedirDeNovo) {
            builder.setMessage(R.string.msg_camera_negada_explicacao)
                    .setPositiveButton(R.string.acao_tentar_novamente,
                            (d, w) -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA));
        } else {
            builder.setMessage(R.string.msg_camera_bloqueada)
                    .setPositiveButton(R.string.acao_abrir_configuracoes, (d, w) -> abrirConfiguracoes());
        }

        builder.show();
    }

    private void abrirConfiguracoes() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        appSettingsLauncher.launch(intent);
    }
}
