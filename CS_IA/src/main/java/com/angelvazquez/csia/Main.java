package com.angelvazquez.csia;

import java.awt.Window;

import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.ConfiguracionManager;
import com.angelvazquez.csia.ui.ventanas.AsignarTab;
import com.angelvazquez.csia.ui.ventanas.LoginPage;
import com.angelvazquez.csia.ui.ventanas.RegistarTab;
import com.angelvazquez.csia.ui.ventanas.VisualizarAlumnos;
import com.angelvazquez.csia.ui.ventanas.VisualizarProfesores;
import com.angelvazquez.csia.ui.ventanas.WelcomePage;

public class Main {

    private static ConfigDB configuracion;

    public static void main(String[] args) {
        configuracion = new ConfiguracionManager().inicializarConfiguracion();
        if (configuracion == null) {
            return;
        }
        new LoginPage();
    }

    public static ConfigDB getConfiguracion() {
        if (configuracion == null) {
            throw new IllegalStateException("Configuración no inicializada");
        }
        return configuracion;
    }

    public static void LogIn() {
        new LoginPage();
    }

    public static void Welcome() {
        WelcomePage ventana = new WelcomePage();
        ventana.setVisible(true);
    }

    public static void Asignar() {
        Asignar(null);
    }

    public static void Asignar(Window parent) {
        AsignarTab ventana = new AsignarTab(parent);
        ventana.setVisible(true);
    }

    public static void AlumTabla() {
        AlumTabla(null);
    }

    public static void AlumTabla(Window parent) {
        VisualizarAlumnos ventana = new VisualizarAlumnos(parent);
        ventana.setVisible(true);
    }

    public static void ProfeTabla() {
        ProfeTabla(null);
    }

    public static void ProfeTabla(Window parent) {
        VisualizarProfesores ventana = new VisualizarProfesores(parent);
        ventana.setVisible(true);
    }

    public static void RegistrarUser() {
        RegistrarUser(null);
    }

    public static void RegistrarUser(Window parent) {
        RegistarTab ventana = new RegistarTab(parent);
        ventana.setVisible(true);
    }
}
