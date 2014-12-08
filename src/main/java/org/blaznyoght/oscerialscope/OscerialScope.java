package org.blaznyoght.oscerialscope;

import java.util.Locale;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Window;


public class OscerialScope implements Application {
    private Window window = null;

    public static final String LANGUAGE_KEY = "language";

	public static void main(String[] args) {
		DesktopApplicationContext.main(OscerialScope.class, args);
	}

	@Override
	public void startup(Display display, Map<String, String> properties)
			throws Exception {
        String language = properties.get(LANGUAGE_KEY);
        Locale locale = (language == null) ? Locale.getDefault() : new Locale(language);
        Resources resources = new Resources(OscerialScope.class.getName(), locale);
        
		BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window)bxmlSerializer.readObject(getClass().getResource("OscerialScope.bxml"), resources);
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
