package org.blaznyoght.oscerialscope.ui.pivot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.charts.ChartView.Category;
import org.apache.pivot.charts.LineChartView;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListView.SelectMode;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.Window;
import org.blaznyoght.oscerialscope.exception.FunctionalException;
import org.blaznyoght.oscerialscope.model.CaptureResult;
import org.blaznyoght.oscerialscope.service.SampleSourceFileGenerator;
import org.blaznyoght.oscerialscope.service.SerialCaptureService;
import org.blaznyoght.oscerialscope.service.exception.InvalidStateException;
import org.blaznyoght.oscerialscope.utils.PivotUtils;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class MainWindow extends Window implements Bindable {
	private static final Logger LOG = LogManager.getLogger(MainWindow.class);

	// Generate Sample Tab
	@BXML
	private PushButton chooseSourceFile = null;
	@BXML
	private Label labelSourceFile = null;
	private File sourceFile = null;

	@BXML
	private PushButton chooseTargetFile = null;
	@BXML
	private Label labelTargetFile = null;

	private File targetFile = null;

	@BXML
	private PushButton generateFile = null;

	@BXML
	private PushButton generateCapture = null;

	// Capture Tab
	@BXML
	private ListView serialInterfaceListView = null;

	@BXML
	private PushButton refreshSerialPortList = null;

	@BXML
	private PushButton startCapture = null;

	@BXML
	private PushButton stopCapture = null;

	// View Tab
	@BXML
	private BoxPane chartBoxPane;

	@BXML
	private PushButton viewButton;

	// Listen Tab

	// Play Tab

	// General
	@BXML
	private ListView captureList = null;

	@BXML
	private PushButton removeCapture = null;

	@BXML
	private Label captureStatusLabel = null;

	private SerialCaptureService serialCaptureService = new SerialCaptureService();

	@Override
	public void initialize(Map<String, Object> namespace, URL location,
			Resources resources) {
		initGenerateSampleTab();
		initCaptureTab();
		initViewTab();
		initListenTab();
		initPlayTab();
	}

	private void initPlayTab() {
		// TODO Auto-generated method stub

	}

	private void initListenTab() {
		// TODO Auto-generated method stub

	}

	private void initViewTab() {
		viewButton.getButtonPressListeners().add(new ButtonPressListener() {
			@Override
			public void buttonPressed(Button button) {
				CaptureResult selected = (CaptureResult) captureList
						.getSelectedItem();

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
				LineChartView chart = new LineChartView();
				chart.getCategories().add(new Category(serie.getDescription(), serie.getDescription()));
				List<XYSeriesCollection> chartData = new ArrayList<XYSeriesCollection>();
				chartData.add(dataSet);
				chart.setChartData(chartData);
				chartBoxPane.add(chart);
			}
		});
	}

	private void initCaptureTab() {
		refreshSerialPorts();
		refreshSerialPortList.getButtonPressListeners().add(
				new ButtonPressListener() {
					@Override
					public void buttonPressed(Button button) {
						refreshSerialPorts();
					}
				});
		startCapture.getButtonPressListeners().add(new ButtonPressListener() {
			@Override
			public void buttonPressed(Button button) {
				String port = (String) serialInterfaceListView
						.getSelectedItem();
				try {
					if (port == null) {
						throw new FunctionalException("No port selected");
					}
					serialCaptureService.startCapture(port);
					refreshCaptureIhm(true);
					final Task<Integer> refreshStatusTask = new Task<Integer>() {
						@Override
						public Integer execute() throws TaskExecutionException {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								throw new TaskExecutionException(e);
							}
							return serialCaptureService.getCaptureProgress();
						}

					};
					refreshStatusTask.execute(new TaskListener<Integer>() {
						@Override
						public void taskExecuted(Task<Integer> task) {
							String status = StringUtils.EMPTY;
							if (serialCaptureService.isRunning()) {
								status = String.format(
										"Capturing...(%d samples so far)",
										task.getResult());
								task.execute(this);
							}
							captureStatusLabel.setText(status);
						}

						@Override
						public void executeFailed(Task<Integer> task) {
							handleException(task.getFault());
						}
					});
				} catch (InvalidStateException | FunctionalException e) {
					handleException(e);
				}
			}
		});
		stopCapture.getButtonPressListeners().add(new ButtonPressListener() {
			@Override
			public void buttonPressed(Button button) {
				try {
					String message = serialCaptureService.stopCapture();
					refreshCaptureIhm(false);
					Alert.alert(MessageType.INFO, message, MainWindow.this);
				} catch (InvalidStateException e) {
					handleException(e);
				}
			}
		});
		removeCapture.getButtonPressListeners().add(new ButtonPressListener() {
			@Override
			public void buttonPressed(Button button) {
				try {
					CaptureResult capture = (CaptureResult) captureList
							.getSelectedItem();
					if (capture == null) {
						throw new FunctionalException("No capture selected");
					}
					serialCaptureService.removeCapture(capture);
					refreshCaptureList();

				} catch (FunctionalException e) {
					handleException(e);
				}
			}
		});
	}

	private void refreshSerialPorts() {
		serialInterfaceListView.clear();
		List<String> portList = PivotUtils
				.translateJavaList2PivotList(serialCaptureService
						.listSerialPorts());
		serialInterfaceListView.setListData(portList);
	}

	private void refreshCaptureIhm(boolean captureRunning) {
		if (captureRunning) {
			serialInterfaceListView.setEnabled(false);
			refreshSerialPortList.setEnabled(false);
			startCapture.setEnabled(false);
			stopCapture.setEnabled(true);
			removeCapture.setEnabled(false);
			captureList.setEnabled(false);
			captureList.setSelectMode(SelectMode.NONE);
		} else {
			serialInterfaceListView.setEnabled(true);
			refreshSerialPortList.setEnabled(true);
			startCapture.setEnabled(true);
			stopCapture.setEnabled(false);
			removeCapture.setEnabled(true);
			captureList.setEnabled(true);
			Object selection = captureList.getSelectedItem();
			refreshCaptureList();
			captureList.setSelectedItem(selection);
			captureList.setSelectMode(SelectMode.SINGLE);
		}
	}

	private void refreshCaptureList() {
		captureList.clear();
		captureList.setListData(PivotUtils
				.translateJavaList2PivotList(serialCaptureService
						.getCaptureResultList()));
		if (captureList.getListData().getLength() > 0) {
			captureList.setSelectMode(SelectMode.SINGLE);
		} else {
			captureList.setSelectMode(SelectMode.NONE);
		}
	}

	private void initGenerateSampleTab() {
		chooseSourceFile.getButtonPressListeners().add(
				new ButtonPressListener() {
					@Override
					public void buttonPressed(Button button) {
						final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet();
						fileBrowserSheet.setMode(FileBrowserSheet.Mode.OPEN);
						fileBrowserSheet.open(MainWindow.this,
								new SheetCloseListener() {
									@Override
									public void sheetClosed(Sheet sheet) {
										if (sheet.getResult()) {
											sourceFile = fileBrowserSheet
													.getSelectedFiles().get(0);
											labelSourceFile.setText(sourceFile
													.getName());
										}
									}
								});
					}
				});
		chooseTargetFile.getButtonPressListeners().add(
				new ButtonPressListener() {
					@Override
					public void buttonPressed(Button button) {
						final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet();
						fileBrowserSheet.setMode(FileBrowserSheet.Mode.SAVE_AS);
						fileBrowserSheet.open(MainWindow.this,
								new SheetCloseListener() {
									@Override
									public void sheetClosed(Sheet sheet) {
										if (sheet.getResult()) {
											targetFile = fileBrowserSheet
													.getSelectedFiles().get(0);
											labelTargetFile.setText(targetFile
													.getName());
										}
									}
								});
					}
				});
		generateFile.getButtonPressListeners().add(new ButtonPressListener() {
			@Override
			public void buttonPressed(Button button) {
				try {
					if (sourceFile == null) {
						throw new FunctionalException("source_file_missing");
					}
					if (targetFile == null) {
						throw new FunctionalException("target_file_missing");
					}
					SampleSourceFileGenerator generator = new SampleSourceFileGenerator(
							sourceFile, targetFile);
					boolean result = generator.generateFile();
					if (!result) {
						throw new FunctionalException("%generator_error");
					} else {
						Alert.alert(MessageType.INFO, "%done", MainWindow.this);
					}
				} catch (FunctionalException | IOException
						| UnsupportedAudioFileException e) {
					handleException(e);
					LOG.error("Generator error", e);
				}
			}
		});

		generateCapture.getButtonPressListeners().add(
				new ButtonPressListener() {
					@Override
					public void buttonPressed(Button button) {
						try {
							if (sourceFile == null) {
								throw new FunctionalException(
										"source_file_missing");
							}
							SampleSourceFileGenerator generator = new SampleSourceFileGenerator(
									sourceFile, targetFile);
							ByteArrayOutputStream baos = generator
									.generateBuffer();
							if (baos == null) {
								throw new FunctionalException(
										"source_file_missing");
							}
							serialCaptureService.addGeneratedCapture(baos);
							refreshCaptureList();
							Alert.alert(MessageType.INFO, "%done",
									MainWindow.this);
						} catch (FunctionalException | IOException
								| UnsupportedAudioFileException e) {
							handleException(e);
							LOG.error("%generator_error", e);
						}
					}
				});
	}

	private void handleException(Throwable e) {
		Alert.alert(MessageType.ERROR, e.getLocalizedMessage(), this);
	}
}
