package com.angelvazquez.csia;

import java.awt.Window;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import com.angelvazquez.csia.config.AppConfig;
import com.angelvazquez.csia.config.StartupManager;
import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;
import com.angelvazquez.csia.database.repository.UsuarioRepository;
import com.angelvazquez.csia.ui.ventanas.AsignarTab;
import com.angelvazquez.csia.ui.ventanas.LoginPage;
import com.angelvazquez.csia.ui.ventanas.RegistarTab;
import com.angelvazquez.csia.ui.ventanas.RegistroInicialUsuario;
import com.angelvazquez.csia.ui.ventanas.VisualizarAlumnos;
import com.angelvazquez.csia.ui.ventanas.VisualizarProfesores;
import com.angelvazquez.csia.ui.ventanas.WelcomePage;

public class Main {

    private static AppConfig appConfig;
    private static ConfigDB configuracion;

    public static void main(String[] args) {
        appConfig = new StartupManager().inicializar();
        if (appConfig == null) {
            return;
        }

        configuracion = appConfig.getDatabase();
        if (!asegurarUsuarioInicial()) {
            return;
        }

        new LoginPage();
    }

    private static boolean asegurarUsuarioInicial() {
        UsuarioRepository repository = new UsuarioRepository(
                new DatabaseConnectionFactory(), configuracion);
        try {
            if (repository.existeAlgunUsuario()) {
                return true;
            }
            return RegistroInicialUsuario.solicitar(configuracion);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "No se ha podido inicializar el acceso de usuarios.\n" + ex.getMessage(),
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public static ConfigDB getConfiguracion() {
        if (configuracion == null) {
            throw new IllegalStateException("Configuración no inicializada");
        }
        return configuracion;
    }

    public static AppConfig getAppConfig() {
        if (appConfig == null) {
            throw new IllegalStateException("Configuración de aplicación no inicializada");
        }
        return appConfig;
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
