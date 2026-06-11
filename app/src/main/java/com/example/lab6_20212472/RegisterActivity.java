package com.example.lab6_20212472;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_20212472.databinding.ActivityRegisterBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.setLanguageCode("es");

        binding.btnRegister.setOnClickListener(view -> intentarRegistro());
        binding.tvGoLogin.setOnClickListener(view -> finish());
    }

    private void intentarRegistro() {
        String email = binding.etEmail.getText() != null
                ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null
                ? binding.etPassword.getText().toString() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null
                ? binding.etConfirmPassword.getText().toString() : "";

        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        boolean valido = true;

        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.error_field_required));
            valido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            valido = false;
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.error_field_required));
            valido = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError(getString(R.string.error_password_length));
            valido = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_field_required));
            valido = false;
        } else if (!confirmPassword.equals(password)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            valido = false;
        }

        if (!valido) return;

        mostrarCargando(true);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    mostrarCargando(false);
                    Snackbar.make(binding.getRoot(), R.string.success_register, Snackbar.LENGTH_LONG).show();
                    goToMainActivity();
                })
                .addOnFailureListener(e -> {
                    mostrarCargando(false);
                    mostrarError(e);
                });
    }

    private void mostrarError(Exception e) {
        String mensaje;
        if (e instanceof FirebaseAuthUserCollisionException) {
            mensaje = getString(R.string.error_email_in_use);
        } else if (e instanceof FirebaseAuthWeakPasswordException) {
            mensaje = getString(R.string.error_password_length);
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            mensaje = getString(R.string.error_invalid_email);
        } else {
            mensaje = getString(R.string.error_register_failed);
        }
        Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_LONG).show();
    }

    private void mostrarCargando(boolean cargando) {
        binding.progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!cargando);
    }

    private void goToMainActivity() {
        android.content.Intent intent = new android.content.Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
