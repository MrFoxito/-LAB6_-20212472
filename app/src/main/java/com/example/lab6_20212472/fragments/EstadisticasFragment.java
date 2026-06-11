package com.example.lab6_20212472.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lab6_20212472.R;
import com.example.lab6_20212472.databinding.FragmentEstadisticasBinding;
import com.example.lab6_20212472.models.Pronostico;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class EstadisticasFragment extends Fragment {

    private static final String COLLECTION = "pronosticos";

    private FragmentEstadisticasBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private ListenerRegistration listenerRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentEstadisticasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        escucharEstadisticas();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void escucharEstadisticas() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) return;

        listenerRegistration = db.collection(COLLECTION)
                .whereEqualTo("userId", user.getUid())
                .addSnapshotListener((snapshots, error) -> {
                    if (binding == null || error != null || snapshots == null) return;

                    int acertados = 0;
                    int fallados = 0;
                    int pendientes = 0;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String estado = doc.getString("estado");
                        if (Pronostico.ESTADO_ACERTADO.equals(estado)) {
                            acertados++;
                        } else if (Pronostico.ESTADO_FALLADO.equals(estado)) {
                            fallados++;
                        } else {
                            pendientes++;
                        }
                    }

                    int total = acertados + fallados + pendientes;
                    actualizarVista(total, acertados, fallados, pendientes);
                });
    }

    private void actualizarVista(int total, int acertados, int fallados, int pendientes) {
        if (total == 0) {
            binding.layoutStats.setVisibility(View.GONE);
            binding.tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        binding.layoutStats.setVisibility(View.VISIBLE);
        binding.tvEmpty.setVisibility(View.GONE);

        binding.tvTotal.setText(String.valueOf(total));
        binding.tvAcertados.setText(String.valueOf(acertados));
        binding.tvFallados.setText(String.valueOf(fallados));
        binding.tvPendientes.setText(String.valueOf(pendientes));

        setBarWeight(binding.barAcertados, acertados);
        setBarWeight(binding.barFallados, fallados);
        setBarWeight(binding.barPendientes, pendientes);

        int decididos = acertados + fallados;
        int porcentaje = decididos > 0 ? Math.round(acertados * 100f / decididos) : 0;
        binding.tvPorcentajeAcierto.setText(getString(R.string.stats_porcentaje_acierto, porcentaje));
    }

    private void setBarWeight(View bar, int count) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bar.getLayoutParams();
        params.weight = count;
        bar.setLayoutParams(params);
    }
}
