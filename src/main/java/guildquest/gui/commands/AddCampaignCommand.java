package guildquest.gui.commands;

import guildquest.gui.GuiMain;

public class AddCampaignCommand implements GuiCommand {
    private final GuiMain ui;
    public AddCampaignCommand(GuiMain ui) { this.ui = ui; }

    @Override public String name() { return "Add Campaign"; }
    @Override public void execute() { ui.handleAddCampaign(); }
}
