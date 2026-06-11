package com.example.lab6_20212472;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.lab6_20212472.databinding.ActivityMainBinding;
import com.example.lab6_20212472.fragments.EstadisticasFragment;
import com.example.lab6_20212472.fragments.PronosticosFragment;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            goToLoginActivity();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        if (savedInstanceState == null) {
            mostrarFragment(new PronosticosFragment());
        }

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_pronosticos) {
                mostrarFragment(new PronosticosFragment());
                return true;
            } else if (id == R.id.nav_estadisticas) {
                mostrarFragment(new EstadisticasFragment());
                return true;
            } else if (id == R.id.nav_logout) {
                cerrarSesion();
                return false;
            }

            return false;
        });
    }

    private void mostrarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void cerrarSesion() {
        firebaseAuth.signOut();
        goToLoginActivity();
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
