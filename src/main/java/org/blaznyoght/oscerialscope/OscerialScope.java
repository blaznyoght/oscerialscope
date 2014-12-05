package org.blaznyoght.oscerialscope;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Window;


public class OscerialScope implements Application {
    private Window window = null;

	public static void main(String[] args) {
		new OscerialScope();
	}

	@Override
	public void startup(Display display, Map<String, String> properties)
			throws Exception {
		// TODO Auto-generated method stub
		
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
