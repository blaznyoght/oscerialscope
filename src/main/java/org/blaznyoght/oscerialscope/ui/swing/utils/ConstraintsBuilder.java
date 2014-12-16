package org.blaznyoght.oscerialscope.ui.swing.utils;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import org.apache.commons.lang3.SerializationUtils;

public class ConstraintsBuilder {
	private GridBagConstraints constraints;
	
	public ConstraintsBuilder() {
		constraints = new GridBagConstraints();
	}
	
	public ConstraintsBuilder anchor(int anchor) {
		constraints.anchor = anchor;
		return this;
	}
	
	public ConstraintsBuilder fill(int fill) {
		constraints.fill = fill;
		return this;
	}
	
	public ConstraintsBuilder gridheight(int gridheight) {
		constraints.gridheight = gridheight;
		return this;
	}
	
	public ConstraintsBuilder gridwidth(int gridwidth) {
		constraints.gridwidth = gridwidth;
		return this;
	}
	
	public ConstraintsBuilder gridx(int gridx) {
		constraints.gridx = gridx;
		return this;
	}
	
	public ConstraintsBuilder gridy(int gridy) {
		constraints.gridy = gridy;
		return this;
	}
	
	public ConstraintsBuilder insets(Insets insets) {
		constraints.insets = insets;
		return this;
	}
	
	public ConstraintsBuilder insets(int top, int left, int bottom, int right) {
		constraints.insets = new Insets(top, left, bottom, right);
		return this;
	}
	
	public ConstraintsBuilder insets(int top, int left) {
		constraints.insets = new Insets(top, left, top, left);
		return this;
	}
	
	public ConstraintsBuilder insets(int top) {
		constraints.insets = new Insets(top, top, top, top);
		return this;
	}
	
	public ConstraintsBuilder ipadx(int ipadx) {
		constraints.ipadx = ipadx;
		return this;
	}
	
	public ConstraintsBuilder ipady(int ipady) {
		constraints.ipady = ipady;
		return this;
	}
	
	public ConstraintsBuilder weightx(double weightx) {
		constraints.weightx = weightx;
		return this;
	}
	
	public ConstraintsBuilder weighty(double weighty) {
		constraints.weighty = weighty;
		return this;
	}
	
	public GridBagConstraints build() {
		return SerializationUtils.clone(constraints);
	}
}
