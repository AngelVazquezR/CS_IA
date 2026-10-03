package com.angelvazquez.csia;

import java.awt.Window;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import com.angelvazquez.csia.config.AppConfig;
import com.angelvazquez.csia.config.StartupManager;
import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.DatabaseConnectionFactory;
import com.angelvazquez.csia.database.repository.UsuarioRepository;
import com.angelvazquez.csia.i18n.I18n;
import com.angelvazquez.csia.ui.ventanas.AsignarTab;
import com.angelvazquez.csia.ui.ventanas.LoginPage;
import com.angelvazquez.csia.ui.ventanas.PreferenciasPage;
import com.angelvazquez.csia.ui.ventanas.RegistarTab;
import com.angelvazquez.csia.ui.ventanas.RegistroInicialUsuario;
import com.angelvazquez.csia.ui.ventanas.VisualizarAlumnos;
import com.angelvazquez.csia.ui.ventanas.VisualizarAsignaciones;
import com.angelvazquez.csia.ui.ventanas.VisualizarProfesores;
import com.angelvazquez.csia.ui.ventanas.WelcomePage;

/**
 * Punto de entrada y acceso compartido a la configuración y a la apertura de ventanas.
 * Los métodos de navegación crean nuevas ventanas; el cierre u ocultación de la
 * ventana anterior corresponde al código que los invoca.
 */
public class Main {

    private static AppConfig appConfig;
    private static ConfigDB configuracion;

    /**
     * Inicializa configuración e idioma antes de consultar los usuarios y abrir el login.
     * Si se cancela la configuración o no se garantiza un primer usuario, termina
     * este método sin abrir LoginPage.
     *
     * @param args argumentos de lanzamiento, no utilizados
     */
    public static void main(String[] args) {
        appConfig = new StartupManager().inicializar();
        if (appConfig == null) return;

        configuracion = appConfig.getDatabase();
        if (!asegurarUsuarioInicial()) return;
        new LoginPage();
    }

    /**
     * Comprueba si USERS contiene alguna fila y, si está vacía, solicita el primer usuario.
     *
     * @return true si ya existe un usuario o se registra el primero; false si se cancela
     *         el registro o falla una operación SQL, cuyo error se muestra al usuario
     */
    private static boolean asegurarUsuarioInicial() {
        UsuarioRepository repository = new UsuarioRepository(
                new DatabaseConnectionFactory(), configuracion);
        try {
            if (repository.existeAlgunUsuario()) return true;
            return RegistroInicialUsuario.solicitar(configuracion);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    I18n.get("user.accessInitError", ex.getMessage()),
                    I18n.get("database.error.title"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Devuelve la instancia compartida de configuración de base de datos, sin copiarla.
     *
     * @return configuración establecida durante el arranque
     * @throws IllegalStateException si aún no se ha establecido
     */
    public static ConfigDB getConfiguracion() {
        if (configuracion == null) throw new IllegalStateException("Configuración no inicializada");
        return configuracion;
    }

    /**
     * Devuelve la instancia compartida de configuración global, sin copiarla.
     *
     * @return configuración recuperada durante el arranque
     * @throws IllegalStateException si aún no se ha establecido
     */
    public static AppConfig getAppConfig() {
        if (appConfig == null) throw new IllegalStateException("Configuración de aplicación no inicializada");
        return appConfig;
    }

    public static void LogIn() { new LoginPage(); }

    public static void Welcome() {
        WelcomePage ventana = new WelcomePage();
        ventana.setVisible(true);
    }

    // Las sobrecargas sin padre abren ventanas sin una ventana de retorno asociada.
    public static void Asignar() { Asignar(null); }
    public static void Asignar(Window parent) {
        AsignarTab ventana = new AsignarTab(parent);
        ventana.setVisible(true);
    }

    public static void AlumTabla() { AlumTabla(null); }
    public static void AlumTabla(Window parent) {
        VisualizarAlumnos ventana = new VisualizarAlumnos(parent);
        ventana.setVisible(true);
    }

    public static void AsignacionesTabla(Window parent) {
        new VisualizarAsignaciones(parent).setVisible(true);
    }

    public static void ProfeTabla() { ProfeTabla(null); }
    public static void ProfeTabla(Window parent) {
        VisualizarProfesores ventana = new VisualizarProfesores(parent);
        ventana.setVisible(true);
    }

    public static void RegistrarUser() { RegistrarUser(null); }
    public static void RegistrarUser(Window parent) {
        RegistarTab ventana = new RegistarTab(parent);
        ventana.setVisible(true);
    }

    public static void Preferencias(Window parent) {
        PreferenciasPage ventana = new PreferenciasPage(parent);
        ventana.setVisible(true);
    }
}
