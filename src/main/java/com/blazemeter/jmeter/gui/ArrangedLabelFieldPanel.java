package com.blazemeter.jmeter.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ArrangedLabelFieldPanel extends JPanel
{
  private final GridBagConstraints labelC;
  private final GridBagConstraints fieldC;
  private final GridBagConstraints oneLineC;
  private int rowN = 0;

  public ArrangedLabelFieldPanel() {
    super(new GridBagLayout());
    this.labelC = new GridBagConstraints();
    this.labelC.anchor = 24;
    this.labelC.gridx = 0;
    this.labelC.insets = new Insets(5, 2, 5, 2);

    this.fieldC = new GridBagConstraints();
    this.fieldC.anchor = 23;
    this.fieldC.weightx = 1.0D;
    this.fieldC.fill = 2;
    this.fieldC.gridx = 1;
    this.fieldC.insets = new Insets(5, 2, 5, 2);

    this.oneLineC = new GridBagConstraints();
    this.oneLineC.gridx = 0;
    this.oneLineC.gridwidth = 2;
    this.oneLineC.fill = 2;
    this.oneLineC.weighty = 1.0D;
    this.oneLineC.insets = new Insets(5, 0, 5, 0);
  }

  public void add(Component label, Component field) {
    assert (label != null);
    assert (field != null);
    this.labelC.gridy = this.rowN;
    this.fieldC.gridy = this.rowN;
    add(label, this.labelC);
    add(field, this.fieldC);
    this.rowN += 1;
  }

  public Component add(String label, Component field) {
    add(new JLabel(label), field);
    return this;
  }

  public Component add(Component comp) {
    add(comp, this.oneLineC);
    this.rowN += 1;
    return this;
  }
}