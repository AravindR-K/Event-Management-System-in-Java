/*
Event Management System
This Java program implements an Event Management System that allows users to schedule,
modify, delete, and display events. The system supports both single and recurring events,
and it manages conflicts based on event priorities using an interval tree data structure.
Key Features:
Add events with details such as title, description, duration, start time, end time, and priority.
Support for recurring events with the ability to schedule them across multiple days.
Conflict detection and resolution based on event priorities, allowing users to either choose a new time slot
or attempt to reschedule conflicting events.
Display available time slots for scheduling events based on user-defined daily start and end times.
Delete events, including the option to remove all occurrences of a recurring event or just a single instance.
Modify existing events, including changing their title, description, duration, time slot, and priority.
The program utilizes Java's LocalDateTime and Duration classes for time management and employs
an interval tree to efficiently handle event scheduling and conflict resolution.
Author: Aadhikesh,Vinayak kanagaraj, Aravind, Jaikrishna.
Date: 04.11.2024
*/

/*
ADT for Event Management System

1. Event
Description: Represents an event with details such as title, description, duration, start time, end time, priority, and recurrence status.

Operations:

Event(eventId: String, title: String, description: String, duration: Duration, startTime: LocalDateTime, endTime: LocalDateTime, priority: int, isRecurring: boolean): Constructor to create a new event.
String getEventId(): Returns the event ID.
String getTitle(): Returns the title of the event.
void setTitle(String title): Sets the title of the event.
String getDescription(): Returns the description of the event.
void setDescription(String description): Sets the description of the event.
Duration getDuration(): Returns the duration of the event.
void setDuration(Duration duration): Sets the duration of the event.
LocalDateTime getStartTime(): Returns the start time of the event.
void setStartTime(LocalDateTime startTime): Sets the start time of the event.
LocalDateTime getEndTime(): Returns the end time of the event.
void setEndTime(LocalDateTime endTime): Sets the end time of the event.
int getPriority(): Returns the priority of the event.
void setPriority(int priority): Sets the priority of the event.
boolean isRecurring(): Returns whether the event is recurring.

2. IntervalNode
Description: Represents a node in the interval tree, containing an event and pointers to its children.

Operations:

IntervalNode(Event event): Constructor to create a new interval node.
Event getEvent(): Returns the event associated with this node.
LocalDateTime getMax(): Returns the maximum end time in the subtree.
IntervalNode getLeft(): Returns the left child node.
IntervalNode getRight(): Returns the right child node.

3. IntervalTree
Description: A binary search tree that manages events based on their start and end times, allowing for efficient scheduling and conflict detection.

Operations:

IntervalTree(): Constructor to create a new interval tree.
boolean insert(Event event): Inserts a new event into the tree. Returns false if the event ID already exists.
boolean hasOverlap(Event newEvent): Checks if the new event overlaps with any existing events.
void delete(String eventId): Deletes an event from the tree based on its event ID.
List<TimeSlot> findFreeSlots(LocalDateTime dayStart, LocalDateTime dayEnd, Duration minDuration): Finds and returns a list of free time slots within a specified time range.
List<Event> getAllEvents(): Returns a list of all events stored in the interval tree.

4. TimeSlot
Description: Represents a time slot with a start and end time.

Operations:

TimeSlot(LocalDateTime start, LocalDateTime end): Constructor to create a new time slot.
LocalDateTime getStart(): Returns the start time of the time slot.
LocalDateTime getEnd(): Returns the end time of the time slot.
5. EventManagementSystem
Description: The main class that manages the event scheduling system, including user interactions and event management.

Operations:

EventManagementSystem(): Constructor to initialize the event management system.
void initialize(): Initializes the system by prompting the user for locations, number of days, and daily start and end times.
void showMenu(): Displays the main menu and handles user input for various operations.
void addEvent(): Prompts the user to enter details for a new event and schedules it.
void deleteEvent(): Deletes an event based on user input.
void modifyEvent(): Modifies an existing event based on user input.
void displayEvents(): Displays all scheduled events.
void displayFreeSlots(LocalDate date, Duration duration): Displays available time slots for scheduling events.
*/



