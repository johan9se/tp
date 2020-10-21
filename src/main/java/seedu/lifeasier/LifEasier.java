package seedu.lifeasier;

import seedu.lifeasier.commands.Command;
import seedu.lifeasier.parser.Parser;
import seedu.lifeasier.parser.ParserException;
import seedu.lifeasier.notes.NoteList;
import seedu.lifeasier.storage.FileStorage;
import seedu.lifeasier.tasks.TaskList;
import seedu.lifeasier.ui.ScheduleUi;
import seedu.lifeasier.ui.Ui;

import java.util.logging.LogManager;

/**
 * LifEasier is a CLI application that allows busy CEG students to schedule their day.
 * If you can type fast, LifEasier will get your scheduling done faster than traditional GUI calender apps.
 */
public class LifEasier {

    private Ui ui;
    private Parser parser;
    private TaskList tasks;
    private NoteList notes;
    private FileStorage storage;
    private ScheduleUi scheduleUi;

    public LifEasier(String fileNameTasks, String fileNameNotes) {
        ui = new Ui();
        parser = new Parser();
        tasks = new TaskList();
        notes = new NoteList();
        storage = new FileStorage(fileNameTasks, fileNameNotes, ui, notes, tasks);
        scheduleUi = new ScheduleUi();
    }

    /**
     * Runs the LifEasier program infinitely until termination by the user.
     */
    public void run(boolean showLogging) {

        if (!showLogging) {
            LogManager.getLogManager().reset();
        }

        storage.readSaveFiles();

        showStartupSequence();

        boolean isFinished = false;

        while (!isFinished) {

            String fullCommand = ui.readCommand();

            try {
                Command userCommand = parser.parseCommand(fullCommand, ui);
                userCommand.execute(ui, notes, tasks, storage, parser);
                isFinished = userCommand.isFinished();

            } catch (ParserException e) {
                ui.showParseUnknownCommandMessage();

            }

        }

        ui.showGoodbyeMessage();
    }

    public void showStartupSequence() {
        ui.showLogo();
        //scheduleUi.showHome(tasks);
        ui.showGreetingMessage();
    }


    /**
     * Main entry-point for the LifEasier application.
     */
    public static void main(String[] args) {
        new LifEasier("saveFileTasks.txt", "saveFileNotes.txt").run(true);
    }
}