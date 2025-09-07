import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

class Book {
    String id, title, author;
    boolean issued;

    Book(String id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.issued = false;
    }
}

public class LibraryManagementSystem extends JFrame {
    ArrayList<Book> books = new ArrayList<>();
    DefaultTableModel model;

    JTextField idField, titleField, authorField;

    LibraryManagementSystem() {
        setTitle("Library Management System");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Layout
        JPanel panel = new JPanel(new BorderLayout());

        // Table
        String[] cols = {"ID", "Title", "Author", "Issued"};
        model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Input Fields
        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        idField = new JTextField();
        titleField = new JTextField();
        authorField = new JTextField();
        inputPanel.add(new JLabel("Book ID:"));
        inputPanel.add(idField);
        inputPanel.add(new JLabel("Title:"));
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("Author:"));
        inputPanel.add(authorField);
        panel.add(inputPanel, BorderLayout.NORTH);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton addBtn = new JButton("Add Book");
        JButton issueBtn = new JButton("Issue Book");
        JButton returnBtn = new JButton("Return Book");
        JButton deleteBtn = new JButton("Delete Book");

        buttonPanel.add(addBtn);
        buttonPanel.add(issueBtn);
        buttonPanel.add(returnBtn);
        buttonPanel.add(deleteBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);

        // Button Actions
        addBtn.addActionListener(e -> addBook());
        issueBtn.addActionListener(e -> issueBook(table));
        returnBtn.addActionListener(e -> returnBook(table));
        deleteBtn.addActionListener(e -> deleteBook(table));
    }

    void addBook() {
        String id = idField.getText().trim();
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();

        if (id.isEmpty() || title.isEmpty() || author.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }

        Book b = new Book(id, title, author);
        books.add(b);
        model.addRow(new Object[]{b.id, b.title, b.author, b.issued ? "Yes" : "No"});
        idField.setText(""); titleField.setText(""); authorField.setText("");
    }

    void issueBook(JTable table) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            Book b = books.get(row);
            if (!b.issued) {
                b.issued = true;
                model.setValueAt("Yes", row, 3);
                JOptionPane.showMessageDialog(this, "Book Issued!");
            } else {
                JOptionPane.showMessageDialog(this, "Book already issued!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a book to issue!");
        }
    }

    void returnBook(JTable table) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            Book b = books.get(row);
            if (b.issued) {
                b.issued = false;
                model.setValueAt("No", row, 3);
                JOptionPane.showMessageDialog(this, "Book Returned!");
            } else {
                JOptionPane.showMessageDialog(this, "Book was not issued!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a book to return!");
        }
    }

    void deleteBook(JTable table) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            books.remove(row);
            model.removeRow(row);
            JOptionPane.showMessageDialog(this, "Book Deleted!");
        } else {
            JOptionPane.showMessageDialog(this, "Select a book to delete!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryManagementSystem().setVisible(true));
    }
}
