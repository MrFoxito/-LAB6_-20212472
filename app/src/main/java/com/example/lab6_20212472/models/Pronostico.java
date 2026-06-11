package com.example.lab6_20212472.models;

import com.google.firebase.firestore.Exclude;

import java.util.Date;

public class Pronostico {

    public static final String ESTADO_PENDIENTE = "Pendiente";
    public static final String ESTADO_ACERTADO = "Acertado";
    public static final String ESTADO_FALLADO = "Fallado";

    private String userId;
    private String seleccionA;
    private String seleccionB;
    private Date fecha;
    private int golesA;
    private int golesB;
    private String estado;

    @Exclude
    private String id;

    public Pronostico() {
        // Constructor vacío requerido por Firestore
    }

    public Pronostico(String userId, String seleccionA, String seleccionB, Date fecha,
                       int golesA, int golesB, String estado) {
        this.userId = userId;
        this.seleccionA = seleccionA;
        this.seleccionB = seleccionB;
        this.fecha = fecha;
        this.golesA = golesA;
        this.golesB = golesB;
        this.estado = estado;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSeleccionA() {
        return seleccionA;
    }

    public void setSeleccionA(String seleccionA) {
        this.seleccionA = seleccionA;
    }

    public String getSeleccionB() {
        return seleccionB;
    }

    public void setSeleccionB(String seleccionB) {
        this.seleccionB = seleccionB;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public int getGolesA() {
        return golesA;
    }

    public void setGolesA(int golesA) {
        this.golesA = golesA;
    }

    public int getGolesB() {
        return golesB;
    }

    public void setGolesB(int golesB) {
        this.golesB = golesB;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    @Exclude
    public boolean isPendiente() {
        return ESTADO_PENDIENTE.equals(estado);
    }
}
