package org.blaznyoght.oscerialscope.ui.swing;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.FIRST_LINE_START;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import org.apache.commons.lang3.StringUtils;
import org.blaznyoght.oscerialscope.exception.FunctionalException;
import org.blaznyoght.oscerialscope.model.CaptureResult;
import org.blaznyoght.oscerialscope.service.ExceptionHandler;
import org.blaznyoght.oscerialscope.service.SampleSourceFileGenerator;
import org.blaznyoght.oscerialscope.service.SerialCaptureService;
import org.blaznyoght.oscerialscope.service.exception.InvalidStateException;
import org.blaznyoght.oscerialscope.ui.swing.utils.CaptureResultModel;
import org.blaznyoght.oscerialscope.ui.swing.utils.ConstraintsBuilder;
import org.blaznyoght.oscerialscope.ui.swing.utils.WavFileFilter;

public class MainWindow extends JFrame implements ExceptionHandler {
	private static final long serialVersionUID = -3896331928947551067L;

	private JTabbedPane tabbedPane = new JTabbedPane();

	// View/Listen Tab
	private JPanel viewListenPanel = new JPanel();

	// Capture Tab
	private JPanel capturePanel = new JPanel();

	// Play Tab
	private JPanel playPanel = new JPanel();

	// Generate Tab
	private JPanel generatePanel = new JPanel();
	
	private File sourceFile = null;
	
	private File targetFile = null;

	// Captures Panel
	private JPanel captureListPanel = new JPanel();

	private JComboBox<CaptureResult> captureList;

	// Status bar
	private JPanel statusPanel = new JPanel();

	private JLabel statusLabel = new JLabel();

	// Service
	private SerialCaptureService serialCaptureService = new SerialCaptureService(this);

	private JButton refreshButton;

	private JButton startCaptureButton;

	private JButton stopCaptureButton;

	private JComboBox<String> portList;

	private JButton removeCaptureButton;
	
