/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.sf.hale.swingeditor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A widget for selecting from the list of all available items
 * @author Jared
 *
 */

public class ItemSelector extends JPanel implements ListSelectionListener {
	public static final int ACTION_SELECTED = 0;
	
	private int currentListIndex = -1;
	
	private List<Action> callbacks;
	
	private JDialog parentDialog;
	
	private JScrollPane pane;
	private JList list;
	
	private JButton accept;
	private AcceptAction acceptAction;
	
	/**
	 * Creates a new ItemSelector using the model of all available items
	 */
	
	public ItemSelector() {
		super(new BorderLayout());
		
		list = new JList(EditorManager.getItemsModel());
		list.setVisibleRowCount(30);
		list.addListSelectionListener(this);
		
		pane = new JScrollPane(list);
		add(pane);
		
		callbacks = new ArrayList<Action>();
		acceptAction = new AcceptAction();
	}
	
	/**
	 * Adds an action whose {@link Action#actionPerformed(ActionEvent)} method
	 * is invoked whenever a selection is made in this ItemSelector.  In normal mode
	 * (the default), this is whenever the user clicks a list entry. In dialog mode, this
	 * is when the user accepts an item and closes the dialog
	 * @param action
	 */
	
	public void addSelectionAction(Action action) {
		callbacks.add(action);
	}
	
	/**
	 * Sets this item selector to run in dialog mode (with accept and cancel buttons)
	 * @param parent the parent dialog
	 */
	
	public void setDialogMode(JDialog parent) {
		this.parentDialog = parent;
		
		JPanel bottom = new JPanel();
		add(bottom, BorderLayout.SOUTH);
		
		accept = new JButton(acceptAction);
		accept.setEnabled(false);
		
		bottom.add(accept);
		bottom.add(new JButton(new CancelAction()));
	}
	
	private class AcceptAction extends AbstractAction {
		private AcceptAction() { super ("Accept"); }

		@Override public void actionPerformed(ActionEvent e) {
			parentDialog.setVisible(false);
			
			String itemID = EditorManager.getItemsModel().getElementAt(list.getSelectedIndex());
			
			ActionEvent event = new ActionEvent(this, ItemSelector.ACTION_SELECTED, itemID);
			
			for (Action action : callbacks) {
				action.actionPerformed(event);
			}
		}
	}
	
	private class CancelAction extends AbstractAction {
		private CancelAction() { super("Cancel"); }

		@Override public void actionPerformed(ActionEvent e) {
			parentDialog.setVisible(false);
		}
	}

	// the list selection listener
	@Override public void valueChanged(ListSelectionEvent event) {
		int newIndex = list.getSelectedIndex();
		
		if (newIndex == currentListIndex) return;
		
		currentListIndex = newIndex;
		
		if (currentListIndex < 0) return;
		
		if (parentDialog != null) {
			// if we are in dialog mode
			
			accept.setEnabled(true);
			
		} else {
			// if we are not in dialog mode, fire a selection event
			acceptAction.actionPerformed(null);
		}
	}
}
