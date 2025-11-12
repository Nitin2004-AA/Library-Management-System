import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class CollegeLibrarySystem extends JFrame {

    public CollegeLibrarySystem() {
        setTitle("College Library System");
        setSize(600, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 30));

        // Buttons
        JButton borrowButton = new JButton("Borrow Book");
        JButton viewButton = new JButton("View Borrowed Books");
        JButton returnButton = new JButton("Return Book");
        JButton searchStudentButton = new JButton("Search Student by UID");
        JButton exitButton = new JButton("Exit");

        add(borrowButton);
        add(viewButton);
        add(returnButton);
        add(searchStudentButton);
        add(exitButton);

        borrowButton.addActionListener(e -> openBorrowDialog());
        viewButton.addActionListener(e -> openViewDialog());
        returnButton.addActionListener(e -> openReturnDialog());
        searchStudentButton.addActionListener(e -> openStudentSearchDialog());
        exitButton.addActionListener(e -> System.exit(0));

        setVisible(true);
        createTable(); // Create DB table
    }

    private void openBorrowDialog() {
        JDialog dialog = new JDialog(this, "Borrow Book", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);

        JTextField nameField = new JTextField(20);
        JTextField uidField = new JTextField(20);
        JTextField bookField = new JTextField(20);
        JComboBox<String> libraryBox = new JComboBox<>(new String[]{"B1", "C3", "D6", "B2"});
        JButton submitButton = new JButton("Submit");

        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Student Name:"), gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("UID:"), gbc);
        gbc.gridx = 1;
        dialog.add(uidField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Book Title:"), gbc);
        gbc.gridx = 1;
        dialog.add(bookField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Library:"), gbc);
        gbc.gridx = 1;
        dialog.add(libraryBox, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        dialog.add(submitButton, gbc);

        submitButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String uid = uidField.getText().trim();
            String book = bookField.getText().trim();
            String library = (String) libraryBox.getSelectedItem();

            if (name.isEmpty() || uid.isEmpty() || book.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sql = "INSERT INTO borrowed_books (name, uid, book_title, library, borrowed_at, return_date) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                String borrowDate = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 14);
                String returnDate = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());

                pstmt.setString(1, name);
                pstmt.setString(2, uid);
                pstmt.setString(3, book);
                pstmt.setString(4, library);
                pstmt.setString(5, borrowDate);
                pstmt.setString(6, returnDate);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(dialog, "✅ Book borrowed successfully!");
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    private void openViewDialog() {
        JDialog dialog = new JDialog(this, "View Borrowed Books", true);
        dialog.setSize(700, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel();
        JTextField searchField = new JTextField(15);
        JButton searchButton = new JButton("Search by UID");
        searchPanel.add(new JLabel("Search UID:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        JTextArea displayArea = new JTextArea();
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        dialog.add(searchPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);

        searchButton.addActionListener(e -> {
            String uid = searchField.getText().trim();
            if (uid.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "UID cannot be empty!", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sql = "SELECT * FROM borrowed_books WHERE uid = ? ORDER BY borrowed_at DESC";
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, uid);
                ResultSet rs = pstmt.executeQuery();

                StringBuilder sb = new StringBuilder();
                sb.append("ID | Name | UID | Book Title | Library | Borrowed Date | Return Date\n");
                sb.append("-------------------------------------------------------------\n");
                while (rs.next()) {
                    sb.append(rs.getInt("id")).append(" | ")
                            .append(rs.getString("name")).append(" | ")
                            .append(rs.getString("uid")).append(" | ")
                            .append(rs.getString("book_title")).append(" | ")
                            .append(rs.getString("library")).append(" | ")
                            .append(rs.getString("borrowed_at")).append(" | ")
                            .append(rs.getString("return_date")).append("\n");
                }
                displayArea.setText(sb.toString());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    private void openReturnDialog() {
        String uid = JOptionPane.showInputDialog(this, "Enter UID to search for borrowed books:");
        if (uid == null || uid.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "UID cannot be empty!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ArrayList<String> borrowedBooks = new ArrayList<>();
        String sql = "SELECT id, book_title FROM borrowed_books WHERE uid = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uid);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                borrowedBooks.add(rs.getInt("id") + ": " + rs.getString("book_title"));
            }

            if (borrowedBooks.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No borrowed books found for UID: " + uid, "No Books", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] bookArray = borrowedBooks.toArray(new String[0]);
            String selectedBook = (String) JOptionPane.showInputDialog(
                    this,
                    "Select the book to return:",
                    "Return Book",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    bookArray,
                    bookArray[0]
            );

            if (selectedBook != null) {
                int bookId = Integer.parseInt(selectedBook.split(":")[0]);
                String deleteSql = "DELETE FROM borrowed_books WHERE id = ?";
                try (Connection connDel = connect(); PreparedStatement pstmtDel = connDel.prepareStatement(deleteSql)) {
                    pstmtDel.setInt(1, bookId);
                    pstmtDel.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Book returned successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error while returning book: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching borrowed books: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openStudentSearchDialog() {
        String uid = JOptionPane.showInputDialog(this, "Enter UID to search:");
        if (uid == null || uid.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "UID cannot be empty!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT name, book_title, borrowed_at, return_date FROM borrowed_books WHERE uid = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uid);
            ResultSet rs = pstmt.executeQuery();

            StringBuilder result = new StringBuilder();
            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                result.append("Name: ").append(rs.getString("name")).append("\n")
                        .append("Book: ").append(rs.getString("book_title")).append("\n")
                        .append("Borrowed On: ").append(rs.getString("borrowed_at")).append("\n")
                        .append("Return By: ").append(rs.getString("return_date")).append("\n")
                        .append("-----------------------------\n");
            }

            if (hasResults) {
                JTextArea textArea = new JTextArea(result.toString(), 15, 40);
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                JOptionPane.showMessageDialog(this, scrollPane, "Student Borrowed Books", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No borrowed books found for UID: " + uid, "Not Found", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching student data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Connection connect() {
    try {
        Class.forName("org.sqlite.JDBC");

        String url = "jdbc:sqlite:C:/Users/sharm/Desktop/VS CODE/Project_Java_ADBMS/lib/college_libraries.db";

        return DriverManager.getConnection(url);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Database connection error:\n" + e.getMessage());
        return null;
    }
}

    private void createTable() {
        Connection conn = connect();

        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed. Table not created.");
            return;
        }

        String sql = "CREATE TABLE IF NOT EXISTS borrowed_books (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "uid TEXT NOT NULL," +
                "book_title TEXT NOT NULL," +
                "library TEXT NOT NULL," +
                "borrowed_at TEXT," +
                "return_date TEXT" +
                ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to create table:\n" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CollegeLibrarySystem::new);
    }
}
