package org.blaznyoght.oscerialscope;

import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.blaznyoght.oscerialscope.panel.Startup;

public class OscerialScope {
	
	private ResourceBundle messages;
	
	private JMenuBar menu;
	
	private JFrame frame; 
	
	public OscerialScope() {
		initMessages();
		initFrame();
		initMenu();
		frame.setJMenuBar(menu);
        frame.pack();
        frame.setVisible(true);
	}
	
	private void initMessages() {
		messages = PropertyResourceBundle.getBundle("i18n/messages");
	}

	private void initMenu() {
		menu = new JMenuBar();
		
		// Menu operation
		JMenu menuOperation = new JMenu(m("text.menu.operation"));
		JMenuItem generate = new JMenuItem(m("text.menu.operation.generate_buffer"));
		
		JMenuItem capture = new JMenuItem(m("text.menu.operation.capture_output"));
		
		JMenuItem quit = new JMenuItem(m("text.menu.operation.quit"));
		quit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		menuOperation.add(generate);
		menuOperation.add(capture);
		menuOperation.add(quit);
		
		
		// Menu help
		JMenu menuHelp = new JMenu(m("text.menu.help"));
		JMenuItem manual = new JMenuItem(m("text.menu.help.manual"));
		
		JMenuItem about = new JMenuItem(m("text.menu.help.about"));
		
		menuHelp.add(manual);
		menuHelp.add(about);
		
		menu.add(menuOperation);
		menu.add(menuHelp);
	}

	private void initFrame() {
		frame = new JFrame(m("text.title"));
        frame.setFont(new Font("Helvetica.Italic", Font.PLAIN, 12));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(getFDImage());
        frame.getContentPane().add(new Startup());
        frame.setLocation(200, 200);
	}
	
	private String m(String key) {
		return messages.getString(key);
	}

    // Returns an Image or null.
    protected static Image getFDImage() {
        final java.net.URL imgURL = OscerialScope.class.getResource("/images/images.gif");
        if (imgURL != null) {
            return new ImageIcon(imgURL).getImage();
        } else {
            return null;
        }
    }

	public static void main(String[] args) {
		new OscerialScope();
	}

}
