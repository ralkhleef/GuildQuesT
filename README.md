# GuildQuest (Assignment 2) - Runnable Submission

## Run
Requires Java 17+.

```bash
java -jar GuildQuest.jar
```

This launches a **text-based** version of GuildQuest (no JavaFX required).

## Implemented requirements (>= 1/2)
- WorldClock time tracked in days/hours/min (GlobalTime)
- Users manage Campaigns (add/rename/archive/delete)
- Campaigns contain QuestEvents (title, start time, optional end time)
- Realms with a **fixed offset** from WorldClock time; events display World time and Realm-local time
- Timeline views implemented: All / Day / Week

## Notes
The repository still contains the earlier JavaFX UI file (`GuildQuestApp.java`), but the submitted JAR is built to run without JavaFX so it works on graders' machines.
