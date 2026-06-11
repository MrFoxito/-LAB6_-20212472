package com.example.lab6_20212472;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_20212472.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.setLanguageCode("es");

        configurarGoogleSignIn();

        binding.btnLogin.setOnClickListener(view -> intentarLogin());
        binding.tvGoRegister.setOnClickListener(view ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        binding.btnLoginGoogle.setOnClickListener(view -> iniciarSesionConGoogle());
        binding.btnLoginGithub.setOnClickListener(view -> iniciarSesionConGithub());
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Si ya hay una sesión activa, vamos directo a la pantalla principal
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            goToMainActivity();
            return;
        }

        // Si la actividad fue recreada durante un login con GitHub, recuperamos el resultado pendiente
        Task<com.google.firebase.auth.AuthResult> pendingTask = firebaseAuth.getPendingAuthResult();
        if (pendingTask != null) {
            mostrarCargando(true);
            pendingTask
                    .addOnSuccessListener(authResult -> goToMainActivity())
                    .addOnFailureListener(e -> {
                        mostrarCargando(false);
                        mostrarErrorSocial(e);
                    });
        }
    }

    private void intentarLogin() {
        String email = binding.etEmail.getText() != null
                ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null
                ? binding.etPassword.getText().toString() : "";

        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

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
        }

        if (!valido) return;

        mostrarCargando(true);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> goToMainActivity())
                .addOnFailureListener(e -> {
                    mostrarCargando(false);
                    mostrarError(e);
                });
    }

    private void mostrarError(Exception e) {
        String mensaje;
        if (e instanceof FirebaseAuthInvalidUserException
                || e instanceof FirebaseAuthInvalidCredentialsException) {
            mensaje = getString(R.string.error_login_failed);
        } else {
            mensaje = getString(R.string.error_login_failed);
        }
        Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_LONG).show();
    }

    private void mostrarCargando(boolean cargando) {
        binding.progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!cargando);
        binding.btnLoginGoogle.setEnabled(!cargando);
        binding.btnLoginGithub.setEnabled(!cargando);
    }

    // ---------- Login con Google ----------

    private void configurarGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Task<GoogleSignInAccount> task =
                            GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        autenticarConGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        mostrarCargando(false);
                        mostrarErrorSocial(e);
                    }
                });
    }

    private void iniciarSesionConGoogle() {
        mostrarCargando(true);
        googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
    }

    private void autenticarConGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> goToMainActivity())
                .addOnFailureListener(e -> {
                    mostrarCargando(false);
                    mostrarErrorSocial(e);
                });
    }

    // ---------- Login con GitHub ----------

    private void iniciarSesionConGithub() {
        mostrarCargando(true);

        OAuthProvider.Builder provider = OAuthProvider.newBuilder("github.com");

        firebaseAuth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener(authResult -> goToMainActivity())
                .addOnFailureListener(e -> {
                    mostrarCargando(false);
                    mostrarErrorSocial(e);
                });
    }

    private void mostrarErrorSocial(Exception e) {
        String mensaje;
        if (e instanceof FirebaseAuthUserCollisionException) {
            mensaje = getString(R.string.error_account_exists_different_credential);
        } else {
            mensaje = getString(R.string.error_social_login_failed);
        }
        Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_LONG).show();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
