package io.github.tetsuji16.swingribbonui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Objects;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JTextField;

/**
 * Optional application chrome matching the title/quick-access/search row used
 * by modern desktop ribbons. Window management stays with the host frame.
 */
public final class RibbonApplicationHeader extends JPanel {
	private final JToggleButton autoSave = new JToggleButton("AutoSave");
	private final JTextField searchField = new JTextField();

	public RibbonApplicationHeader(String applicationTitle, List<Action> quickActions, Action searchAction,
		RibbonTheme theme) {
		this(applicationTitle, null, quickActions, searchAction, theme);
	}

	public RibbonApplicationHeader(String applicationTitle, Action autoSaveAction, List<Action> quickActions,
		Action searchAction, RibbonTheme theme) {
		super(new BorderLayout(8, 0));
		Objects.requireNonNull(applicationTitle);
		Objects.requireNonNull(quickActions);
		Objects.requireNonNull(theme);
		quickActions.forEach(Objects::requireNonNull);
		setName("swingRibbon.applicationHeader");
		setOpaque(true);
		setBackground(theme.chromeBackground());
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, theme.borderColor()),
			BorderFactory.createEmptyBorder(3, 8, 3, 8)));

		JPanel left = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 0));
		left.setOpaque(false);
		autoSave.setName("swingRibbon.autoSave");
		autoSave.setFocusable(false);
		autoSave.setContentAreaFilled(true);
		autoSave.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(0x82bda5)), BorderFactory.createEmptyBorder(2, 7, 2, 7)));
		autoSave.setBackground(new Color(0xe9f7f0));
		if (autoSaveAction != null) {
			autoSave.setAction(autoSaveAction);
			autoSave.setText("AutoSave");
		}
		left.add(autoSave);
		for (Action action : quickActions) left.add(quickAccessButton(action));
		add(left, BorderLayout.WEST);

		JLabel title = new JLabel(applicationTitle, JLabel.CENTER);
		title.setName("swingRibbon.applicationTitle");
		title.setFont(theme.tabFont());
		add(title, BorderLayout.CENTER);

		JPanel right = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));
		right.setOpaque(false);
		searchField.setName("swingRibbon.search");
		searchField.setToolTipText("Search");
		searchField.setPreferredSize(new Dimension(270, 26));
		if (searchAction != null) searchField.addActionListener(searchAction);
		right.add(searchField);
		add(right, BorderLayout.EAST);
	}

	public JToggleButton autoSaveControl() { return autoSave; }
	public JTextField searchField() { return searchField; }

	private static JButton quickAccessButton(Action action) {
		JButton button = new JButton(action);
		button.setName("swingRibbon.quickAccess." + actionName(action));
		button.setFocusable(false);
		button.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
		button.setContentAreaFilled(false);
		button.setText("");
		return button;
	}

	private static String actionName(Action action) {
		Object value = action.getValue(Action.NAME);
		return value == null ? "action" : value.toString().replaceAll("[^A-Za-z0-9_-]", "-");
	}
}
