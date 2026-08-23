package com.angelvazquez.csia.i18n;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Selector reutilizable de idioma para el primer arranque y preferencias. */
public final class IdiomaPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final JLabel etiquetaIdioma = new JLabel();
    private final JComboBox<Idioma> comboIdioma = new JComboBox<>(Idioma.values());

    public IdiomaPanel() {
        super(new GridBagLayout());
        comboIdioma.setSelectedItem(Idioma.desdeSistema());
        construirFormulario();
        actualizarTextos();
        comboIdioma.addActionListener(event -> {
            Idioma seleccionado = getIdiomaSeleccionado();
            if (seleccionado != null) {
                I18n.setIdioma(seleccionado);
                actualizarTextos();
            }
        });
    }

    private void construirFormulario() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 8, 8, 8);
        constraints.anchor = GridBagConstraints.WEST;

        constraints.gridx = 0;
        constraints.gridy = 0;
        add(etiquetaIdioma, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(comboIdioma, constraints);
    }

    public void actualizarTextos() {
        etiquetaIdioma.setText(I18n.get("language.select.label"));
    }

    public Idioma getIdiomaSeleccionado() {
        return (Idioma) comboIdioma.getSelectedItem();
    }

    public void setIdiomaSeleccionado(Idioma idioma) {
        comboIdioma.setSelectedItem(idioma);
    }

    public int getNumeroIdiomasDisponibles() {
        return comboIdioma.getItemCount();
    }
}
