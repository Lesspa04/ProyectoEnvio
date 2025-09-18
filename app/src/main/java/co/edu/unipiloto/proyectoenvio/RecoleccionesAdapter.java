package co.edu.unipiloto.proyectoenvio;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecoleccionesAdapter extends RecyclerView.Adapter<RecoleccionesAdapter.VH> {

    public interface OnItemClick { void onClick(Encomiendas e); }

    private List<Encomiendas> items;
    private OnItemClick listener;

    public RecoleccionesAdapter(List<Encomiendas> items, OnItemClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recoleccion, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final Encomiendas e = items.get(position);
        holder.tvGuia.setText(e.getNumeroGuia());
        holder.tvNombre.setText("Remitente: " + e.getRemitenteNombre());
        holder.tvDireccion.setText("Dirección: " + e.getRemitenteDireccion());
        holder.tvEstado.setText("Estado: " + e.getEstado().name());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { listener.onClick(e); }
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvGuia, tvNombre, tvDireccion, tvEstado;
        VH(View itemView) {
            super(itemView);
            tvGuia = itemView.findViewById(R.id.tvGuia);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDireccion = itemView.findViewById(R.id.tvDireccion);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }
}
