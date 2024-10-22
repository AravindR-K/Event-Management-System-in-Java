package Draft_4;

import java.util.*;

class Interval implements Comparable<Interval> {
    int start, end;

    public Interval(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public boolean overlaps(Interval other) {
        return this.start < other.end && other.start < this.end;
    }

    @Override
    public String toString() {
        return "[" + start + ", " + end + "]";
    }

    @Override
    public int compareTo(Interval other) {
        if (this.start != other.start) {
            return Integer.compare(this.start, other.start);
        }
        return Integer.compare(this.end, other.end);
    }
}

class Event {
    int eventID;
    String title;
    String description;
    int priority;  // Lower value means higher priority
    Interval interval;
    boolean isRecurring;
    int recurringDays;  // Number of days for recurring events

    public Event(int eventID, String title, String description, int priority, Interval interval, boolean isRecurring, int recurringDays) {
        this.eventID = eventID;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.interval = interval;
        this.isRecurring = isRecurring;
        this.recurringDays = recurringDays;
    }

    @Override
    public String toString() {
        return "ID: " + eventID + ", Title: " + title + ", Time: " + interval + ", Priority: " + priority + ", Recurring: " + isRecurring;
    }
}

class IntervalTree {
    private TreeMap<Interval, Event> intervalTree = new TreeMap<>();

    public boolean addEvent(Event event) {
        // Check for conflicts
        for (Map.Entry<Interval, Event> entry : intervalTree.entrySet()) {
            if (entry.getKey().overlaps(event.interval)) {
                Event conflictingEvent = entry.getValue();

                // Compare priorities
                if (event.priority < conflictingEvent.priority) {
                    // New event has higher priority, reschedule conflicting event
                    intervalTree.remove(entry.getKey());
                    intervalTree.put(event.interval, event);
                    return rescheduleEvent(conflictingEvent);
                } else {
                    // Conflict with higher or equal priority, reschedule the new event
                    return rescheduleEvent(event);
                }
            }
        }
        // No conflict, add the event
        intervalTree.put(event.interval, event);
        return true;
    }

    private boolean rescheduleEvent(Event event) {
        int newStart = Math.max(event.interval.start, getEarliestAvailableTime(event.interval.start)); // Get earliest available slot
        int newEnd = newStart + (event.interval.end - event.interval.start); // Maintain the same duration

        while (newEnd <= 24) { // Assuming we're working within a 24-hour period
            Interval newInterval = new Interval(newStart, newEnd);

            // Check if new interval is free
            boolean conflict = false;
            for (Interval interval : intervalTree.keySet()) {
                if (interval.overlaps(newInterval)) {
                    conflict = true;
                    break;
                }
            }

            if (!conflict) {
                // Slot found, reschedule the event
                event.interval = newInterval;
                intervalTree.put(newInterval, event);
                return true;
            }

            // If there's a conflict, move the event further
            newStart = newEnd;
            newEnd = newStart + (event.interval.end - event.interval.start);
        }

        // No free slot found
        System.out.println("No available slot to reschedule event: " + event.title);
        return false;
    }

    private int getEarliestAvailableTime(int startTime) {
        for (Interval interval : intervalTree.keySet()) {
            if (interval.start >= startTime) {
                return interval.end; // Return the end time of the first conflicting event
            }
        }
        return startTime; // If no conflict, return the initial start time
    }




    public boolean removeEvent(int eventID) {
        Interval intervalToRemove = null;
        for (Map.Entry<Interval, Event> entry : intervalTree.entrySet()) {
            if (entry.getValue().eventID == eventID) {
                intervalToRemove = entry.getKey();
                break;
            }
        }
        if (intervalToRemove != null) {
            intervalTree.remove(intervalToRemove);
            return true;
        }
        return false;
    }

    public boolean modifyEvent(int eventID, Interval newInterval, int newPriority) {
        Event eventToModify = null;
        Interval oldInterval = null;

        // Find event by ID
        for (Map.Entry<Interval, Event> entry : intervalTree.entrySet()) {
            if (entry.getValue().eventID == eventID) {
                eventToModify = entry.getValue();
                oldInterval = entry.getKey();
                break;
            }
        }

        if (eventToModify == null) {
            return false; // Event not found
        }

        // Remove the old event and check if the new slot is free
        intervalTree.remove(oldInterval);
        if (addEvent(new Event(eventID, eventToModify.title, eventToModify.description, newPriority, newInterval, eventToModify.isRecurring, eventToModify.recurringDays))) {
            return true; // Event successfully modified
        } else {
            intervalTree.put(oldInterval, eventToModify); // Restore the old event
            return false; // Conflict in new interval
        }
    }

    public void displayEvents() {
        if (intervalTree.isEmpty()) {
            System.out.println("No events scheduled.");
        } else {
            System.out.println("Scheduled Events:");
            for (Map.Entry<Interval, Event> entry : intervalTree.entrySet()) {
                System.out.println(entry.getValue());
            }
        }
    }

    public TreeMap<Interval, Event> getIntervalTree() {
        return intervalTree;
    }

}

class EventManager {
    private static int nextEventID = 1;
    private IntervalTree[] days;

