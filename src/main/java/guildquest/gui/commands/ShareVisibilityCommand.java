package guildquest.gui.commands;

import guildquest.gui.GuiMain;

public class ShareVisibilityCommand implements GuiCommand {
    private final GuiMain ui;
    public ShareVisibilityCommand(GuiMain ui) { this.ui = ui; }

    @Override public String name() { return "Share / Visibility"; }
    @Override public void execute() { ui.handleShareVisibility(); }
}
