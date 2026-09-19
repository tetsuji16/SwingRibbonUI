package io.github.tetsuji16.swingribbonui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

/**
 * A dependency-free, Office-style Swing ribbon. Host applications own all
 * commands, icons, localization and persistence; the component owns only view
 * state and physical tab/command routing.
 */
public final class SwingRibbon extends JPanel {
	private final List<RibbonTab> tabs;
	private final RibbonCommandSource commands;
	private final RibbonTheme theme;
	private final JPanel tabStrip = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
	private final JPanel commandSurface = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
	private final ButtonGroup tabGroup = new ButtonGroup();
	private final Map<String, JToggleButton> tabButtons = new LinkedHashMap<>();
	private final Set<String> contextualTabs = new LinkedHashSet<>();
	private RibbonDisplayMode displayMode = RibbonDisplayMode.ALWAYS_SHOW;
	private String selectedTabId;

	public SwingRibbon(List<RibbonTab> tabs, RibbonCommandSource commands) {
		this(tabs, commands, new DefaultRibbonTheme());
	}

	public SwingRibbon(List<RibbonTab> tabs, RibbonCommandSource commands, RibbonTheme theme) {
		super(new BorderLayout());
		this.tabs = List.copyOf(tabs);
		validateTabIds(this.tabs);
		this.commands = Objects.requireNonNull(commands);
		this.theme = Objects.requireNonNull(theme);
		setOpaque(true);
		setBackground(theme.chromeBackground());
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, theme.borderColor()));
		build();
	}

	public RibbonDisplayMode getDisplayMode() { return displayMode; }

	/** Changes chrome only; it never alters host model data or Undo history. */
	public void setDisplayMode(RibbonDisplayMode mode) {
		displayMode = Objects.requireNonNull(mode);
		boolean surfaceVisible = mode == RibbonDisplayMode.ALWAYS_SHOW;
		commandSurface.setVisible(surfaceVisible);
		tabStrip.setVisible(mode != RibbonDisplayMode.AUTO_HIDE);
		setVisible(mode != RibbonDisplayMode.AUTO_HIDE);
		revalidate();
		repaint();
	}

	/** Reveals an auto-hidden ribbon. The host chooses when to auto-hide again. */
	public void reveal() {
		if (displayMode != RibbonDisplayMode.AUTO_HIDE) return;
		setVisible(true);
		tabStrip.setVisible(true);
		commandSurface.setVisible(true);
		revalidate();
		repaint();
	}

	public String getSelectedTabId() { return selectedTabId; }

	public void selectTab(String tabId) {
		RibbonTab tab = findTab(tabId);
		if (tab.contextual() && !contextualTabs.contains(tab.id())) return;
		selectedTabId = tab.id();
		JToggleButton button = tabButtons.get(tab.id());
		if (button != null) button.setSelected(true);
		commandSurface.removeAll();
		for (RibbonBand band : tab.bands()) commandSurface.add(createBand(band));
		if (displayMode == RibbonDisplayMode.TABS_ONLY) commandSurface.setVisible(false);
		commandSurface.revalidate();
		commandSurface.repaint();
	}

	/** Makes exactly the supplied contextual tabs visible; ordinary tabs are unaffected. */
	public void setContextualTabs(Collection<String> tabIds) {
		contextualTabs.clear();
		if (tabIds != null) contextualTabs.addAll(tabIds);
		for (RibbonTab tab : tabs) {
			if (tab.contextual()) tabButtons.get(tab.id()).setVisible(contextualTabs.contains(tab.id()));
		}
		if (selectedTabId != null && findTab(selectedTabId).contextual() && !contextualTabs.contains(selectedTabId)) {
			firstVisibleTab().ifPresent(tab -> selectTab(tab.id()));
		}
		revalidate();
		repaint();
	}

	private void build() {
		tabStrip.setOpaque(true);
		tabStrip.setName("swingRibbon.tabStrip");
		tabStrip.setBackground(theme.chromeBackground());
		tabStrip.setPreferredSize(new Dimension(0, theme.tabHeight()));
		for (RibbonTab tab : tabs) tabStrip.add(createTabButton(tab));
		commandSurface.setOpaque(true);
		commandSurface.setName("swingRibbon.commandSurface");
		commandSurface.setBackground(theme.surfaceBackground());
		add(tabStrip, BorderLayout.NORTH);
		add(commandSurface, BorderLayout.CENTER);
		firstVisibleTab().ifPresent(tab -> selectTab(tab.id()));
	}

	private JToggleButton createTabButton(RibbonTab tab) {
		JToggleButton button = new JToggleButton(tab.title());
		button.setName("swingRibbon.tab." + tab.id());
		button.setVisible(!tab.contextual());
		button.setFocusable(false);
		button.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
		button.setContentAreaFilled(false);
		button.setBackground(theme.chromeBackground());
		button.setFont(theme.tabFont());
		button.setForeground(theme.unselectedTabColor());
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.addActionListener(event -> selectTab(tab.id()));
		button.getModel().addChangeListener(event -> {
			button.setForeground(button.isSelected() ? theme.selectedTabColor() : theme.unselectedTabColor());
			button.setContentAreaFilled(button.isSelected() || button.getModel().isRollover());
			button.setBackground(button.isSelected() ? new Color(0xe7f1fb) : new Color(0xf0f4f8));
		});
		button.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent event) { showTabPopup(event); }
			@Override public void mouseReleased(MouseEvent event) { showTabPopup(event); }
		});
		tabGroup.add(button);
		tabButtons.put(tab.id(), button);
		return button;
	}

	private JComponent createBand(RibbonBand band) {
		JPanel panel = new JPanel(new BorderLayout(0, 3));
		panel.setOpaque(false);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 0, 1, theme.borderColor()),
			BorderFactory.createEmptyBorder(5, 8, 3, 8)));
		JPanel commandsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
		commandsPanel.setOpaque(false);
		for (RibbonItem item : band.items()) commandsPanel.add(createCommandButton(item));
		javax.swing.JLabel title = new javax.swing.JLabel(band.title(), SwingConstants.CENTER);
		title.setFont(theme.commandFont().deriveFont(Font.PLAIN, Math.max(10f, theme.commandFont().getSize2D() - 1)));
		title.setForeground(new Color(0x5a6268));
		panel.add(commandsPanel, BorderLayout.CENTER);
		panel.add(title, BorderLayout.SOUTH);
		return panel;
	}

	private JComponent createCommandButton(RibbonItem item) {
		return switch (item.kind()) {
			case BUTTON -> configureCommandButton(new JButton(item.text(), item.icon()), item, true);
			case TOGGLE -> configureCommandButton(new JToggleButton(item.text(), item.icon()), item, true);
			case DROP_DOWN -> createDropDownButton(item);
			case SPLIT_BUTTON -> createSplitButton(item);
		};
	}

	private <T extends AbstractButton> T configureCommandButton(T button, RibbonItem item, boolean bindPrimaryAction) {
		Action action = commands.actionFor(item.id());
		if (bindPrimaryAction && action != null) button.setAction(action);
		button.setName("swingRibbon.command." + item.id());
		button.setText(item.text());
		button.setIcon(item.icon());
		button.setActionCommand(item.id());
		button.setFont(theme.commandFont());
		button.setFocusable(false);
		button.setOpaque(false);
		button.setContentAreaFilled(false);
		button.setBackground(theme.surfaceBackground());
		button.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
		button.setRolloverEnabled(true);
		button.setIconTextGap(item.size() == RibbonItem.Size.LARGE ? 5 : 6);
		button.setMargin(new Insets(3, 7, 3, 7));
		if (bindPrimaryAction && action == null) button.setEnabled(false);
		configureSize(button, item.size());
		installCommandInteractionStyle(button);
		button.getAccessibleContext().setAccessibleName(item.text());
		return button;
	}

	private void installCommandInteractionStyle(AbstractButton button) {
		button.getModel().addChangeListener(event -> {
			boolean selected = button.getModel().isSelected();
			boolean active = selected || button.getModel().isRollover() || button.getModel().isPressed();
			button.setContentAreaFilled(active);
			button.setOpaque(active);
			button.setBackground(selected ? new Color(0xdbeeff) : new Color(0xf0f6fc));
			button.setForeground(selected ? theme.selectedTabColor() : Color.DARK_GRAY);
		});
	}

	private void configureSize(AbstractButton button, RibbonItem.Size size) {
		switch (size) {
			case LARGE -> {
				button.setHorizontalTextPosition(SwingConstants.CENTER);
				button.setVerticalTextPosition(SwingConstants.BOTTOM);
				button.setPreferredSize(new Dimension(76, theme.commandHeight() - 8));
			}
			case MEDIUM -> button.setPreferredSize(new Dimension(104, 30));
			case SMALL -> button.setPreferredSize(new Dimension(82, 24));
		}
	}

	private JButton createDropDownButton(RibbonItem item) {
		JButton button = configureCommandButton(new JButton(item.text() + " \u25be", item.icon()), item, false);
		button.setEnabled(!item.menuItems().isEmpty() && item.menuItems().stream()
			.map(menuItem -> commands.actionFor(menuItem.id())).anyMatch(Objects::nonNull));
		button.addActionListener(event -> showCommandMenu(button, item));
		return button;
	}

	private JComponent createSplitButton(RibbonItem item) {
		JPanel split = new JPanel(new BorderLayout(0, 0));
		split.setName("swingRibbon.command." + item.id());
		split.setOpaque(false);
		JButton primary = configureCommandButton(new JButton(item.text(), item.icon()), item, true);
		JButton arrow = new JButton("\u25be");
		arrow.setName("swingRibbon.commandMenu." + item.id());
		arrow.setFocusable(false);
		arrow.setMargin(new Insets(2, 5, 2, 5));
		arrow.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		arrow.setContentAreaFilled(false);
		arrow.setRolloverEnabled(true);
		installCommandInteractionStyle(arrow);
		arrow.addActionListener(event -> showCommandMenu(arrow, item));
		split.add(primary, BorderLayout.CENTER);
		split.add(arrow, BorderLayout.EAST);
		split.setPreferredSize(new Dimension(primary.getPreferredSize().width + 21, primary.getPreferredSize().height));
		return split;
	}

	private void showCommandMenu(Component owner, RibbonItem item) {
		if (item.menuItems().isEmpty()) return;
		JPopupMenu popup = new JPopupMenu();
		for (RibbonMenuItem menuItem : item.menuItems()) {
			Action action = commands.actionFor(menuItem.id());
			JMenuItem entry = action == null ? new JMenuItem(menuItem.text(), menuItem.icon()) : new JMenuItem(action);
			entry.setName("swingRibbon.commandMenuItem." + menuItem.id());
			entry.setText(menuItem.text());
			entry.setIcon(menuItem.icon());
			if (action == null) entry.setEnabled(false);
			popup.add(entry);
		}
		popup.show(owner, 0, owner.getHeight());
	}

	private void showTabPopup(MouseEvent event) {
		if (!event.isPopupTrigger()) return;
		JPopupMenu popup = new JPopupMenu();
		JButton toggle = new JButton(displayMode == RibbonDisplayMode.TABS_ONLY ? "Show Ribbon" : "Collapse Ribbon");
		toggle.addActionListener(action -> setDisplayMode(displayMode == RibbonDisplayMode.TABS_ONLY
			? RibbonDisplayMode.ALWAYS_SHOW : RibbonDisplayMode.TABS_ONLY));
		popup.add(toggle);
		popup.show(event.getComponent(), event.getX(), event.getY());
	}

	private RibbonTab findTab(String id) {
		return tabs.stream().filter(tab -> tab.id().equals(id)).findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Unknown ribbon tab: " + id));
	}

	private java.util.Optional<RibbonTab> firstVisibleTab() {
		return tabs.stream().filter(tab -> !tab.contextual() || contextualTabs.contains(tab.id())).findFirst();
	}

	private static void validateTabIds(List<RibbonTab> tabs) {
		Set<String> ids = new LinkedHashSet<>();
		for (RibbonTab tab : tabs) {
			if (!ids.add(tab.id())) throw new IllegalArgumentException("Duplicate ribbon tab ID: " + tab.id());
		}
	}
}
