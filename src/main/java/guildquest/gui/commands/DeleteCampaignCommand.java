package guildquest.gui.commands;

import guildquest.gui.GuiMain;

public class DeleteCampaignCommand implements GuiCommand {
    private final GuiMain ui;
    public DeleteCampaignCommand(GuiMain ui) { this.ui = ui; }

    @Override public String name() { return "Delete Campaign"; }
    @Override public void execute() { ui.handleDeleteCampaign(); }
}
