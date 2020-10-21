package seedu.lifeasier.storage;

import seedu.lifeasier.notes.Note;
import seedu.lifeasier.notes.NoteList;
import seedu.lifeasier.ui.Ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The NoteStorage class handles the reading and writing of the save file for notes.
 */
public class NoteStorage {

    public static final String BLANK_STRING = "";

    private static Logger logger = Logger.getLogger(NoteStorage.class.getName());
    private static final String SAVE_DELIMITER = "=-=";

    private NoteList notes;
    private String filePathNotes;
    private FileCommand fileCommand;
    private Ui ui;

    public NoteStorage(NoteList notes, String filePathNotes) {
        this.notes = notes;
        this.filePathNotes = filePathNotes;
        this.fileCommand = new FileCommand();
        this.ui = new Ui();
    }

    /**
     * Reads and loads all saved note information.
     */
    protected void readNotesSave() {
        logger.log(Level.INFO, "Read Notes save file start");

        try {
            File saveFile = new File(filePathNotes);

            assert saveFile.exists() : "Save file is supposed to exist";

            Scanner fileScanner = new Scanner(saveFile);
            createNoteList(fileScanner);

        } catch (IOException e) {
            ui.showFileReadError();
            logger.log(Level.SEVERE, "Encountered error reading Notes save file");
        }

        logger.log(Level.INFO, "Read Notes save file end");
    }

    protected void createNoteList(Scanner fileScanner) {
        logger.log(Level.INFO, "Rebuilding notes from save");

        try {
            while (fileScanner.hasNext()) {
                String noteInformation = fileScanner.nextLine();

                String[] noteComponents = noteInformation.split(SAVE_DELIMITER);
                String noteTitle = noteComponents[0];
                String noteDescription = noteComponents[1];

                if (noteTitle.equals(BLANK_STRING)) {
                    throw new ArrayIndexOutOfBoundsException();
                }

                notes.add(new Note(noteTitle, noteDescription));
                logger.log(Level.INFO, "New Note added: " + noteTitle);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            ui.showSaveDataMissingError();
            logger.log(Level.SEVERE, "Missing data from save file");
        }

        logger.log(Level.INFO, "Notes rebuilt");
    }

    /**
     * Writes information from the notes onto the notes save file for storage when there is a change to the notes.
     */
    public void writeToNoteSaveFile() {
        logger.log(Level.INFO, "Write to Notes save start");

        try {
            FileWriter fileWriter = new FileWriter(filePathNotes, true);

            fileCommand.clearSaveFile(filePathNotes);
            ArrayList<Note> noteList = notes.getNotes();

            //Append note information into save file for notes
            for (Note note : noteList) {
                String noteToSave = convertNoteToString(note);
                fileWriter.write(noteToSave);
                logger.log(Level.INFO, "New Note saved");
            }
            fileWriter.close();
        } catch (IOException e) {
            ui.showFileWriteError();
            logger.log(Level.SEVERE, "Unable to write to notes save file");
        }

        logger.log(Level.INFO, "Write to Notes save end");
    }

    protected String convertNoteToString(Note note) {
        return note.getTitle().trim() + SAVE_DELIMITER + note.getDescription().trim() + System.lineSeparator();
    }

}