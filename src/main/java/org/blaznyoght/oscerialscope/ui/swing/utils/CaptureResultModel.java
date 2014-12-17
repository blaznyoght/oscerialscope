package org.blaznyoght.oscerialscope.ui.swing.utils;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.blaznyoght.oscerialscope.model.CaptureResult;
import org.blaznyoght.oscerialscope.service.SerialCaptureService;
import org.blaznyoght.oscerialscope.service.listener.CaptureResultListChangedListener;

public class CaptureResultModel implements ComboBoxModel<CaptureResult> {

	List<CaptureListListenerAdapter> listeners = new ArrayList<>();

	final SerialCaptureService serialCaptureService;

	private CaptureResult selectedItem;

	public CaptureResultModel(SerialCaptureService serialCaptureService) {
		this.serialCaptureService = serialCaptureService;
	}

	@Override
	public int getSize() {
		return serialCaptureService.getCaptureResultList().size();
	}

	@Override
	public CaptureResult getElementAt(int index) {
		return serialCaptureService.getCaptureResultList().get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		CaptureListListenerAdapter adapter = new CaptureListListenerAdapter(l);
		serialCaptureService.addCaptureResultListChangedListener(adapter);
		listeners.add(adapter);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		CaptureListListenerAdapter toRemove = null;
		for (CaptureListListenerAdapter adapter : listeners) {
			if (adapter.getOriginalListener() == l) {
				toRemove = adapter;
			}
		}
		if (toRemove != null) {
			serialCaptureService.removeCaptureResultListChangedListener(toRemove);
			listeners.remove(toRemove);
		}
	}

	@Override
	public void setSelectedItem(Object anItem) {
		selectedItem = (CaptureResult) anItem;
	}

	@Override
	public Object getSelectedItem() {
		return selectedItem;
	}

	private class CaptureListListenerAdapter implements
			CaptureResultListChangedListener {
		private final ListDataListener listener;

		CaptureListListenerAdapter(ListDataListener listener) {
			this.listener = listener;
		}

		@Override
		public void captureResultListChanged() {
			listener.contentsChanged(new ListDataEvent(this,
					ListDataEvent.CONTENTS_CHANGED, 0, serialCaptureService
							.getCaptureResultList().size()));
		}

		public ListDataListener getOriginalListener() {
			return listener;
		}
	}

}
