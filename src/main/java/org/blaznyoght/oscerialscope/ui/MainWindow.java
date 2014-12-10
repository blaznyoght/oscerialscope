package org.blaznyoght.oscerialscope.ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Alert;
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
import org.blaznyoght.oscerialscope.model.CaptureResult;
import org.blaznyoght.oscerialscope.service.SampleSourceFileGenerator;
import org.blaznyoght.oscerialscope.service.SerialCaptureService;
import org.blaznyoght.oscerialscope.service.exception.InvalidStateException;
import org.blaznyoght.oscerialscope.ui.exception.FunctionalException;
import org.blaznyoght.oscerialscope.utils.PivotUtils;

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

	// Capture Tab
	@BXML
	private ListView serialInterfaceListView = null;

	@BXML
	private PushButton refreshSerialPortList = null;

	@BXML
	private PushButton startCapture = null;

	@BXML
	private PushButton stopCapture = null;

	@BXML
	private PushButton removeCapture = null;

	@BXML
	private Label captureStatusLabel = null;
	
	@BXML
	private ListView captureList = null;

	private SerialCaptureService serialCaptureService = new SerialCaptureService();

	@Override
	public void initialize(Map<String, Object> namespace, URL location,
			Resources resources) {
		initGenerateSampleTab();
		initCaptureTab();
		initViewListenTab();
	}

	private void initViewListenTab() {
		// TODO Auto-generated method stub
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
							}
							catch(InterruptedException e) {
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
								status = String.format("Capturing...(%d samples so far)", task.getResult());
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
					CaptureResult capture = (CaptureResult) captureList.getSelectedItem();
					if (capture == null) {
						throw new FunctionalException("No capture selected");
					}
					serialCaptureService.removeCapture(capture);
					refreshCaptureList();
					
				}
				catch(FunctionalException e) {
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
		}
		else {
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
		captureList.setListData(PivotUtils.translateJavaList2PivotList(serialCaptureService.getCaptureResultList()));
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
						throw new FunctionalException("generator_error");
					} else {
						Alert.alert(MessageType.INFO, "done", MainWindow.this);
					}
				} catch (FunctionalException | IOException
						| UnsupportedAudioFileException e) {
					handleException(e);
					LOG.error("Generator error", e);
				}
			}
		});
	}
	
	private void handleException(Throwable e) {
		Alert.alert(MessageType.ERROR, e.getLocalizedMessage(), this);
	}
}
