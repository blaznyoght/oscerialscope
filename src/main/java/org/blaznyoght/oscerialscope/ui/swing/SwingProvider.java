package org.blaznyoght.oscerialscope.ui.swing;

import org.blaznyoght.oscerialscope.ui.UIProvider;

public class SwingProvider implements UIProvider {

	public static void main(String[] args) {
		new SwingProvider().provideUI(args);
	}

	@Override
	public void provideUI(String[] args) {
		new MainWindow();
	}

	@Override
	public String getId() {
		return "Swing";
	}

}
