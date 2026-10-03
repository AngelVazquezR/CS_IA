package com.angelvazquez.csia.ui.ventanas;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
 * Centraliza el cierre de una ventana secundaria y el regreso a su ventana padre.
 * La X invoca volverAlPadre; los botones de regreso pueden usar el mismo método.
 */
public abstract class VentanaSecundaria extends JFrame {
    private static final long serialVersionUID = 1L;
    private final Window parent;

    /**
     * Asocia la ventana de retorno y configura el cierre mediante la X.
     * El constructor no oculta ni muestra la ventana padre.
     *
     * @param parent ventana que se mostrará al regresar; puede ser nula
     */
    protected VentanaSecundaria(Window parent) {
        this.parent = parent;
        // El listener gestiona el regreso y dispose, en lugar del cierre automático de JFrame.
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                volverAlPadre();
            }
        });
    }

    /**
     * Muestra y trae al frente la ventana padre, si existe, y libera esta ventana
     * mediante dispose. Si el padre es nulo, solo libera la ventana actual.
     * No llama a System.exit.
     */
    protected final void volverAlPadre() {
        if (parent != null) {
            parent.setVisible(true);
            parent.toFront();
        }
        dispose();
    }
}
