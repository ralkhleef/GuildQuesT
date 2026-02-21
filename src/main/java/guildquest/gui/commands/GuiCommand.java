package guildquest.gui.commands;

/**
 * Command pattern (A3 AI-assisted pattern):
 * Encapsulates a UI action as an object so the GUI can
 * trigger behavior without hard-wiring logic into event handlers.
 */
public interface GuiCommand {
    String name();
    void execute();
}