    public EventManager(int totalDays) {
        days = new IntervalTree[totalDays];
        for (int i = 0; i < totalDays; i++) {
            days[i] = new IntervalTree();
        }
    }

    public void addEvent(String title, String description, int priority, Interval interval, boolean isRecurring, int recurringDays) {
        if (isRecurring) {
            for (int i = 0; i < recurringDays; i++) {
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter the day number to schedule the event (1 to " + days.length + "): ");
                int day = scanner.nextInt();

                // Validate the day input
                if (day >= 1 && day <= days.length) {
                    Event event = new Event(nextEventID++, title, description, priority, interval, isRecurring, recurringDays);
                    if (!days[day - 1].addEvent(event)) {
                        System.out.println("Conflict detected for day " + day + ".");
                    }
                } else {
                    System.out.println("Invalid day: " + day + ". Skipping this occurrence.");
                }
            }
        } else {
            // For non-recurring events, add to the first day
            if (!days[0].addEvent(new Event(nextEventID++, title, description, priority, interval, isRecurring, recurringDays))) {
                System.out.println("Conflict detected for day 1.");
            }
        }
    }

    public void deleteEvent(int eventID, boolean deleteAllOccurrences) {
        if (deleteAllOccurrences) {
            for (IntervalTree day : days) {
                day.removeEvent(eventID);
            }
        } else {
            days[0].removeEvent(eventID);
        }
    }

    public void modifyEvent(int eventID, Interval newInterval, int newPriority) {
        for (IntervalTree day : days) {
            if (day.modifyEvent(eventID, newInterval, newPriority)) {
                System.out.println("Event modified.");
                return;
            }
        }
        System.out.println("Event not found or conflict in new schedule.");
    }

    public void displayAllEvents() {
        String leftAlignFormat = "| %-10s | %-15s | %-10s | %-10s | %-8s | %-10s | %-8s |%n";

        for (int i = 0; i < days.length; i++) {
            IntervalTree dayTree = days[i];

            System.out.println("Day " + (i + 1) + ":");
            System.out.format("+------------+-----------------+------------+------------+----------+------------+----------+%n");
            System.out.format("| Event ID   | Title           | Start Time | End Time   | Priority | Recurring  | Duration |%n");
            System.out.format("+------------+-----------------+------------+------------+----------+------------+----------+%n");

            if (dayTree.getIntervalTree().isEmpty()) {
                System.out.println("| No events scheduled.                                                        |");
            } else {
                for (Map.Entry<Interval, Event> entry : dayTree.getIntervalTree().entrySet()) {
                    Event event = entry.getValue();
                    Interval interval = event.interval;

                    System.out.printf(leftAlignFormat,
                            event.eventID,
                            event.title,
                            interval.start + ":00",
                            interval.end + ":00",
                            event.priority,
                            event.isRecurring ? "Yes" : "No",
                            (interval.end - interval.start) + "h"
                    );
                }
            }
            System.out.format("+------------+-----------------+------------+------------+----------+------------+----------+%n");
        }
    }
}


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter number of days for the event:");
        int days = scanner.nextInt();

        EventManager manager = new EventManager(days);
        int choice;

        do {
            System.out.println("\n1. Add Event\n2. Delete Event\n3. Modify Event\n4. Display Events\n5. Exit");
            System.out.print("Enter choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();  // Consume the newline character after the integer

            switch (choice) {
                case 1:
                    System.out.print("Enter title: ");
                    String title = scanner.nextLine();
                    System.out.print("Enter description: ");
                    String desc = scanner.nextLine();
                    System.out.print("Enter priority (1-highest to 5-lowest): ");
                    int priority = scanner.nextInt();
                    System.out.print("Enter start time (24-hour format, e.g., 10 for 10 AM): ");
                    int startTime = scanner.nextInt();
                    System.out.print("Enter end time (24-hour format): ");
                    int endTime = scanner.nextInt();
                    System.out.print("Is this event recurring (true/false): ");
                    boolean isRecurring = scanner.nextBoolean();
                    int recurringDays = isRecurring ? days : 1;

                    Interval interval = new Interval(startTime, endTime);
                    manager.addEvent(title, desc, priority, interval, isRecurring, recurringDays);
                    break;
                case 2:
                    System.out.print("Enter Event ID to delete: ");
                    int eventID = scanner.nextInt();
                    System.out.print("Delete all occurrences (true/false): ");
                    boolean deleteAll = scanner.nextBoolean();
                    manager.deleteEvent(eventID, deleteAll);
                    break;
                case 3:
                    System.out.print("Enter Event ID to modify: ");
                    int modifyID = scanner.nextInt();
                    System.out.print("Enter new start time: ");
                    int newStart = scanner.nextInt();
                    System.out.print("Enter new end time: ");
                    int newEnd = scanner.nextInt();
                    System.out.print("Enter new priority: ");
                    int newPriority = scanner.nextInt();
                    Interval newInterval = new Interval(newStart, newEnd);
                    manager.modifyEvent(modifyID, newInterval, newPriority);
                    break;
                case 4:
                    manager.displayAllEvents();
                    break;
                case 5:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        } while (choice != 5);

        scanner.close();
    }
}
