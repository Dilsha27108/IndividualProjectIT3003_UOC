package com.library;

import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Transaction;
import com.library.service.Library;

import java.util.List;
import java.util.Scanner;

/**
 * Entry point (index file) for the Library Management System.
 * Wires together the model and service classes and provides a console menu.
 */
public class Main {

    private static final Library library = new Library();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadSampleData();
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> addBookFlow();
                case "2" -> registerMemberFlow();
                case "3" -> issueBookFlow();
                case "4" -> returnBookFlow();
                case "5" -> printAllBooks();
                case "6" -> printAllMembers();
                case "7" -> searchBookFlow();
                case "8" -> printHistory();
                case "0" -> {
                    running = false;
                    System.out.println("Goodbye!");
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n===== LIBRARY MANAGEMENT SYSTEM =====");
        System.out.println("1. Add Book");
        System.out.println("2. Register Member");
        System.out.println("3. Issue Book");
        System.out.println("4. Return Book");
        System.out.println("5. View All Books");
        System.out.println("6. View All Members");
        System.out.println("7. Search Book by Title");
        System.out.println("8. View Transaction History");
        System.out.println("0. Exit");
        System.out.print("Select an option: ");
    }

    private static void addBookFlow() {
        System.out.print("Book ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Author: ");
        String author = scanner.nextLine().trim();
        System.out.print("ISBN: ");
        String isbn = scanner.nextLine().trim();

        try {
            library.addBook(new Book(id, title, author, isbn));
            System.out.println("Book added successfully.");
        } catch (LibraryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void registerMemberFlow() {
        System.out.print("Member ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        try {
            library.registerMember(new Member(id, name, email));
            System.out.println("Member registered successfully.");
        } catch (LibraryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void issueBookFlow() {
        System.out.print("Book ID: ");
        String bookId = scanner.nextLine().trim();
        System.out.print("Member ID: ");
        String memberId = scanner.nextLine().trim();

        try {
            library.issueBook(bookId, memberId);
            System.out.println("Book issued successfully.");
        } catch (LibraryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void returnBookFlow() {
        System.out.print("Book ID: ");
        String bookId = scanner.nextLine().trim();
        System.out.print("Member ID: ");
        String memberId = scanner.nextLine().trim();

        try {
            library.returnBook(bookId, memberId);
            System.out.println("Book returned successfully.");
        } catch (LibraryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void printAllBooks() {
        List<Book> allBooks = library.getAllBooks();
        if (allBooks.isEmpty()) {
            System.out.println("No books in the catalog.");
            return;
        }
        allBooks.forEach(System.out::println);
    }

    private static void printAllMembers() {
        List<Member> allMembers = library.getAllMembers();
        if (allMembers.isEmpty()) {
            System.out.println("No members registered.");
            return;
        }
        allMembers.forEach(System.out::println);
    }

    private static void searchBookFlow() {
        System.out.print("Enter title keyword: ");
        String keyword = scanner.nextLine().trim();
        List<Book> results = library.searchByTitle(keyword);
        if (results.isEmpty()) {
            System.out.println("No matching books found.");
        } else {
            results.forEach(System.out::println);
        }
    }

    private static void printHistory() {
        List<Transaction> history = library.getHistory();
        if (history.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        history.forEach(System.out::println);
    }

    /**
     * Preloads a few books and members so the menu has data to work with immediately.
     */
    private static void loadSampleData() {
        try {
            library.addBook(new Book("B001", "Clean Code", "Robert C. Martin", "9780132350884"));
            library.addBook(new Book("B002", "Effective Java", "Joshua Bloch", "9780134685991"));
            library.addBook(new Book("B003", "Design Patterns", "Erich Gamma et al.", "9780201633610"));

            library.registerMember(new Member("M001", "Anjali Perera", "anjali@example.com"));
            library.registerMember(new Member("M002", "Kasun Silva", "kasun@example.com"));
        } catch (LibraryException e) {
            System.out.println("Error loading sample data: " + e.getMessage());
        }
    }
}
