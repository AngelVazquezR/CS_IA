package com.angelvazquez.csia.ui.ventanas;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

public abstract class VentanaSecundaria extends JFrame {
    private static final long serialVersionUID = 1L;
    private final Window parent;

    protected VentanaSecundaria(Window parent) {
        this.parent = parent;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                volverAlPadre();
            }
        });
    }

    protected final void volverAlPadre() {
        if (parent != null) {
            parent.setVisible(true);
            parent.toFront();
        }
        dispose();
    }
}
