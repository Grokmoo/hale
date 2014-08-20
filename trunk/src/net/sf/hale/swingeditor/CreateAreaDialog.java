package net.sf.hale.swingeditor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A widget for creating a new area within a campaign
 * @author jared
 *
 */

public class CreateAreaDialog extends JDialog {
	private JButton ok, cancel;
	
	private JTextField name;
	private JSpinner width, height;
	
	public CreateAreaDialog(JFrame parent) {
		super(parent, "Create New Area", true);
		this.setResizable(false);
		
		JPanel content = new JPanel(new GridBagLayout());
		getContentPane().add(content);
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		
		c.gridx = 0;
		c.gridy = 0;
		content.add(new JLabel("Name"), c);
		
		c.gridx++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 3;
		name = new JTextField();
		name.getDocument().addDocumentListener(new NameChangedListener());
		content.add(name, c);
		
		c.gridy++;
		c.gridx = 0;
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		content.add(new JLabel("Size"), c);
		
		c.gridx++;
		width = new JSpinner(new SpinnerNumberModel(20, 5, 200, 1));
		content.add(width, c);
		
		c.gridx++;
		content.add(new JLabel(" x "), c);
		
		c.gridx++;
		height = new JSpinner(new SpinnerNumberModel(20, 5, 200, 1));
		content.add(height, c);
		
		c.gridx = 1;
		c.gridy++;
		ok = new JButton(new OKAction());
		ok.setEnabled(false);
		content.add(ok, c);
		
		c.gridx++;
		c.gridwidth = 2;
		cancel = new JButton(new CancelAction());
		content.add(cancel, c);
		
		pack();
	}
	
	private void checkValid() {
		boolean valid = true;
		
		if (name.getText().length() == 0) valid = false;
		
		// if name is alphanumeric or underscore
		if (!name.getText().matches("^[a-zA-Z0-9_-]*$")) valid = false;
		
		ok.setEnabled(valid);
	}
	
	private class NameChangedListener implements DocumentListener {
		@Override public void changedUpdate(DocumentEvent e) {
			checkValid();
		}

		@Override public void insertUpdate(DocumentEvent e) {
			checkValid();
		}

		@Override public void removeUpdate(DocumentEvent e) {
			checkValid();
		}
	}
	
	private class CancelAction extends AbstractAction {
		public CancelAction() {
			super("Cancel");
		}
		
		@Override public void actionPerformed(ActionEvent e) {
			setVisible(false);
			dispose();
		}
	}
	
	private class OKAction extends AbstractAction {
		public OKAction() {
			super("Create");
		}
		
		@Override public void actionPerformed(ActionEvent e) {
			
		}
	}
}
