package guildquest.model;

/**
 * Minimal user settings model.
 * (GUI may optionally expose these; the core requirement is that the app can store them.)
 */
public class UserSettings {
    private Realm currentRealm;
    private Theme theme;
    private TimeDisplayPreference timeDisplayPreference;

    public UserSettings(Realm currentRealm, Theme theme, TimeDisplayPreference timeDisplayPreference) {
        this.currentRealm = currentRealm;
        this.theme = (theme == null) ? Theme.CLASSIC : theme;
        this.timeDisplayPreference = (timeDisplayPreference == null) ? TimeDisplayPreference.BOTH : timeDisplayPreference;
    }

    public Realm getCurrentRealm() { return currentRealm; }
    public Theme getTheme() { return theme; }
    public TimeDisplayPreference getTimeDisplayPreference() { return timeDisplayPreference; }

    public void setCurrentRealm(Realm currentRealm) { this.currentRealm = currentRealm; }
    public void setTheme(Theme theme) { this.theme = theme; }
    public void setTimeDisplayPreference(TimeDisplayPreference pref) { this.timeDisplayPreference = pref; }
}
