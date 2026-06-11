# Lab 6 — Sistema de Seguimiento del Mundial (Mundial Tracker)

Curso: **Servicios y Aplicaciones para IoT [1TEL05]** — PUCP, Semestre 2026-1
Alumno: Miguel Angel Alvizuri (20212472)
Paquete: `com.example.lab6_20212472`

## Descripción

App Android nativa (Java, Views + ViewBinding) que usa **Firebase** como Backend-as-a-Service para
registrar y dar seguimiento a pronósticos del Mundial de Fútbol.

## Funcionalidades implementadas

1. **Autenticación por correo** — Login y registro con UI personalizada en español
   (sin usar las pantallas en inglés de FirebaseUI), barra de navegación inferior con
   "Mis Pronósticos", "Estadísticas" y "Cerrar Sesión".
2. **CRUD de Pronósticos en Firestore** — listado en tiempo real (`addSnapshotListener`),
   registro/edición con validaciones (Selección A ≠ B, fecha obligatoria, goles ≥ 0),
   estado automático "Pendiente" al crear, edición de estado a Acertado/Fallado
   (bloquea edición/eliminación una vez decidido), eliminación con confirmación.
3. **Estadísticas en tiempo real** — panel reactivo con Total, Pendientes, Acertados,
   Fallados, barra de distribución y porcentaje de acierto.
4. **Login social** — botones de "Continuar con Google" y "Continuar con GitHub"
   integrados con Firebase Authentication.

## Cómo compilar y ejecutar

1. Clona el repositorio.
2. Abre la carpeta del proyecto en Android Studio y espera a que sincronice Gradle.
3. `app/google-services.json` ya está incluido en el repo (proyecto Firebase
   `mundial-tracker-lab6`), no es necesario configurarlo de nuevo.
4. Ejecuta (`Run ▶`) en un emulador o dispositivo con API 26+.

## ⚠️ Nota sobre el login con Google en otra computadora

El proveedor **Google Sign-In** está vinculado al SHA-1 del `debug.keystore` de la
computadora original (cada máquina genera un `debug.keystore` distinto). Si se ejecuta
el proyecto desde otra PC:

- **Login con correo/contraseña**, **GitHub**, el **CRUD de pronósticos** y las
  **estadísticas** funcionan sin ningún cambio.
- **Login con Google** puede fallar con un error `DEVELOPER_ERROR` / "No se pudo
  iniciar sesión con el proveedor seleccionado", porque el SHA-1 de esa máquina no
  está registrado en Firebase.

Para habilitarlo en otra PC:
1. Ejecutar `./gradlew signingReport` y copiar el `SHA1` de la variante `debug`.
2. En Firebase Console → Configuración del proyecto → Tus apps → app Android →
   "Agregar huella digital" → pegar el SHA-1.
3. Volver a compilar la app (no es necesario descargar un nuevo `google-services.json`
   para que el SHA-1 adicional surta efecto en Google Sign-In).

## Reglas de seguridad de Firestore

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /pronosticos/{pronosticoId} {
      allow read, update, delete: if request.auth != null
                                   && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null
                     && request.auth.uid == request.resource.data.userId;
    }
  }
}
```

## Stack técnico

- Java (Android Views + ViewBinding), Material Components 3
- Firebase Authentication (correo/contraseña, Google, GitHub)
- Cloud Firestore (datos en tiempo real)
- AGP 9.1.0 / Gradle 9.4.1, compileSdk/targetSdk 36, minSdk 26
