package guildquest.gui.commands;

import guildquest.gui.GuiMain;

public class DeleteEventCommand implements GuiCommand {
    private final GuiMain ui;
    public DeleteEventCommand(GuiMain ui) { this.ui = ui; }

    @Override public String name() { return "Delete Event"; }
    @Override public void execute() { ui.handleDeleteEvent(); }
}
