📚 College Library Management System
A simple Java Swing + SQLite based desktop application to manage book borrowing in a college library.

✅ Features
Borrow books
View borrowed books by UID
Return borrowed books
Search student borrowing details
Auto-generated borrow and return dates
🗄️ Database
Uses SQLite (local file DB)
Table: borrowed_books
Fields: id, name, uid, book_title, library, borrowed_at, return_date
🛠️ Tech Used
Java (Swing)
SQLite (JDBC)
JDBC Driver: org.sqlite.JDBC
▶️ How to Run
javac CollegeLibrarySystem.java
java CollegeLibrarySystem
Database file auto-creates at:

lib/college_libraries.db
📌 Description
This app allows students to borrow and return books through a simple GUI. All records are stored in a SQLite database, and data can be viewed or searched using UID.
