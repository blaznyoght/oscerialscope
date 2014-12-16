package org.blaznyoght.oscerialscope;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Window;
import org.blaznyoght.oscerialscope.ui.pivot.MainWindow;


public class OscerialScopePivot implements Application {
    private Window window = null;

    public static final String LANGUAGE_KEY = "language";

	public static void main(String[] args) {
		DesktopApplicationContext.main(OscerialScopePivot.class, args);
	}

	@Override
	public void startup(Display display, Map<String, String> properties)
			throws Exception {
		BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window)bxmlSerializer.readObject(MainWindow.class, "MainWindow.bxml", true);
        window.open(display);
	}

	@Override
	public boolean shutdown(boolean optional) throws Exception {
        if (window != null) {
            window.close();
        }
		return false;
	}

	@Override
	public void suspend() throws Exception {
	}

	@Override
	public void resume() throws Exception {
	}

}
