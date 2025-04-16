
# 📅 Event Management System in Java

This Java application is an advanced **Event Management System** that allows users to **schedule, modify, delete, and display events**, with smart handling of **conflicts and recurring schedules** using an **Interval Tree** data structure.

---

## ✨ Key Features

- ✅ Add events with title, description, time, duration, and priority
- 🔁 Supports both **single** and **recurring** events
- ⚠️ Detects and resolves scheduling conflicts using **priorities**
- 🔄 Modify existing events (title, time, duration, etc.)
- 🗑️ Delete single or all instances of recurring events
- 🕒 Display available **free time slots** between working hours
- 🌲 Efficient scheduling with **Interval Tree** for overlapping detection

---

## 💡 How It Works

The system uses **Java’s `LocalDateTime` and `Duration` classes** for time tracking and an **Interval Tree** to manage overlapping intervals efficiently.

- Events are stored as nodes in an interval tree
- When adding or modifying an event, the tree checks for conflicts
- Conflicts are resolved based on **priority**
- Free slots are calculated dynamically between scheduled events

---

## 🧱 System Architecture

### 1. **Event**
Represents the core event with ID, title, description, start and end time, duration, priority, and recurrence status.

### 2. **IntervalNode**
A node in the interval tree containing an event and pointers to left and right subtrees.

### 3. **IntervalTree**
Custom-built binary search tree structure for:
- Fast event insertion
- Conflict detection (`hasOverlap`)
- Event deletion
- Free slot calculation

### 4. **TimeSlot**
Simple representation of a time range with a start and end time.

### 5. **EventManagementSystem**
Handles the UI and user actions (menu-driven console app). Supports:
- Adding events
- Deleting events
- Modifying events
- Displaying events
- Showing free time slots

---

## 🖥️ Usage & Execution

### 📦 Prerequisites

- Java 8 or above
- Basic command-line interface

### ▶️ To Run

```bash
javac *.java
java EventManagementSystem
```

You'll be prompted with a console menu to interact with the system.

---

## 📁 Project Structure

```
├── Event.java
├── IntervalNode.java
├── IntervalTree.java
├── TimeSlot.java
├── EventManagementSystem.java
└── README.md
```

---

## 📝 Sample Functionalities

- Add recurring events every day for a week
- Detect if a new event conflicts with existing events
- Show all free time slots from 9 AM to 5 PM
- Delete an individual event or a series
- Modify event priority and reschedule

---

## 📅 Sample Event Format

```
Title: Team Meeting
Description: Discuss progress
Start: 2024-11-05 10:00
End:   2024-11-05 11:00
Priority: 2
Recurring: No
```

---

## ✅ Future Improvements

- GUI-based interface using JavaFX or Swing
- Export/Import events to/from `.csv`
- Google Calendar integration
- Role-based user access (admin, viewer)

---

## 👨‍💻 Authors

- Aadhikesh  
- Vinayak Kanagaraj  
- Aravind  
- Jaikrishna  

📅 **Project Date**: *04.11.2024*

---

## 📚 Topics Covered

- Data Structures (Interval Tree)
- Java OOP and Exception Handling
- Time and Date API
- Command-line interaction
- Recurring events and calendar logic
