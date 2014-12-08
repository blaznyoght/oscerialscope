package org.blaznyoght.oscerialscope.ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.Window;
import org.blaznyoght.oscerialscope.service.SampleSourceFileGenerator;
import org.blaznyoght.oscerialscope.ui.exception.FunctionalException;

public class MainWindow extends Window implements Bindable {
	private static final Logger LOG = LogManager.getLogger(MainWindow.class);
	
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

	@Override
	public void initialize(Map<String, Object> namespace, URL location,
			Resources resources) {
		initGenerateSampleTab();
	}

	private void initGenerateSampleTab() {
		chooseSourceFile.getButtonPressListeners().add(new ButtonPressListener() {
			@Override
			public void buttonPressed(Button button) {
				final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet();
				fileBrowserSheet.setMode(FileBrowserSheet.Mode.OPEN);
				fileBrowserSheet.open(MainWindow.this, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            sourceFile = fileBrowserSheet.getSelectedFiles().get(0);
                            labelSourceFile.setText(sourceFile.getName());
                        }
                    }
                });
			}
		});
		chooseTargetFile.getButtonPressListeners().add(new ButtonPressListener() {
			@Override
			public void buttonPressed(Button button) {
				final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet();
				fileBrowserSheet.setMode(FileBrowserSheet.Mode.SAVE_AS);
				fileBrowserSheet.open(MainWindow.this, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            targetFile = fileBrowserSheet.getSelectedFiles().get(0);
                            labelTargetFile.setText(targetFile.getName());
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
					SampleSourceFileGenerator generator = new SampleSourceFileGenerator(sourceFile, targetFile);
					boolean result = generator.generateFile();
					if (!result) {
						throw new FunctionalException("generator_error");
					}
				}
				catch(FunctionalException | IOException | UnsupportedAudioFileException e) {
					LOG.error("Generator error", e);
					Alert.alert(MessageType.ERROR, e.getLocalizedMessage(), MainWindow.this);
				}
			}
		});
	}
}
