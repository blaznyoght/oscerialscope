package org.blaznyoght.oscerialscope.ui.swing;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.FIRST_LINE_START;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.NONE;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.blaznyoght.oscerialscope.exception.FunctionalException;
import org.blaznyoght.oscerialscope.model.CaptureResult;
import org.blaznyoght.oscerialscope.service.GroovyInterpreterService;
import org.blaznyoght.oscerialscope.service.SampleSourceFileGenerator;
import org.blaznyoght.oscerialscope.service.SerialCaptureService;
import org.blaznyoght.oscerialscope.service.SoundPlayerService;
import org.blaznyoght.oscerialscope.service.handler.ExceptionHandler;
import org.blaznyoght.oscerialscope.ui.swing.utils.CaptureResultModel;
import org.blaznyoght.oscerialscope.ui.swing.utils.ConstraintsBuilder;
import org.blaznyoght.oscerialscope.ui.swing.utils.WavFileFilter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class MainWindow extends JFrame implements ExceptionHandler {
	
	private static final Logger LOG = LogManager.getLogger(MainWindow.class);

	private static final long serialVersionUID = -3896331928947551067L;

	private JTabbedPane tabbedPane = new JTabbedPane();

	// View/Listen Tab
	private JPanel viewListenPanel = new JPanel();

	private ChartPanel currentChartPanel;

	// Capture Tab
	private JPanel capturePanel = new JPanel();

	private JComboBox<String> portList;

	private JButton refreshButton;

	private JButton startCaptureButton;

	private JButton stopCaptureButton;

	// Play Tab
	private JPanel playPanel = new JPanel();

	// Generate Tab
	private JPanel generatePanel = new JPanel();

	private File sourceFile = null;

	private File targetFile = null;

	// Captures Panel
	private JPanel captureListPanel = new JPanel();

	private JComboBox<CaptureResult> captureList;

	private JButton removeCaptureButton;

	// Status bar
	private JPanel statusPanel = new JPanel();

	private JLabel statusLabel = new JLabel();

	// Service
	private SerialCaptureService serialCapture = new SerialCaptureService(this);

	private SoundPlayerService soundPlayer = new SoundPlayerService();

	private GroovyInterpreterService groovyInterpreter = new GroovyInterpreterService();

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
		setTitle("OscerialScope");
		
		// test
		try {
			InputStream is = getClass().getResourceAsStream("/test.wav");
			SampleSourceFileGenerator sampleSourceFileGenerator = 
					new SampleSourceFileGenerator(is, (File) null);
			ByteArrayOutputStream baos = sampleSourceFileGenerator.generateBuffer();
			serialCapture.addGeneratedCapture(baos);
		}
		catch(Exception ex) {
			LOG.error("Error generating capture", ex);
		}
		// test end
	}

	private void initCaptureListPanel() {
		captureList = new JComboBox<CaptureResult>(
				new CaptureResultModel(serialCapture));
		captureListPanel.setLayout(new GridBagLayout());
		captureListPanel.add(captureList);
		removeCaptureButton = new JButton("Remove");
		captureListPanel.add(removeCaptureButton);
		removeCaptureButton.addActionListener(new ActionListenerAdapter() {
			@Override
			public void process() throws Exception {
				CaptureResult selected = checkCaptureSelected();
				serialCapture.removeCapture(selected);
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
		playPanel.setLayout(new GridBagLayout());
		final JTextArea groovyEditor = new JTextArea();
		try {
			String defaultGroovy = IOUtils.toString(getClass().getResourceAsStream("/default.groovy"));
			groovyEditor.setText(defaultGroovy);
		} catch (IOException e) {
			handleException(e);
		}
		JButton launchButton = new JButton("Launch");
		launchButton.addActionListener(new ActionListenerAdapter() {
			@Override
			public void process() throws Exception {
				CaptureResult selected = checkCaptureSelected();
				String groovyScript = groovyEditor.getText();
				byte[] inBuf = selected.getBuffer().toByteArray();
				ByteArrayOutputStream baos = groovyInterpreter.process(
						groovyScript, inBuf);
				serialCapture.addGeneratedCapture(baos);
			}
		});
		ConstraintsBuilder builder = new ConstraintsBuilder();
		builder.fill(BOTH).weighty(1.0).weightx(1.0).gridx(0).gridy(0);
		playPanel.add(groovyEditor, builder.build());
		builder.fill(NONE).weighty(0.0).weightx(0.0).gridx(0).gridy(1);
		playPanel.add(launchButton, builder.build());
	}

	private void initViewListenTab() {
		tabbedPane.addTab("View/Listen", viewListenPanel);
		viewListenPanel.setLayout(new GridBagLayout());
		JButton viewButton = new JButton("View");
		viewButton.addActionListener(new ActionListenerAdapter() {
			@Override
			public void process() throws Exception {
				CaptureResult selected = checkCaptureSelected();
				viewListenPanel.remove(currentChartPanel);

				XYSeriesCollection dataSet = new XYSeriesCollection();
				XYSeries serie = new XYSeries(selected.toString());

				int globalCount = 0;
				ByteBuffer buffer = ByteBuffer.wrap(selected.getBuffer()
						.toByteArray());
				buffer.order(ByteOrder.LITTLE_ENDIAN);
				for (int i = 0; i < buffer.capacity(); i += 2) {
					serie.add(globalCount, buffer.getShort(i));
					globalCount++;
				}

				dataSet.addSeries(serie);
				JFreeChart chart = ChartFactory.createXYLineChart(
						"Waveform", null, null, dataSet);
				currentChartPanel = new ChartPanel(chart);
				ConstraintsBuilder builder = new ConstraintsBuilder();
				builder.insets(2);
				viewListenPanel.add(currentChartPanel, builder.fill(BOTH)
						.gridx(0).gridy(2).gridwidth(10).build());
			}
		});
		JButton playButton = new JButton("Play");
		playButton.addActionListener(new ActionListenerAdapter() {
			@Override
			public void process() throws Exception {
				CaptureResult selected = checkCaptureSelected();

				ByteArrayOutputStream baos = selected.getBuffer();
				soundPlayer.play(baos.toByteArray());
			}
		});
		ConstraintsBuilder builder = new ConstraintsBuilder();
		builder.insets(2);
		viewListenPanel.add(viewButton, builder.build());
		viewListenPanel.add(playButton, builder.build());
		JFreeChart chart = ChartFactory.createXYLineChart("Waveform", null,
				null, null);
		currentChartPanel = new ChartPanel(chart);
		viewListenPanel.add(currentChartPanel, builder.fill(BOTH).gridx(0)
				.gridy(2).gridwidth(10).build());

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
		startCaptureButton.addActionListener(new ActionListenerAdapter() {
			@Override
			public void process() throws Exception {
				String port = checkPortSelected();
				serialCapture.startCapture(port);
				refreshCaptureIhm(true);
			}
		});
		stopCaptureButton = new JButton("Stop capture");
		stopCaptureButton.addActionListener(new ActionListenerAdapter() {
			@Override
			public void process() throws Exception {
				String message = serialCapture.stopCapture();
				refreshCaptureIhm(false);
				setStatus(message);
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
		for (String port : serialCapture.listSerialPorts()) {
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
		Font boldFont = new Font(baseFont.getName(), Font.BOLD,
				baseFont.getSize());
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
				} else {
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
				} else {
					targetFile = null;
					targetFileName.setText(StringUtils.EMPTY);
				}
			}
		});
		JButton generateFileButton = new JButton("Generate file");
		generateFileButton.addActionListener(new ActionListenerAdapter() {
			@Override
			public void process() throws Exception {
				checkSourceFile();
				checkTargetFile();
				SampleSourceFileGenerator sampleSourceFileGenerator = new SampleSourceFileGenerator(
						sourceFile, targetFile);
				if (sampleSourceFileGenerator.generateFile()) {
					setStatus("File generated");
				}
			}
		});
		JButton generateCaptureButton = new JButton("Generate capture");
		generateCaptureButton.addActionListener(new ActionListenerAdapter() {
			@Override
			public void process() throws Exception {
				checkSourceFile();
				SampleSourceFileGenerator sampleSourceFileGenerator = new SampleSourceFileGenerator(
						sourceFile, targetFile);
				ByteArrayOutputStream baos = sampleSourceFileGenerator
						.generateBuffer();
				serialCapture.addGeneratedCapture(baos);
				setStatus("Capture generated");
			}
		});
		ConstraintsBuilder builder = new ConstraintsBuilder();
		builder.anchor(LINE_START).insets(2);
		generatePanel.add(sourceFileLabel, builder.gridwidth(2).build());
		builder.gridwidth(1).gridy(1);
		generatePanel.add(sourceFileName, builder.gridx(1).build());
		generatePanel.add(sourceFileChooseButton, builder.gridx(2).build());
		generatePanel.add(targetFileLabel,
				builder.gridwidth(2).gridx(1).gridy(2).build());
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
		LOG.error("Error", e);
	}

	private void setStatus(String text) {
		statusLabel.setText(text);
	}

	private abstract class ActionListenerAdapter implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				setStatus("Processing...");
				process();
				setStatus("Done.");
			} catch (Throwable ex) {
				handleException(ex);
				setStatus("Failed");
			}

		}

		public File checkSourceFile() throws FunctionalException {
			if (sourceFile == null) {
				throw new FunctionalException("no source file selected");
			}
			return sourceFile;
		}

		public File checkTargetFile() throws FunctionalException {
			if (targetFile == null) {
				throw new FunctionalException("no source file selected");
			}
			return targetFile;
		}

		public String checkPortSelected() throws FunctionalException {
			String selected = (String) portList.getSelectedItem();
			if (selected == null) {
				throw new FunctionalException("no port selected");
			}
			return selected;
		}

		public CaptureResult checkCaptureSelected() throws FunctionalException {
			CaptureResult selected = (CaptureResult) captureList.getSelectedItem();
			if (selected == null) {
				throw new FunctionalException("no capture selected");
			}
			return selected;
		}

		public abstract void process() throws Exception;
	}
}