import java.time.*;
import java.time.format.*;
import java.util.*;

// Event class to store event details
class Event {
    private String eventId;
    private String title;
    private String description;
    private Duration duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int priority;  // 1-4 (Eisenhower Matrix: 1=Urgent&Important, 4=Not Urgent&Not Important)
    private boolean isRecurring;
    private List<LocalDateTime> recurringDates;

    public Event(String eventId, String title, String description, Duration duration,
                 LocalDateTime startTime, LocalDateTime endTime, int priority, boolean isRecurring) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority = priority;
        this.isRecurring = isRecurring;
        this.recurringDates = new ArrayList<>();
    }

    // Getters and setters
    public String getEventId() { return eventId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Duration getDuration() { return duration; }
    public void setDuration(Duration duration) { this.duration = duration; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public boolean isRecurring() { return isRecurring; }
    public List<LocalDateTime> getRecurringDates() { return recurringDates; }
}

// Interval Tree Node
class IntervalNode {
    Event event;
    LocalDateTime max;
    IntervalNode left, right;

    public IntervalNode(Event event) {
        this.event = event;
        this.max = event.getEndTime();
        this.left = this.right = null;
    }
}

// Interval Tree implementation
class IntervalTree {
    private IntervalNode root;
    private Set<String> eventIds;

    public IntervalTree() {
        root = null;
        eventIds = new HashSet<>();
    }

    // Insert a new event into the tree
    // Inserts a new event into the interval tree. Returns false if the event ID already exists; otherwise, it adds the event and returns true.
    public boolean insert(Event event) {
        if (eventIds.contains(event.getEventId())) {
            return false; // Don't insert if event ID already exists
        }
        root = insert(root, event);
        eventIds.add(event.getEventId());
        return true;
    }

    //Recursive helper method to insert an event into the correct position in the tree based on the event's start time.
    private IntervalNode insert(IntervalNode node, Event event) {
        if (node == null) {
            return new IntervalNode(event);
        }

        LocalDateTime start = event.getStartTime();
        LocalDateTime nodeStart = node.event.getStartTime();

        if (start.isBefore(nodeStart)) {
            node.left = insert(node.left, event);
        } else {
            node.right = insert(node.right, event);
        }

        if (node.max.isBefore(event.getEndTime())) {
            node.max = event.getEndTime();
        }

        return node;
    }

    // Function for checking if there's any overlap with existing events returns True if there exists any
    public boolean hasOverlap(Event newEvent) {
        return searchOverlap(root, newEvent);
    }
    //  Recursive method to search for overlapping events in the tree.
    private boolean searchOverlap(IntervalNode node, Event event) {
        if (node == null) return false;

        if (overlaps(node.event, event)) {
            return true;
        }

        if (node.left != null && !node.left.event.getStartTime().isAfter(event.getEndTime())) {
            return searchOverlap(node.left, event);
        }

        return searchOverlap(node.right, event);
    }

    // Determines if two events overlap based on their start and end times.
    private boolean overlaps(Event e1, Event e2) {
        return !(e1.getEndTime().isBefore(e2.getStartTime()) ||
                e2.getEndTime().isBefore(e1.getStartTime()));
    }

    // Delete an event from the tree
    public void delete(String eventId) {
        root = delete(root, eventId);
        eventIds.remove(eventId);
    }

    private IntervalNode delete(IntervalNode node, String eventId) {
        if (node == null) return null;

        if (node.event.getEventId().equals(eventId)) {
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;

            IntervalNode successor = findMin(node.right);
            node.event = successor.event;
            node.right = delete(node.right, successor.event.getEventId());
        } else if (node.left != null && node.left.event.getEventId().equals(eventId)) {
            node.left = delete(node.left, eventId);
        } else {
            node.right = delete(node.right, eventId);
        }

        updateMax(node);
        return node;
    }

    private IntervalNode findMin(IntervalNode node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    private void updateMax(IntervalNode node) {
        node.max = node.event.getEndTime();
        if (node.left != null && node.left.max.isAfter(node.max)) {
            node.max = node.left.max;
        }
        if (node.right != null && node.right.max.isAfter(node.max)) {
            node.max = node.right.max;
        }
    }


    //Finds and returns a list of free time slots within a specified time range that can accommodate a minimum duration.
    public List<TimeSlot> findFreeSlots(LocalDateTime dayStart, LocalDateTime dayEnd, Duration minDuration) {
        List<Event> events = new ArrayList<>();
        collectEvents(root, events);

        // Sort events by start time
        events.sort(Comparator.comparing(Event::getStartTime));

        List<TimeSlot> freeSlots = new ArrayList<>();
        LocalDateTime currentTime = dayStart;

        // Validate inputs
        if (dayStart.isAfter(dayEnd) || minDuration.isNegative() || minDuration.isZero()) {
            return freeSlots;
        }

        // If there are no events, return the entire day as free
        if (events.isEmpty()) {
            freeSlots.add(new TimeSlot(dayStart, dayEnd));
            return freeSlots;
        }

        for (Event event : events) {
            // Skip events outside our day range
            if (event.getEndTime().isBefore(dayStart) || event.getStartTime().isAfter(dayEnd)) {
                continue;
            }

            // Check for free slot before the current event
            if (currentTime.isBefore(event.getStartTime())) {
                Duration slotDuration = Duration.between(currentTime, event.getStartTime());
                if (slotDuration.compareTo(minDuration) >= 0) {
                    freeSlots.add(new TimeSlot(currentTime, event.getStartTime()));
                }
            }

            // Update current time to the end of current event
            currentTime = event.getEndTime();
        }

        // Check for remaining time after the last event
        if (currentTime.isBefore(dayEnd)) {
            Duration finalSlotDuration = Duration.between(currentTime, dayEnd);
            if (finalSlotDuration.compareTo(minDuration) >= 0) {
                freeSlots.add(new TimeSlot(currentTime, dayEnd));
            }
        }

        return freeSlots;
    }

    private void collectEvents(IntervalNode node, List<Event> events) {
        if (node == null) return;
        collectEvents(node.left, events);
        events.add(node.event);
        collectEvents(node.right, events);
    }

    // Get all events for display
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        collectEvents(root, events);
        return events;
    }
}

// Time slot class for representing free time slots
class TimeSlot {
    private LocalDateTime start;
    private LocalDateTime end;

    public TimeSlot(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
}

// Main Event Management System
public class EventManagementSystem {
    private Map<LocalDate, IntervalTree> dailyEvents;
    private Set<String> locations;
    private LocalTime defaultStartTime;
    private LocalTime defaultEndTime;
    private int totalDays;
    private Scanner scanner;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public EventManagementSystem() {
        this.dailyEvents = new HashMap<>();
        this.locations = new HashSet<>();
        this.scanner = new Scanner(System.in);
    }

    //Initializes the system by prompting the user for locations, the number of days for events, and daily start and end times.
    public void initialize() {
        System.out.println("Enter locations (comma-separated):");
        String[] locs = scanner.nextLine().split(",");
        for (String loc : locs) {
            locations.add(loc.trim());
        }

        System.out.println("Enter number of days for the event:");
        totalDays = Integer.parseInt(scanner.nextLine());

        System.out.println("Enter daily start time (HH:mm):");
        defaultStartTime = LocalTime.parse(scanner.nextLine(), TIME_FORMATTER);

        System.out.println("Enter daily end time (HH:mm):");
        defaultEndTime = LocalTime.parse(scanner.nextLine(), TIME_FORMATTER);

        LocalDate startDate = LocalDate.now();
        for (int i = 0; i < totalDays; i++) {
            dailyEvents.put(startDate.plusDays(i), new IntervalTree());
        }
    }

    // Displays the main menu and handles user input for various operations such as adding, deleting, modifying, and displaying events.
    public void showMenu() {
        while (true) {
            System.out.println("\n=== Event Management System ===");
            System.out.println("1. Add Event");
            System.out.println("2. Delete Event");
            System.out.println("3. Modify Event");
            System.out.println("4. Display Events");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    addEvent();
                    break;
                case 2:
                    deleteEvent();
                    break;
                case 3:
                    modifyEvent();
                    break;
                case 4:
                    displayEvents();
                    break;
                case 5:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    // Prompts the user to enter details for a new event and schedules it, either as a single or recurring event.
    private void addEvent() {
        System.out.println("\nAdding New Event");
        System.out.println("Enter Event ID:");
        String eventId = scanner.nextLine();

        System.out.println("Enter Title:");
        String title = scanner.nextLine();

        System.out.println("Enter Description:");
        String description = scanner.nextLine();

        System.out.println("Enter Duration (minutes):");
        Duration duration = Duration.ofMinutes(Integer.parseInt(scanner.nextLine()));

        System.out.println("Is this event recurring? (yes/no):");
        boolean isRecurring = scanner.nextLine().equalsIgnoreCase("yes");

        System.out.println("Enter Priority (1-4):");
        System.out.println("1 - Urgent & Important");
        System.out.println("2 - Important but Not Urgent");
        System.out.println("3 - Urgent but Not Important");
        System.out.println("4 - Neither Urgent nor Important");
        int priority = Integer.parseInt(scanner.nextLine());

        if (isRecurring) {
            scheduleRecurringEvent(eventId, title, description, duration, priority);
        } else {
            scheduleSingleEvent(eventId, title, description, duration, priority);
        }
    }

    // Schedules a single event for a selected day, checking for free slots and conflicts.
    private void scheduleSingleEvent(String eventId, String title, String description,
                                     Duration duration, int priority) {
        System.out.println("\nAvailable days:");
        List<LocalDate> dates = new ArrayList<>(dailyEvents.keySet());
        for (int i = 0; i < dates.size(); i++) {
            System.out.println((i + 1) + ". " + dates.get(i).format(DATE_FORMATTER));
        }

        System.out.println("Select day (1-" + dates.size() + "):");
        int dayChoice = Integer.parseInt(scanner.nextLine()) - 1;
        LocalDate selectedDate = dates.get(dayChoice);

        displayFreeSlots(selectedDate, duration);
        System.out.println("Enter start time (HH:mm):");
        LocalTime startTime = LocalTime.parse(scanner.nextLine(), TIME_FORMATTER);

        LocalDateTime eventStart = LocalDateTime.of(selectedDate, startTime);
        LocalDateTime eventEnd = eventStart.plus(duration);

        Event newEvent = new Event(eventId, title, description, duration,
                eventStart, eventEnd, priority, false);

        if (checkAndScheduleEvent(selectedDate, newEvent)) {
            System.out.println("Event scheduled successfully!");
        }
    }

    // Schedules a recurring event for multiple days, allowing the user to specify start times for each occurrence.
    private void scheduleRecurringEvent(String eventId, String title, String description,
                                        Duration duration, int priority) {
        System.out.println("\nSchedule recurring event for each day:");

        for (LocalDate date : dailyEvents.keySet()) {
            System.out.println("\nScheduling for " + date.format(DATE_FORMATTER));
            displayFreeSlots(date, duration);

            System.out.println("Enter start time for this day (HH:mm) or 'skip' to skip:");
            String input = scanner.nextLine();

            if (!input.equalsIgnoreCase("skip")) {
                LocalTime startTime = LocalTime.parse(input, TIME_FORMATTER);
                LocalDateTime eventStart = LocalDateTime.of(date, startTime);
                LocalDateTime eventEnd = eventStart.plus(duration);

                Event newEvent = new Event(eventId + "_" + date.format(DATE_FORMATTER),
                        title, description, duration, eventStart, eventEnd,
                        priority, true);

                if (checkAndScheduleEvent(date, newEvent)) {
                    System.out.println("Event scheduled for " + date.format(DATE_FORMATTER));
                }
            }
        }
    }

    //Checks for conflicts with an event and attempts to schedule it if no conflicts are found.
    private boolean checkAndScheduleEvent(LocalDate date, Event newEvent) {
        IntervalTree tree = dailyEvents.get(date);

        if (tree.hasOverlap(newEvent)) {
            System.out.println("Time slot conflict detected!");
            if (handleConflict(date, newEvent)) {
                tree.insert(newEvent);
                return true;
            }
            return false;
        }

        tree.insert(newEvent);
        return true;
    }

    // Handles conflicts by allowing the user to choose a new time slot, attempt to reschedule conflicting events, or cancel scheduling.
    private boolean handleConflict(LocalDate date, Event newEvent) {
        System.out.println("Would you like to:");
        System.out.println("1. Choose another time slot");
        System.out.println("2. Try to reschedule conflicting events (if priority permits)");
        System.out.println("3. Cancel scheduling");

        int choice = Integer.parseInt(scanner.nextLine());

        switch (choice) {
            case 1:
                displayFreeSlots(date, newEvent.getDuration());
                System.out.println("Enter new start time (HH:mm):");
                LocalTime newStartTime = LocalTime.parse(scanner.nextLine(), TIME_FORMATTER);
                newEvent.setStartTime(LocalDateTime.of(date, newStartTime));
                newEvent.setEndTime(newEvent.getStartTime().plus(newEvent.getDuration()));
                return checkAndScheduleEvent(date, newEvent);

            case 2:
                return tryRescheduleConflicts(date, newEvent);

            default:
                return false;
        }
    }

    // Attempts to reschedule conflicting events based on priority and available time slots.
    private boolean tryRescheduleConflicts(LocalDate date, Event newEvent) {
        IntervalTree tree = dailyEvents.get(date);
        List<Event> conflicts = findConflictingEvents(date, newEvent);

        if (conflicts.isEmpty()) {
            tree.insert(newEvent);  // Only insert if there are no conflicts
            return true;
        }

        // Check if new event has higher priority than all conflicts
        for (Event conflict : conflicts) {
            if (conflict.getPriority() <= newEvent.getPriority()) {
                System.out.println("Cannot reschedule - Conflicting event has equal or higher priority:");
                displayEvent(conflict);
                return false;
            }
        }

        // Remove all conflicting events
        for (Event event : conflicts) {
            tree.delete(event.getEventId());
        }

        // Schedule the new event
        tree.insert(newEvent);

        // Try to reschedule conflicting events
        boolean allRescheduled = true;
        for (Event event : conflicts) {
            List<TimeSlot> freeSlots = tree.findFreeSlots(
                    LocalDateTime.of(date, defaultStartTime),
                    LocalDateTime.of(date, defaultEndTime),
                    event.getDuration()
            );

            if (freeSlots.isEmpty()) {
                System.out.println("Failed to reschedule all conflicts!");
                allRescheduled = false;
                break;
            }

            // Schedule in the first available slot
            TimeSlot slot = freeSlots.get(0);
            event.setStartTime(slot.getStart());
            event.setEndTime(slot.getStart().plus(event.getDuration()));
            tree.insert(event);
        }

        if (!allRescheduled) {
            // If rescheduling failed, revert all changes
            tree.delete(newEvent.getEventId());
            for (Event event : conflicts) {
                tree.insert(event);
            }
            return false;
        }

        return true;
    }

    private List<Event> findConflictingEvents(LocalDate date, Event newEvent) {
        List<Event> conflicts = new ArrayList<>();
        IntervalTree tree = dailyEvents.get(date);
        List<Event> allEvents = tree.getAllEvents();

        for (Event event : allEvents) {
            // Skip if it's the same event (important for modifications)
            if (event.getEventId().equals(newEvent.getEventId())) {
                continue;
            }

            // Check for time overlap
            boolean hasOverlap = isTimeOverlapping(event, newEvent);
            if (hasOverlap) {
                conflicts.add(event);
            }
        }

        return conflicts;
    }

    private boolean isTimeOverlapping(Event event1, Event event2) {
        // Event times overlap if one event doesn't end before the other starts
        return !(event1.getEndTime().isBefore(event2.getStartTime()) ||
                event2.getEndTime().isBefore(event1.getStartTime()));
    }



    private void displayFreeSlots(LocalDate date, Duration duration) {
        System.out.println("\nAvailable time slots:");
        List<TimeSlot> freeSlots = dailyEvents.get(date).findFreeSlots(
                LocalDateTime.of(date, defaultStartTime),
                LocalDateTime.of(date, defaultEndTime),
                duration
        );

        if (freeSlots.isEmpty()) {
            System.out.println("No free slots available for the requested duration!");
            return;
        }

        for (int i = 0; i < freeSlots.size(); i++) {
            TimeSlot slot = freeSlots.get(i);
            System.out.printf("%d. %s - %s\n",
                    i + 1,
                    slot.getStart().format(DateTimeFormatter.ofPattern("HH:mm")),
                    slot.getEnd().format(DateTimeFormatter.ofPattern("HH:mm"))
            );
        }
    }

    private void deleteEvent() {
        System.out.println("\nEnter Event ID to delete:");
        String eventId = scanner.nextLine();

        // Check if it's a recurring event
        boolean isRecurring = false;
        LocalDate recurringDate = null;

        for (Map.Entry<LocalDate, IntervalTree> entry : dailyEvents.entrySet()) {
            List<Event> events = entry.getValue().getAllEvents();
            for (Event event : events) {
                if (event.getEventId().startsWith(eventId + "_")) {
                    isRecurring = true;
                    recurringDate = entry.getKey();
                    break;
                }
            }
            if (isRecurring) break;
        }

        if (isRecurring) {
            System.out.println("This is a recurring event. Delete:");
            System.out.println("1. All occurrences");
            System.out.println("2. Single occurrence");
            int choice = Integer.parseInt(scanner.nextLine());

            if (choice == 1) {
                deleteRecurringEvent(eventId);
            } else {
                System.out.println("Enter the date (yyyy-MM-dd):");
                LocalDate date = LocalDate.parse(scanner.nextLine(), DATE_FORMATTER);
                dailyEvents.get(date).delete(eventId + "_" + date.format(DATE_FORMATTER));
            }
        } else {
            // Delete single event
            for (IntervalTree tree : dailyEvents.values()) {
                tree.delete(eventId);
            }
        }
        System.out.println("Event(s) deleted successfully!");
    }

    private void deleteRecurringEvent(String baseEventId) {
        for (Map.Entry<LocalDate, IntervalTree> entry : dailyEvents.entrySet()) {
            entry.getValue().delete(baseEventId + "_" + entry.getKey().format(DATE_FORMATTER));
        }
    }

    private void modifyEvent() {
        System.out.println("\nEnter Event ID to modify:");
        String eventId = scanner.nextLine();

        List<Event> eventsToModify = new ArrayList<>();
        List<LocalDate> eventDates = new ArrayList<>();

        // Find all occurrences of the recurring event
        for (Map.Entry<LocalDate, IntervalTree> entry : dailyEvents.entrySet()) {
            List<Event> events = entry.getValue().getAllEvents();
            for (Event event : events) {
                if (event.getEventId().equals(eventId) || event.getEventId().startsWith(eventId + "_")) {
                    eventsToModify.add(event);
                    eventDates.add(entry.getKey());
                }
            }
        }

        if (eventsToModify.isEmpty()) {
            System.out.println("Event not found!");
            return;
        }

        // Modify each occurrence of the event separately
        for (int i = 0; i < eventsToModify.size(); i++) {
            Event eventToModify = eventsToModify.get(i);
            LocalDate eventDate = eventDates.get(i);

            System.out.println("\nModifying event on date: " + eventDate.format(DATE_FORMATTER));
            displayEvent(eventToModify);

            System.out.println("\nWhat would you like to modify?");
            System.out.println("1. Title");
            System.out.println("2. Description");
            System.out.println("3. Duration");
            System.out.println("4. Time slot");
            System.out.println("5. Priority");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    System.out.println("Enter new title:");
                    eventToModify.setTitle(scanner.nextLine());
                    break;

                case 2:
                    System.out.println("Enter new description:");
                    eventToModify.setDescription(scanner.nextLine());
                    break;

                case 3:
                    System.out.println("Enter new duration (minutes):");
                    Duration newDuration = Duration.ofMinutes(Integer.parseInt(scanner.nextLine()));
                    modifyEventDuration(eventToModify, eventDate, newDuration);
                    break;

                case 4:
                    modifyEventTimeSlot(eventToModify, eventDate);
                    break;

                case 5:
                    System.out.println("Enter new priority (1-4):");
                    eventToModify.setPriority(Integer.parseInt(scanner.nextLine()));
                    break;
            }

            // Re-insert the modified event into the interval tree
            IntervalTree tree = dailyEvents.get(eventDate);
            tree.delete(eventToModify.getEventId());
            tree.insert(eventToModify);

            System.out.println("Event modified successfully on " + eventDate.format(DATE_FORMATTER) + "!");
        }
    }


    private void modifyEventDuration(Event event, LocalDate date, Duration newDuration) {
        IntervalTree tree = dailyEvents.get(date);
        LocalDateTime newEndTime = event.getStartTime().plus(newDuration);

        // Check if extended duration creates conflicts
        Event tempEvent = new Event(
                event.getEventId(), event.getTitle(), event.getDescription(),
                newDuration, event.getStartTime(), newEndTime,
                event.getPriority(), event.isRecurring()
        );

        tree.delete(event.getEventId());
        if (checkAndScheduleEvent(date, tempEvent)) {
            event.setDuration(newDuration);
            event.setEndTime(newEndTime);
        } else {
            tree.insert(event); // Revert if modification failed
            System.out.println("Could not modify duration due to conflicts!");
        }
    }

    private void modifyEventTimeSlot(Event event, LocalDate date) {
        IntervalTree tree = dailyEvents.get(date);
        tree.delete(event.getEventId());

        displayFreeSlots(date, event.getDuration());
        System.out.println("Enter new start time (HH:mm):");
        LocalTime newStartTime = LocalTime.parse(scanner.nextLine(), TIME_FORMATTER);

        LocalDateTime newStartDateTime = LocalDateTime.of(date, newStartTime);
        event.setStartTime(newStartDateTime);
        event.setEndTime(newStartDateTime.plus(event.getDuration()));

        if (!checkAndScheduleEvent(date, event)) {
            System.out.println("Could not modify time slot due to conflicts!");
            // Revert to original time slot
            tree.insert(event);
        }
    }

    private void displayEvents() {
        System.out.println("\n=== Events Schedule ===");

        for (Map.Entry<LocalDate, IntervalTree> entry : dailyEvents.entrySet()) {
            LocalDate date = entry.getKey();
            List<Event> events = entry.getValue().getAllEvents();

            if (events.isEmpty()) continue;

            System.out.println("\nDate: " + date.format(DATE_FORMATTER));
            System.out.println(String.format("%-10s %-20s %-15s %-15s %-10s %-10s",
                    "ID", "Title", "Start", "End", "Priority", "Recurring"));
            System.out.println("-".repeat(80));

            events.sort((e1, e2) -> e1.getStartTime().compareTo(e2.getStartTime()));

            for (Event event : events) {
                displayEvent(event);
            }
        }
    }

    private void displayEvent(Event event) {
        System.out.println(String.format("%-10s %-20s %-15s %-15s %-10d %-10s",
                event.getEventId(),
                event.getTitle(),
                event.getStartTime().format(TIME_FORMATTER),
                event.getEndTime().format(TIME_FORMATTER),
                event.getPriority(),
                event.isRecurring() ? "Yes" : "No"
        ));
    }

    public static void main(String[] args) {
        EventManagementSystem system = new EventManagementSystem();
        system.initialize();
        system.showMenu();
    }
}
