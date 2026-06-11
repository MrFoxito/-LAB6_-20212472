package com.example.lab6_20212472.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lab6_20212472.R;
import com.example.lab6_20212472.adapters.PronosticoAdapter;
import com.example.lab6_20212472.databinding.DialogPronosticoBinding;
import com.example.lab6_20212472.databinding.FragmentPronosticosBinding;
import com.example.lab6_20212472.models.Pronostico;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PronosticosFragment extends Fragment implements PronosticoAdapter.OnPronosticoActionListener {

    private static final String COLLECTION = "pronosticos";

    private FragmentPronosticosBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private PronosticoAdapter adapter;
    private ListenerRegistration listenerRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentPronosticosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        adapter = new PronosticoAdapter(this);
        binding.rvPronosticos.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPronosticos.setAdapter(adapter);

        binding.fabAdd.setOnClickListener(v -> mostrarDialogoPronostico(null));
    }

    @Override
    public void onStart() {
        super.onStart();
        escucharPronosticos();
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

    private void escucharPronosticos() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) return;

        listenerRegistration = db.collection(COLLECTION)
                .whereEqualTo("userId", user.getUid())
                .addSnapshotListener((snapshots, error) -> {
                    if (binding == null) return;

                    if (error != null || snapshots == null) {
                        return;
                    }

                    List<Pronostico> lista = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Pronostico pronostico = doc.toObject(Pronostico.class);
                        pronostico.setId(doc.getId());
                        lista.add(pronostico);
                    }

                    Collections.sort(lista, (a, b) -> {
                        if (a.getFecha() == null || b.getFecha() == null) return 0;
                        return a.getFecha().compareTo(b.getFecha());
                    });

                    adapter.setPronosticos(lista);
                    binding.tvEmpty.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                    binding.rvPronosticos.setVisibility(lista.isEmpty() ? View.GONE : View.VISIBLE);
                });
    }

    @Override
    public void onEditar(Pronostico pronostico) {
        mostrarDialogoPronostico(pronostico);
    }

    @Override
    public void onEliminar(Pronostico pronostico) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_eliminar_titulo)
                .setMessage(R.string.confirm_eliminar_mensaje)
                .setNegativeButton(R.string.btn_cancelar, null)
                .setPositiveButton(R.string.btn_eliminar, (dialog, which) -> eliminarPronostico(pronostico))
                .show();
    }

    private void eliminarPronostico(Pronostico pronostico) {
        db.collection(COLLECTION).document(pronostico.getId())
                .delete()
                .addOnFailureListener(e -> {
                    if (binding != null) {
                        Snackbar.make(binding.getRoot(), R.string.error_pronostico_eliminar, Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void mostrarDialogoPronostico(@Nullable Pronostico existente) {
        DialogPronosticoBinding dialogBinding = DialogPronosticoBinding.inflate(LayoutInflater.from(requireContext()));
        boolean esEdicion = existente != null;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "PE"));

        if (esEdicion) {
            dialogBinding.etSeleccionA.setText(existente.getSeleccionA());
            dialogBinding.etSeleccionB.setText(existente.getSeleccionB());
            dialogBinding.etGolesA.setText(String.valueOf(existente.getGolesA()));
            dialogBinding.etGolesB.setText(String.valueOf(existente.getGolesB()));

            if (existente.getFecha() != null) {
                calendar.setTime(existente.getFecha());
                dialogBinding.etFecha.setText(dateFormat.format(existente.getFecha()));
            }

            dialogBinding.layoutEstado.setVisibility(View.VISIBLE);
            if (Pronostico.ESTADO_ACERTADO.equals(existente.getEstado())) {
                dialogBinding.rgEstado.check(R.id.rb_acertado);
            } else if (Pronostico.ESTADO_FALLADO.equals(existente.getEstado())) {
                dialogBinding.rgEstado.check(R.id.rb_fallado);
            } else {
                dialogBinding.rgEstado.check(R.id.rb_pendiente);
            }
        }

        View.OnClickListener mostrarDatePicker = v -> new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth, 0, 0, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    dialogBinding.etFecha.setText(dateFormat.format(calendar.getTime()));
                    dialogBinding.tilFecha.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show();

        dialogBinding.etFecha.setOnClickListener(mostrarDatePicker);
        dialogBinding.tilFecha.setEndIconOnClickListener(mostrarDatePicker);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(esEdicion ? R.string.title_editar_pronostico : R.string.title_registrar_pronostico)
                .setView(dialogBinding.getRoot())
                .setNegativeButton(R.string.btn_cancelar, null)
                .setPositiveButton(R.string.btn_guardar, null)
                .create();

        dialog.setOnShowListener(d -> {
            Button btnGuardar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnGuardar.setOnClickListener(v -> {
                if (validarYGuardar(dialogBinding, existente, calendar)) {
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private boolean validarYGuardar(DialogPronosticoBinding b, @Nullable Pronostico existente, Calendar calendar) {
        String seleccionA = b.etSeleccionA.getText() != null ? b.etSeleccionA.getText().toString().trim() : "";
        String seleccionB = b.etSeleccionB.getText() != null ? b.etSeleccionB.getText().toString().trim() : "";
        String golesAStr = b.etGolesA.getText() != null ? b.etGolesA.getText().toString().trim() : "";
        String golesBStr = b.etGolesB.getText() != null ? b.etGolesB.getText().toString().trim() : "";
        String fechaStr = b.etFecha.getText() != null ? b.etFecha.getText().toString().trim() : "";

        b.tilSeleccionA.setError(null);
        b.tilSeleccionB.setError(null);
        b.tilFecha.setError(null);
        b.tilGolesA.setError(null);
        b.tilGolesB.setError(null);

        boolean valido = true;

        if (TextUtils.isEmpty(seleccionA)) {
            b.tilSeleccionA.setError(getString(R.string.error_field_required));
            valido = false;
        }
        if (TextUtils.isEmpty(seleccionB)) {
            b.tilSeleccionB.setError(getString(R.string.error_field_required));
            valido = false;
        }
        if (!TextUtils.isEmpty(seleccionA) && !TextUtils.isEmpty(seleccionB)
                && seleccionA.equalsIgnoreCase(seleccionB)) {
            b.tilSeleccionB.setError(getString(R.string.error_selecciones_iguales));
            valido = false;
        }
        if (TextUtils.isEmpty(fechaStr)) {
            b.tilFecha.setError(getString(R.string.error_fecha_required));
            valido = false;
        }

        Integer golesA = parseGoles(golesAStr);
        if (golesA == null) {
            b.tilGolesA.setError(getString(R.string.error_goles_required));
            valido = false;
        }
        Integer golesB = parseGoles(golesBStr);
        if (golesB == null) {
            b.tilGolesB.setError(getString(R.string.error_goles_required));
            valido = false;
        }

        if (!valido) return false;

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) return false;

        Date fecha = calendar.getTime();

        if (existente == null) {
            Pronostico nuevo = new Pronostico(user.getUid(), seleccionA, seleccionB, fecha,
                    golesA, golesB, Pronostico.ESTADO_PENDIENTE);

            db.collection(COLLECTION).add(nuevo)
                    .addOnSuccessListener(ref -> mostrarMensaje(R.string.success_pronostico_registrado))
                    .addOnFailureListener(e -> mostrarMensaje(R.string.error_pronostico_guardar));
        } else {
            String estado;
            int checkedId = b.rgEstado.getCheckedRadioButtonId();
            if (checkedId == R.id.rb_acertado) {
                estado = Pronostico.ESTADO_ACERTADO;
            } else if (checkedId == R.id.rb_fallado) {
                estado = Pronostico.ESTADO_FALLADO;
            } else {
                estado = Pronostico.ESTADO_PENDIENTE;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("seleccionA", seleccionA);
            updates.put("seleccionB", seleccionB);
            updates.put("fecha", fecha);
            updates.put("golesA", golesA);
            updates.put("golesB", golesB);
            updates.put("estado", estado);

            db.collection(COLLECTION).document(existente.getId())
                    .update(updates)
                    .addOnSuccessListener(unused -> mostrarMensaje(R.string.success_pronostico_actualizado))
                    .addOnFailureListener(e -> mostrarMensaje(R.string.error_pronostico_guardar));
        }

        return true;
    }

    private void mostrarMensaje(int resId) {
        if (binding != null) {
            Snackbar.make(binding.getRoot(), resId, Snackbar.LENGTH_LONG).show();
        }
    }

    @Nullable
    private Integer parseGoles(String value) {
        if (TextUtils.isEmpty(value)) return null;
        try {
            int n = Integer.parseInt(value);
            return n >= 0 ? n : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
