package com.example.qrcodeapp;

import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.WriterException;

import java.util.Date;

public class DetalheProjetorActivity extends AppCompatActivity {

    public static final String EXTRA_NUMERO = "extra_numero";

    private String numero;
    private Projetor projetor;

    private ImageView imageViewQr;
    private TextView textStatus;
    private TextView textInfoAlugado;

    private View formAluguel;
    private EditText fieldLocatario;
    private EditText fieldTelefone;
    private EditText fieldDataDevolucao;
    private Button btnRegistrarAluguel;

    private Button btnRegistrarDevolucao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_projetor);

        numero = Projetor.normalizarNumero(getIntent().getStringExtra(EXTRA_NUMERO));
        if (!Projetor.isNumeroValido(numero)) {
            finish();
            return;
        }

        imageViewQr = findViewById(R.id.imageViewQrDetalhe);
        textStatus = findViewById(R.id.textStatusDetalhe);
        textInfoAlugado = findViewById(R.id.textInfoAlugado);
        formAluguel = findViewById(R.id.formAluguel);
        fieldLocatario = findViewById(R.id.fieldLocatario);
        fieldTelefone = findViewById(R.id.fieldTelefone);
        fieldDataDevolucao = findViewById(R.id.fieldDataDevolucao);
        btnRegistrarAluguel = findViewById(R.id.btnRegistrarAluguel);
        btnRegistrarDevolucao = findViewById(R.id.btnRegistrarDevolucao);

        setTitle("Projetor " + numero);
        imageViewQr.setContentDescription(getString(R.string.desc_qr_projetor, numero));

        btnRegistrarAluguel.setOnClickListener(v -> registrarAluguel());
        btnRegistrarDevolucao.setOnClickListener(v -> registrarDevolucao());

        carregarProjetor();
    }

    private void carregarProjetor() {
        projetor = ProjetorStorage.buscarPorNumero(this, numero);
        if (projetor == null) {
            perguntarSeDesejaCadastrar();
            return;
        }
        atualizarTela();
        gerarQrCode();
    }

    /**
     * Escanear um QR Code não pode cadastrar o projetor por conta própria: um
     * QR Code impostor geraria um equipamento fantasma na lista. O cadastro só
     * acontece depois que o usuário confirmar na tela.
     */
    private void perguntarSeDesejaCadastrar() {
        textStatus.setText(R.string.status_nao_cadastrado);
        textStatus.setTextColor(Color.parseColor("#757575"));
        imageViewQr.setVisibility(View.INVISIBLE);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.titulo_projetor_nao_cadastrado, numero))
                .setMessage(R.string.msg_projetor_nao_cadastrado)
                .setCancelable(false)
                .setPositiveButton(R.string.acao_cadastrar, (d, w) -> {
                    // Pode falhar se o número for duplicado; nesse caso o
                    // projetor já está na lista e basta recarregá-lo.
                    if (!ProjetorStorage.adicionar(this, numero)) {
                        projetor = ProjetorStorage.buscarPorNumero(this, numero);
                    } else {
                        projetor = new Projetor(numero);
                    }
                    if (projetor == null) {
                        finish();
                        return;
                    }
                    imageViewQr.setVisibility(View.VISIBLE);
                    atualizarTela();
                    gerarQrCode();
                })
                .setNegativeButton(R.string.acao_voltar_para_lista, (d, w) -> finish())
                .show();
    }

    private void atualizarTela() {
        boolean alugado = projetor.isAlugado();

        if (alugado) {
            textStatus.setText(R.string.status_alugado);
            textStatus.setTextColor(Color.parseColor("#C62828"));

            StringBuilder info = new StringBuilder();
            info.append("Locatário: ").append(projetor.locatario.isEmpty() ? "—" : projetor.locatario);
            if (!projetor.telefone.isEmpty()) {
                info.append("\nTelefone: ").append(projetor.telefone);
            }
            if (!projetor.dataInicio.isEmpty()) {
                info.append("\nRetirado em: ").append(projetor.dataInicio);
            }
            if (!projetor.dataDevolucao.isEmpty()) {
                info.append("\nDevolução prevista: ").append(projetor.dataDevolucao);
            }
            textInfoAlugado.setText(info.toString());

            textInfoAlugado.setVisibility(View.VISIBLE);
            btnRegistrarDevolucao.setVisibility(View.VISIBLE);
            formAluguel.setVisibility(View.GONE);
        } else {
            textStatus.setText(R.string.status_disponivel);
            textStatus.setTextColor(Color.parseColor("#2E7D32"));

            textInfoAlugado.setVisibility(View.GONE);
            btnRegistrarDevolucao.setVisibility(View.GONE);
            formAluguel.setVisibility(View.VISIBLE);
        }
    }

    private void registrarAluguel() {
        String nome = fieldLocatario.getText().toString().trim();
        if (nome.isEmpty()) {
            fieldLocatario.setError(getString(R.string.erro_nome_obrigatorio));
            return;
        }

        projetor.status = Projetor.STATUS_ALUGADO;
        projetor.locatario = nome;
        projetor.telefone = fieldTelefone.getText().toString().trim();
        projetor.dataDevolucao = fieldDataDevolucao.getText().toString().trim();
        projetor.dataInicio = DateFormat.format("dd/MM/yyyy", new Date()).toString();

        if (!ProjetorStorage.salvarProjetor(this, projetor)) {
            Toast.makeText(this, R.string.msg_falha_salvar, Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, R.string.msg_aluguel_registrado, Toast.LENGTH_SHORT).show();
        atualizarTela();
    }

    private void registrarDevolucao() {
        projetor.status = Projetor.STATUS_DISPONIVEL;
        projetor.locatario = "";
        projetor.telefone = "";
        projetor.dataInicio = "";
        projetor.dataDevolucao = "";

        if (!ProjetorStorage.salvarProjetor(this, projetor)) {
            Toast.makeText(this, R.string.msg_falha_salvar, Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, R.string.msg_devolucao_registrada, Toast.LENGTH_SHORT).show();

        fieldLocatario.setText("");
        fieldTelefone.setText("");
        fieldDataDevolucao.setText("");
        atualizarTela();
    }

    private void gerarQrCode() {
        try {
            imageViewQr.setImageBitmap(
                    GeradorQrCode.gerar(projetor.conteudoQrCode(), GeradorQrCode.TAMANHO_PADRAO));
        } catch (WriterException | IllegalArgumentException e) {
            Toast.makeText(this, getString(R.string.erro_gerar_qr, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }
}
