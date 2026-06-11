package com.example.lab6_20212472.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6_20212472.R;
import com.example.lab6_20212472.databinding.ItemPronosticoBinding;
import com.example.lab6_20212472.models.Pronostico;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PronosticoAdapter extends RecyclerView.Adapter<PronosticoAdapter.PronosticoViewHolder> {

    public interface OnPronosticoActionListener {
        void onEditar(Pronostico pronostico);

        void onEliminar(Pronostico pronostico);
    }

    private final List<Pronostico> pronosticos = new ArrayList<>();
    private final OnPronosticoActionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "PE"));

    public PronosticoAdapter(OnPronosticoActionListener listener) {
        this.listener = listener;
    }

    public void setPronosticos(List<Pronostico> nuevos) {
        pronosticos.clear();
        pronosticos.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PronosticoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPronosticoBinding binding = ItemPronosticoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PronosticoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PronosticoViewHolder holder, int position) {
        holder.bind(pronosticos.get(position));
    }

    @Override
    public int getItemCount() {
        return pronosticos.size();
    }

    class PronosticoViewHolder extends RecyclerView.ViewHolder {

        private final ItemPronosticoBinding binding;

        PronosticoViewHolder(ItemPronosticoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Pronostico pronostico) {
            Context context = binding.getRoot().getContext();

            binding.tvEquipos.setText(context.getString(R.string.equipos_vs,
                    pronostico.getSeleccionA(), pronostico.getSeleccionB()));

            String fechaTexto = pronostico.getFecha() != null
                    ? dateFormat.format(pronostico.getFecha()) : "-";
            binding.tvFecha.setText(context.getString(R.string.fecha_label, fechaTexto));

            binding.tvResultado.setText(context.getString(R.string.resultado_pronosticado,
                    pronostico.getGolesA(), pronostico.getGolesB()));

            binding.tvEstado.setText(pronostico.getEstado());

            int colorEstado;
            if (Pronostico.ESTADO_ACERTADO.equals(pronostico.getEstado())) {
                colorEstado = ContextCompat.getColor(context, R.color.estado_acertado);
            } else if (Pronostico.ESTADO_FALLADO.equals(pronostico.getEstado())) {
                colorEstado = ContextCompat.getColor(context, R.color.estado_fallado);
            } else {
                colorEstado = ContextCompat.getColor(context, R.color.estado_pendiente);
            }
            binding.tvEstado.getBackground().mutate().setTint(colorEstado);

            boolean editable = pronostico.isPendiente();
            binding.btnEditar.setVisibility(editable ? View.VISIBLE : View.GONE);
            binding.btnEliminar.setVisibility(editable ? View.VISIBLE : View.GONE);

            binding.btnEditar.setOnClickListener(v -> listener.onEditar(pronostico));
            binding.btnEliminar.setOnClickListener(v -> listener.onEliminar(pronostico));
        }
    }
}