	public MainWindow() {
		init();
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	private void init() {
		initGenerateTab();
		initCaptureTab();
		initViewListenTab();
		initPlayTab();
		initCaptureListPanel();
		initStatusBar();
		setLayout(new GridBagLayout());
		ConstraintsBuilder builder = new ConstraintsBuilder();
		builder.weightx(1.0);
		builder.anchor(FIRST_LINE_START);
		add(tabbedPane, builder.fill(BOTH).weighty(1.0).build());
		builder.fill(HORIZONTAL);
		builder.weighty(0.0);
		add(captureListPanel, builder.gridy(1).build());
		add(statusPanel, builder.gridy(2).build());
		Dimension d = new Dimension(800, 600);
		setPreferredSize(d);
		setSize(d);
	}

	private void initCaptureListPanel() {
		captureList = new JComboBox<CaptureResult>(new CaptureResultModel(serialCaptureService));
		captureListPanel.setLayout(new GridBagLayout());
		captureListPanel.add(captureList);
		removeCaptureButton = new JButton("Remove");
		captureListPanel.add(removeCaptureButton);
		removeCaptureButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					CaptureResult selected = (CaptureResult) captureList.getSelectedItem();
					if (selected == null) {
						throw new FunctionalException("no capture selected");
					}
					serialCaptureService.removeCapture(selected);
				} catch (FunctionalException ex) {
					handleException(ex);
				}
			}
		});
		captureListPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
	}

	private void initStatusBar() {
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusPanel.setPreferredSize(new Dimension(getWidth(), 18));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		setStatus("Ready.");
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(statusLabel);
	}

	private void initPlayTab() {
		tabbedPane.addTab("Play", playPanel);
	}

	private void initViewListenTab() {
		tabbedPane.addTab("View/Listen", viewListenPanel);
	}

	private void initCaptureTab() {
		tabbedPane.addTab("Capture", capturePanel);
		capturePanel.setLayout(new GridBagLayout());
		portList = new JComboBox<>();
		refreshButton = new JButton("Refresh list");
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshPortList(portList);
			}
		});
		startCaptureButton = new JButton("Start capture");
		startCaptureButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String port = (String) portList.getSelectedItem();
				try {
					if (port == null) {
						throw new FunctionalException("No port selected");
					}
					serialCaptureService.startCapture(port);
					refreshCaptureIhm(true);
				} catch (InvalidStateException | FunctionalException ex) {
					handleException(ex);
				}
			}
		});
		stopCaptureButton = new JButton("Stop capture");
		stopCaptureButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String message = serialCaptureService.stopCapture();
					refreshCaptureIhm(false);
					setStatus(message);
				} catch (InvalidStateException ex) {
					handleException(ex);
				}
			}
		});
		ConstraintsBuilder builder = new ConstraintsBuilder();
		builder.insets(2);
		capturePanel.add(portList, builder.gridx(1).gridy(1).build());
		capturePanel.add(refreshButton, builder.gridx(2).gridy(1).build());
		capturePanel.add(startCaptureButton, builder.gridx(1).gridy(2).build());
		capturePanel.add(stopCaptureButton, builder.gridx(2).gridy(2).build());
		refreshPortList(portList);
	}
	
	private void refreshPortList(JComboBox<String> portList) {
		String currentSelection = (String) portList.getSelectedItem();
		portList.removeAllItems();
		int i = 0;
		for(String port : serialCaptureService.listSerialPorts()) {
			portList.insertItemAt(port, i++);			
		}
		portList.setSelectedItem(currentSelection);
	}

	private void refreshCaptureIhm(boolean captureRunning) {
		if (captureRunning) {
			captureList.setEnabled(false);
			refreshButton.setEnabled(false);
			startCaptureButton.setEnabled(false);
			stopCaptureButton.setEnabled(true);
			removeCaptureButton.setEnabled(false);
			captureList.setEnabled(false);
		} else {
			captureList.setEnabled(true);
			refreshButton.setEnabled(true);
			startCaptureButton.setEnabled(true);
			stopCaptureButton.setEnabled(false);
			removeCaptureButton.setEnabled(true);
			captureList.setEnabled(true);
			Object selection = captureList.getSelectedItem();
			captureList.setSelectedItem(selection);
		}
	}

	private void initGenerateTab() {
		tabbedPane.addTab("Generate", generatePanel);
		generatePanel.setLayout(new GridBagLayout());
		JLabel sourceFileLabel = new JLabel("Source file");
		Font baseFont = sourceFileLabel.getFont();
		Font boldFont = new Font(baseFont.getName(), Font.BOLD, baseFont.getSize());
		sourceFileLabel.setFont(boldFont);
		final JLabel sourceFileName = new JLabel("None");
		JButton sourceFileChooseButton = new JButton("Choose sourceFile");
		sourceFileChooseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
				fileChooser.setFileFilter(new WavFileFilter());
				int result = fileChooser.showOpenDialog(MainWindow.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					sourceFile = fileChooser.getSelectedFile();
					sourceFileName.setText(sourceFile.getAbsolutePath());
				}
				else {
					sourceFile = null;
					sourceFileName.setText(StringUtils.EMPTY);
				}
			}
		});
		JLabel targetFileLabel = new JLabel("Target file");
		targetFileLabel.setFont(boldFont);
		final JLabel targetFileName = new JLabel("None");
		JButton targetFileChooseButton = new JButton("Choose targetFile");
		targetFileChooseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
				fileChooser.setVisible(true);
				int result = fileChooser.showOpenDialog(MainWindow.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					targetFile = fileChooser.getSelectedFile();
					targetFileName.setText(targetFile.getAbsolutePath());
				}
				else {
					targetFile = null;
					targetFileName.setText(StringUtils.EMPTY);
				}
			}
		});
		JButton generateFileButton = new JButton("Generate file");
		generateFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (sourceFile == null) {
						throw new FunctionalException("no source file selected");
					}
					if (targetFile == null) {
						throw new FunctionalException("no source file selected");
					}
					SampleSourceFileGenerator sampleSourceFileGenerator = 
							new SampleSourceFileGenerator(sourceFile, targetFile); 
					if (sampleSourceFileGenerator.generateFile()) {
						setStatus("File generated");
					}
				}
				catch(FunctionalException | IOException | UnsupportedAudioFileException ex) {
					handleException(ex);
				}
			}
		});
		JButton generateCaptureButton = new JButton("Generate capture");
		generateCaptureButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (sourceFile == null) {
						throw new FunctionalException("no source file selected");
					}
					SampleSourceFileGenerator sampleSourceFileGenerator = 
							new SampleSourceFileGenerator(sourceFile, targetFile); 
					ByteArrayOutputStream baos = sampleSourceFileGenerator.generateBuffer();
					serialCaptureService.addGeneratedCapture(baos);
					setStatus("Capture generated");
				}
				catch(FunctionalException | IOException | UnsupportedAudioFileException ex) {
					handleException(ex);
				}
				
			}
		});
		ConstraintsBuilder builder = new ConstraintsBuilder();
		builder.anchor(LINE_START).insets(2);
		generatePanel.add(sourceFileLabel, builder.gridwidth(2).build());
		builder.gridwidth(1).gridy(1);
		generatePanel.add(sourceFileName, builder.gridx(1).build());
		generatePanel.add(sourceFileChooseButton, builder.gridx(2).build());
		generatePanel.add(targetFileLabel, builder.gridwidth(2).gridx(1).gridy(2).build());
		builder.gridy(3).gridwidth(1);
		generatePanel.add(targetFileName, builder.gridx(1).build());
		generatePanel.add(targetFileChooseButton, builder.gridx(2).build());
		builder.gridy(4);
		generatePanel.add(generateFileButton, builder.gridx(1).build());
		generatePanel.add(generateCaptureButton, builder.gridx(2).build());
	}

	public void handleException(Throwable e) {
		JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), "Error",
				JOptionPane.ERROR_MESSAGE);
	}
	
	private void setStatus(String text) {
		statusLabel.setText(text);
	}
}
