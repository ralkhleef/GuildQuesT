package guildquest.gui.commands;

import guildquest.gui.GuiMain;

public class AddEventCommand implements GuiCommand {
    private final GuiMain ui;
    public AddEventCommand(GuiMain ui) { this.ui = ui; }

    @Override public String name() { return "Add Event"; }
    @Override public void execute() { ui.handleAddEvent(); }
}
