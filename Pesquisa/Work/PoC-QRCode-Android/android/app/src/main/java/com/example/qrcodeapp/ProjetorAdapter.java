package com.example.qrcodeapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProjetorAdapter extends RecyclerView.Adapter<ProjetorAdapter.ViewHolder> {

    public interface OnProjetorClickListener {
        void onProjetorClick(Projetor projetor);
    }

    private final List<Projetor> lista;
    private final OnProjetorClickListener listener;

    public ProjetorAdapter(List<Projetor> lista, OnProjetorClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    /**
     * Troca os dados exibidos mantendo a mesma instância do adapter (e, portanto,
     * a rolagem e o RecyclerView.RecycledViewPool).
     */
    public void atualizar(List<Projetor> novos) {
        lista.clear();
        lista.addAll(novos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_projetor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Projetor p = lista.get(position);
        Context ctx = holder.itemView.getContext();

        holder.textNumero.setText(ctx.getString(R.string.item_projetor_numero, p.numero));

        if (p.isAlugado()) {
            holder.textStatus.setText(R.string.status_alugado);
            holder.textStatus.setBackgroundColor(Color.parseColor("#FDECEA"));
            holder.textStatus.setTextColor(Color.parseColor("#C62828"));
            String quem = p.locatario.isEmpty()
                    ? ctx.getString(R.string.item_locatario_vazio)
                    : p.locatario;
            holder.textSubtitulo.setText(ctx.getString(R.string.item_projetor_com_quem, quem));
        } else {
            holder.textStatus.setText(R.string.status_disponivel);
            holder.textStatus.setBackgroundColor(Color.parseColor("#E6F4EA"));
            holder.textStatus.setTextColor(Color.parseColor("#2E7D32"));
            holder.textSubtitulo.setText(R.string.item_projetor_pronto);
        }

        holder.itemView.setOnClickListener(v -> listener.onProjetorClick(p));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNumero;
        TextView textSubtitulo;
        TextView textStatus;

        ViewHolder(View itemView) {
            super(itemView);
            textNumero = itemView.findViewById(R.id.textNumero);
            textSubtitulo = itemView.findViewById(R.id.textSubtitulo);
            textStatus = itemView.findViewById(R.id.textStatus);
        }
    }
}
