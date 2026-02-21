package guildquest.gui.commands;

import guildquest.gui.GuiMain;

public class OpenSettingsCommand implements GuiCommand {
    private final GuiMain ui;
    public OpenSettingsCommand(GuiMain ui) { this.ui = ui; }

    @Override public String name() { return "Settings"; }
    @Override public void execute() { ui.handleOpenSettings(); }
}
