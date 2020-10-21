package seedu.lifeasier.ui;

import org.fusesource.jansi.AnsiConsole;
import seedu.lifeasier.tasks.Deadline;
import seedu.lifeasier.tasks.Task;
import seedu.lifeasier.tasks.TaskList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * The TimetableUi class handles the displaying of the schedule in a tabulated format.
 */
public class TimetableUi {
    private static final String TIME_COLUMN_NAME = "TIME";
    private static final String TIME_FORMAT = "%02d:00";
    private static final int DEFAULT_START_HOUR = 8;
    private static final int DEFAULT_END_HOUR = 18;

    private static final int DAYS_COLUMN_COUNT = 7;
    private static final int MAX_COLUMN_WIDTH = 15;

    private static final String ROW_FORMAT = "|%-11s| %-15s | %-15s | %-15s | %-15s | %-15s | %-15s | %-15s |";
    private static final String ROW_SEPARATOR = "+-----------+" + "-----------------+".repeat(DAYS_COLUMN_COUNT);

    private static TimetableUi timetableUi = null;
    private static ArrayList<String> timetableRows;

    private TimetableUi() {
        AnsiConsole.systemInstall();
        timetableRows = new ArrayList<>();
    }

    public static TimetableUi getInstance() {
        if (timetableUi == null) {
            timetableUi = new TimetableUi();
        }
        return timetableUi;
    }

    public void showTimetable(TaskList tasks) {
        System.out.println(ansi().eraseScreen().fg(RED).a("Hello").fg(GREEN).a(" World").reset());
        generateTimetable(tasks);
        System.out.println(ROW_SEPARATOR);
        for (String row: timetableRows) {
            System.out.println(row);
            System.out.println(ROW_SEPARATOR);
        }
    }

    /**
     * Fills the timetable with the contents of the TaskList.
     */
    public void generateTimetable(TaskList tasks) {
        timetableRows.clear();
        timetableRows.add(getColumnTitlesString());

        LocalTime[] timeRange = getTimetableTimeRange(tasks);
        int firstHour = timeRange[0].getHour();
        int lastHour = timeRange[1].getHour();

        int currHour = firstHour;
        while (currHour <= lastHour) {
            timetableRows.add(generateRowString(currHour, tasks));
            currHour++;
        }
    }

    /**
     * Formats and returns the table header with the days of the week,
     * starting from the current day.
     */
    public String getColumnTitlesString() {
        String[] columnTitles = new String[8];
        columnTitles[0] = TIME_COLUMN_NAME;
        for (int i = 0; i < 7; i++) {
            LocalDateTime datePointer = LocalDateTime.now().plus(i, ChronoUnit.DAYS);
            columnTitles[i + 1] = ScheduleUi.getDayOfWeek(datePointer);
        }
        return String.format(ROW_FORMAT, (Object[]) columnTitles);
    }

    /**
     * Formats the contents of a table row.
     */
    public String generateRowString(int hour, TaskList tasks) {
        String[] rowContents = generateRowContents(hour, tasks);
        return String.format(ROW_FORMAT, (Object[]) rowContents);
    }

    /**
     * Returns the contents of each row of the timetable into an array.
     * Each row represents the tasks that fall within a certain time slot.
     */
    public String[] generateRowContents(int hour, TaskList tasks) {
        String[] rowContents = new String[8];
        LocalDate todayDate = LocalDate.now();

        rowContents[0] = getTimeSlotString(hour);

        for (int dayIncrement = 0; dayIncrement < 7; dayIncrement++) {
            LocalDate currDate = todayDate.plus(dayIncrement, ChronoUnit.DAYS);
            rowContents[dayIncrement + 1] = getCellString(currDate, hour, tasks);
        }

        return rowContents;
    }

    private String getTimeSlotString(int startHour) {
        String startHourString = String.format(TIME_FORMAT, startHour);
        String endHourString = String.format(TIME_FORMAT, (startHour + 1) % 24);
        return startHourString + "-" + endHourString;
    }

    /**
     * Returns the contents of each cell of the timetable.
     */
    private String getCellString(LocalDate date, int hour, TaskList tasks) {
        ArrayList<String> cellContents = new ArrayList<>();
        for (Task task : tasks.getTaskList()) {
            if (!(task instanceof Deadline) && task.isHappeningOn(date) && task.isWithinTimeSlot(hour)) {
                cellContents.add(task.getDescription());
            }
        }
        return trimToFitTimetableCell(cellContents.toString().replace("[", "").replace("]", ""));
    }

    public String trimToFitTimetableCell(String fullString) {
        if (fullString.length() > MAX_COLUMN_WIDTH) {
            return fullString.substring(0, MAX_COLUMN_WIDTH - 3) + "...";
        }
        return fullString;
    }

    /**
     * Iterates through the TaskList to determine the earliest and
     * latest time that has a certain Task scheduled, so that the
     * timetable can display all Tasks within that range.
     */
    public LocalTime[] getTimetableTimeRange(TaskList tasks) {
        int earliestHour = DEFAULT_START_HOUR;
        int latestHour = DEFAULT_END_HOUR;

        for (int hour = 0; hour < 24; hour++) {
            for (Task task: tasks.getTaskList()) {
                if (!(task instanceof Deadline) && task.isWithinTimeSlot(hour)) {
                    int currTaskStartHour = task.getStart().toLocalTime().getHour();
                    int currTaskEndHour = task.getEnd().toLocalTime().getHour();

                    earliestHour = getEarlierTime(earliestHour, currTaskStartHour, currTaskEndHour);
                    latestHour = getLaterTime(latestHour, currTaskStartHour, currTaskEndHour);
                }
            }
        }

        LocalTime earliestTime = LocalTime.parse(String.format(TIME_FORMAT, earliestHour));
        LocalTime latestTime = LocalTime.parse(String.format(TIME_FORMAT, latestHour));

        return new LocalTime[] {earliestTime, latestTime};
    }

    public int getEarlierTime(int referenceHour, int taskStartHour, int taskEndHour) {
        return Math.min(Math.min(referenceHour, taskStartHour),Math.min(referenceHour, taskEndHour));
    }

    public int getLaterTime(int referenceHour, int taskStartHour, int taskEndHour) {
        return Math.max(Math.max(referenceHour, taskStartHour),Math.max(referenceHour, taskEndHour));
    }
}