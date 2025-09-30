package co.edu.unipiloto.proyectoenvio;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RecoleccionesAdapter extends RecyclerView.Adapter<RecoleccionesAdapter.VH> {

    public interface OnItemClick {
        void onClick(Encomiendas e);
    }

    private List<Encomiendas> items;
    private final OnItemClick listener;

    public RecoleccionesAdapter(List<Encomiendas> items, OnItemClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recoleccion, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final Encomiendas e = items.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        holder.tvGuia.setText("Guía: " + e.getNumeroGuia());
        holder.tvRemitente.setText("Remitente: " + e.getRemitenteNombre() + " (" + e.getRemitenteCelular() + ")");
        holder.tvDestinatario.setText("Destinatario: " + e.getDestinatarioNombre() + " (" + e.getDestinatarioCelular() + ")");
        holder.tvDireccion.setText("Dirección destino: " + e.getDestinatarioDireccion());
        holder.tvEstado.setText("Estado: " + e.getEstado().name());

        holder.itemView.setOnClickListener(v -> listener.onClick(e));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    // 🔑 Refrescar lista desde BD
    public void updateData(List<Encomiendas> nuevas) {
        this.items = nuevas;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvGuia, tvRemitente, tvDestinatario, tvDireccion, tvEstado;

        VH(View itemView) {
            super(itemView);
            tvGuia = itemView.findViewById(R.id.tvGuia);
            tvRemitente = itemView.findViewById(R.id.tvRemitente);
            tvDestinatario = itemView.findViewById(R.id.tvDestinatario);
            tvDireccion = itemView.findViewById(R.id.tvDireccion);
            tvEstado = itemView.findViewById(R.id.tvEstado);

        }
    }
}
