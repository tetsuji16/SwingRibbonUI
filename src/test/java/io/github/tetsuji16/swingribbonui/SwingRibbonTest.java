package io.github.tetsuji16.swingribbonui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

class SwingRibbonTest {
	@Test
	void displayModesPreserveSelectionAndCommands() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			SwingRibbon ribbon = new SwingRibbon(definition(), id -> new AbstractAction(id) {
				@Override public void actionPerformed(java.awt.event.ActionEvent event) { }
			});
			ribbon.selectTab("task");
			ribbon.setDisplayMode(RibbonDisplayMode.TABS_ONLY);
			assertEquals("task", ribbon.getSelectedTabId());
			assertFalse(ribbon.getComponent(1).isVisible());
			ribbon.setDisplayMode(RibbonDisplayMode.ALWAYS_SHOW);
			assertTrue(ribbon.getComponent(1).isVisible());
		});
	}

	@Test
	void contextualTabsAreHiddenUntilRequested() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			SwingRibbon ribbon = new SwingRibbon(definition(), id -> null);
			ribbon.setContextualTabs(List.of("format"));
			ribbon.selectTab("format");
			assertEquals("format", ribbon.getSelectedTabId());
			ribbon.setContextualTabs(List.of());
			assertEquals("file", ribbon.getSelectedTabId());
		});
	}

	@Test
	void rendersOfficeControlKindsAndKeepsThemWithinTheRibbonSurface() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			RibbonBand editing = new RibbonBand("editing", "Editing", List.of(
				new RibbonItem("paste", "Paste", null, RibbonItem.Size.LARGE),
				new RibbonItem("bold", "Bold", null, RibbonItem.Size.MEDIUM, RibbonControlKind.TOGGLE),
				new RibbonItem("find", "Find", null, RibbonItem.Size.SMALL, RibbonControlKind.DROP_DOWN,
					List.of(new RibbonMenuItem("findNext", "Find next"))),
				new RibbonItem("format", "Format", null, RibbonItem.Size.MEDIUM, RibbonControlKind.SPLIT_BUTTON,
					List.of(new RibbonMenuItem("formatPainter", "Format painter")))));
			SwingRibbon ribbon = new SwingRibbon(List.of(new RibbonTab("home", "Home", List.of(editing))), commands());
			ribbon.setSize(new Dimension(900, 120));
			layoutRecursively(ribbon);
			Component surface = findByName(ribbon, "swingRibbon.commandSurface");
			assertTrue(findByName(ribbon, "swingRibbon.command.paste") instanceof JButton);
			assertTrue(findByName(ribbon, "swingRibbon.command.bold") instanceof JToggleButton);
			assertTrue(findByName(ribbon, "swingRibbon.command.find") instanceof JButton);
			assertTrue(findByName(ribbon, "swingRibbon.command.format") instanceof Container);
			for (Component component : descendants(surface)) {
				if (component.getName() != null && component.getName().startsWith("swingRibbon.command")) {
					Rectangle bounds = SwingUtilities.convertRectangle(component.getParent(), component.getBounds(), surface);
					assertTrue(bounds.x >= 0 && bounds.y >= 0, component.getName());
					assertTrue(bounds.getMaxX() <= surface.getWidth() && bounds.getMaxY() <= surface.getHeight(), component.getName());
				}
			}
		});
	}

	@Test
	void applicationHeaderRoutesSearchAndExposesHostControls() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			int[] searches = { 0 };
			Action search = new AbstractAction("Search") {
				@Override public void actionPerformed(ActionEvent event) { searches[0]++; }
			};
			RibbonApplicationHeader header = new RibbonApplicationHeader("microProject", List.of(new AbstractAction("Save") {
				@Override public void actionPerformed(ActionEvent event) { }
			}), search, new DefaultRibbonTheme());
			header.searchField().postActionEvent();
			assertEquals(1, searches[0]);
			assertEquals("swingRibbon.autoSave", header.autoSaveControl().getName());
			assertTrue(findByName(header, "swingRibbon.quickAccess.Save") instanceof JButton);
		});
	}

	@Test
	void adoptsHostActionEnabledAndToggleState() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			Action disabled = new AbstractAction("Disabled") {
				@Override public void actionPerformed(ActionEvent event) { }
			};
			disabled.setEnabled(false);
			Action selected = new AbstractAction("Selected") {
				@Override public void actionPerformed(ActionEvent event) { }
			};
			selected.putValue(Action.SELECTED_KEY, true);
			RibbonBand band = new RibbonBand("commands", "Commands", List.of(
				new RibbonItem("disabled", "Disabled", null, RibbonItem.Size.MEDIUM),
				new RibbonItem("selected", "Selected", null, RibbonItem.Size.MEDIUM, RibbonControlKind.TOGGLE)));
			SwingRibbon ribbon = new SwingRibbon(List.of(new RibbonTab("home", "Home", List.of(band))),
				id -> id.equals("disabled") ? disabled : selected);
			assertFalse(findByName(ribbon, "swingRibbon.command.disabled").isEnabled());
			assertTrue(((JToggleButton) findByName(ribbon, "swingRibbon.command.selected")).isSelected());
			disabled.setEnabled(true);
			selected.putValue(Action.SELECTED_KEY, false);
			assertTrue(findByName(ribbon, "swingRibbon.command.disabled").isEnabled());
			assertFalse(((JToggleButton) findByName(ribbon, "swingRibbon.command.selected")).isSelected());
		});
	}

	@Test
	void rejectsDuplicateTabIdsAndNullQuickActions() {
		RibbonBand band = new RibbonBand("document", "Document", List.of(new RibbonItem("save", "Save")));
		assertThrows(IllegalArgumentException.class, () -> new SwingRibbon(List.of(
			new RibbonTab("file", "File", List.of(band)), new RibbonTab("file", "Duplicate", List.of(band))), commands()));
		assertThrows(NullPointerException.class, () -> new RibbonApplicationHeader("microProject", List.of((Action) null), null,
			new DefaultRibbonTheme()));
	}

	private static RibbonCommandSource commands() {
		return id -> new AbstractAction(id) {
			@Override public void actionPerformed(ActionEvent event) { }
		};
	}

	private static Component findByName(Component root, String name) {
		if (name.equals(root.getName())) return root;
		for (Component component : descendants(root)) if (name.equals(component.getName())) return component;
		throw new AssertionError("Missing component: " + name);
	}

	private static List<Component> descendants(Component root) {
		List<Component> found = new ArrayList<>();
		if (root instanceof Container container) {
			for (Component child : container.getComponents()) {
				found.add(child);
				found.addAll(descendants(child));
			}
		}
		return found;
	}

	private static void layoutRecursively(Component component) {
		component.doLayout();
		if (component instanceof Container container) {
			for (Component child : container.getComponents()) layoutRecursively(child);
		}
	}

	private static List<RibbonTab> definition() {
		RibbonBand band = new RibbonBand("document", "Document", List.of(new RibbonItem("save", "Save")));
		return List.of(new RibbonTab("file", "File", List.of(band)), new RibbonTab("task", "Task", List.of(band)),
			new RibbonTab("format", "Format", List.of(band), true));
	}
}
