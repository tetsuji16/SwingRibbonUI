package io.github.tetsuji16.swingribbonui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.swing.AbstractAction;
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

	private static List<RibbonTab> definition() {
		RibbonBand band = new RibbonBand("document", "Document", List.of(new RibbonItem("save", "Save")));
		return List.of(new RibbonTab("file", "File", List.of(band)), new RibbonTab("task", "Task", List.of(band)),
			new RibbonTab("format", "Format", List.of(band), true));
	}
}
