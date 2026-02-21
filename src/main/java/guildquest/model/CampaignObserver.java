package guildquest.model;

/**
 * Observer for Campaign changes.
 *
 * Used to decouple UI refresh logic (e.g., JavaFX GuiMain) from the Campaign model.
 */
public interface CampaignObserver {
    /** Called when campaign-level metadata changes (name/archive/visibility/sharing). */
    void onCampaignChanged(Campaign campaign);

    /** Called when quest events are added/removed/updated within the campaign. */
    void onEventChanged(Campaign campaign, QuestEvent event);
}
