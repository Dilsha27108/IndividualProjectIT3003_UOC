package com.library;

import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.Book.Genre;
import com.library.model.Member;
import com.library.model.Member.MemberType;
import com.library.model.Transaction;
import com.library.service.Library;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Entry point (index file) for the advanced Library Management System.
 * Imports and coordinates the model, service, and exception classes below.
 */
public class Main {

    private static final String BOOKS_FILE = "books.csv";
    private static final String MEMBERS_FILE = "members.csv";

    private static final Library library = new Library();

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadFromDiskOrSeed();
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> addBookFlow();
                    case "2" -> registerMemberFlow();
                    case "3" -> issueBookFlow();
                    case "4" -> returnBookFlow();
                    case "5" -> printAllBooks();
                    case "6" -> printAllMembers();
                    case "7" -> searchBookFlow();
                    case "8" -> reserveBookFlow();
                    case "9" -> printOverdueReport();
                    case "10" -> printGenreReport();
                    case "11" -> sortBooksFlow();
                    case "12" -> printHistory();
                    case "13" -> saveToDisk();
                    case "0" -> {
                        running = false;
                        saveToDisk();
                        System.out.println("Data saved. Goodbye!");
                    }
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } catch (LibraryException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n===== ADVANCED LIBRARY MANAGEMENT SYSTEM =====");
        System.out.println(" 1. Add Book");
        System.out.println(" 2. Register Member (Student/Faculty)");
        System.out.println(" 3. Issue Book");
        System.out.println(" 4. Return Book (calculates fine if overdue)");
        System.out.println(" 5. View All Books");
        System.out.println(" 6. View All Members");
        System.out.println(" 7. Search Book by Title");
        System.out.println(" 8. Reserve Book (join waitlist)");
        System.out.println(" 9. View Overdue Report");
        System.out.println("10. View Book Count by Genre");
        System.out.println("11. Sort Books (title / year)");
        System.out.println("12. View Transaction History");
        System.out.println("13. Save Data to Disk");
        System.out.println(" 0. Exit (auto-saves)");
        System.out.print("Select an option: ");
    }

    private static void addBookFlow() throws LibraryException {
        System.out.print("Book ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Author: ");
        String author = scanner.nextLine().trim();
        System.out.print("ISBN: ");
        String isbn = scanner.nextLine().trim();
        int year = readInt("Publication Year: ");

        System.out.print("Genre " + java.util.Arrays.toString(Genre.values()) + ": ");
        Genre genre;
        try {
            genre = Genre.valueOf(scanner.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            genre = Genre.OTHER;
        }

        library.addBook(new Book.Builder()
                .bookId(id).title(title).author(author).isbn(isbn)
                .genre(genre).publicationYear(year)
                .build());
        System.out.println("Book added successfully.");
    }

    private static void registerMemberFlow() throws LibraryException {
        System.out.print("Member ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Type (1=Student, 2=Faculty): ");
        String type = scanner.nextLine().trim();

        MemberType memberType = type.equals("2") ? MemberType.FACULTY : MemberType.STUDENT;
        library.registerMember(new Member(id, name, email, memberType));
        System.out.println("Member registered as " + memberType + ".");
    }

    private static void issueBookFlow() throws LibraryException {
        System.out.print("Book ID: ");
        String bookId = scanner.nextLine().trim();
        System.out.print("Member ID: ");
        String memberId = scanner.nextLine().trim();
        library.issueBook(bookId, memberId, LocalDate.now());
        System.out.println("Book issued successfully.");
    }

    private static void returnBookFlow() throws LibraryException {
        System.out.print("Book ID: ");
        String bookId = scanner.nextLine().trim();
        System.out.print("Member ID: ");
        String memberId = scanner.nextLine().trim();
        double fine = library.returnBook(bookId, memberId, LocalDate.now());
        if (fine > 0) {
            System.out.printf("Book returned. Overdue fine due: $%.2f%n", fine);
        } else {
            System.out.println("Book returned on time. No fine.");
        }
    }

    private static void reserveBookFlow() throws LibraryException {
        System.out.print("Book ID: ");
        String bookId = scanner.nextLine().trim();
        System.out.print("Member ID: ");
        String memberId = scanner.nextLine().trim();
        library.reserveBook(bookId, memberId, LocalDate.now());
        System.out.println("Reservation added - member will be notified when the book is returned.");
    }

    private static void printAllBooks() {
        List<Book> books = library.getAllBooks();
        if (books.isEmpty()) { System.out.println("No books in the catalog."); return; }
        books.forEach(System.out::println);
    }

    private static void printAllMembers() {
        List<Member> members = library.getAllMembers();
        if (members.isEmpty()) { System.out.println("No members registered."); return; }
        members.forEach(System.out::println);
    }

    private static void searchBookFlow() {
        System.out.print("Enter title keyword: ");
        String keyword = scanner.nextLine().trim();
        List<Book> results = library.searchByTitle(keyword);
        if (results.isEmpty()) System.out.println("No matching books found.");
        else results.forEach(System.out::println);
    }

    private static void printOverdueReport() {
        List<Transaction> overdue = library.getOverdueLoans(LocalDate.now());
        if (overdue.isEmpty()) { System.out.println("No overdue books. Nice!"); return; }
        overdue.forEach(t -> System.out.println(t + " | Days overdue: " + t.daysOverdue(LocalDate.now())));
    }

    private static void printGenreReport() {
        Map<String, Long> counts = library.countBooksByGenre();
        counts.forEach((genre, count) -> System.out.println(genre + ": " + count));
    }

    private static void sortBooksFlow() {
        System.out.print("Sort by (1=Title, 2=Year newest-first): ");
        String choice = scanner.nextLine().trim();
        List<Book> sorted = choice.equals("2") ? library.sortBooksByYearDescending() : library.sortBooksByTitle();
        sorted.forEach(System.out::println);
    }

    private static void printHistory() {
        List<Transaction> history = library.getHistory();
        if (history.isEmpty()) { System.out.println("No transactions yet."); return; }
        history.forEach(System.out::println);
    }

    private static void saveToDisk() {
        try {
            library.saveBooksToFile(BOOKS_FILE);
            library.saveMembersToFile(MEMBERS_FILE);
        } catch (IOException e) {
            System.out.println("Warning: could not save data - " + e.getMessage());
        }
    }

    /** Loads books/members from CSV files if present; otherwise seeds sample data. */
    private static void loadFromDiskOrSeed() {
        try {
            library.loadBooksFromFile(BOOKS_FILE);
            library.loadMembersFromFile(MEMBERS_FILE);
            if (!library.getAllBooks().isEmpty() || !library.getAllMembers().isEmpty()) {
                System.out.println("Loaded existing data from disk.");
                return;
            }
        } catch (IOException | LibraryException e) {
            System.out.println("Could not load saved data (" + e.getMessage() + "). Starting with sample data.");
        }
        seedSampleData();
    }

    private static void seedSampleData() {
        try {
            library.addBook(new Book.Builder().bookId("B001").title("Clean Code").author("Robert C. Martin")
                    .isbn("9780132350884").genre(Genre.TECHNOLOGY).publicationYear(2008).build());
            library.addBook(new Book.Builder().bookId("B002").title("Sapiens").author("Yuval Noah Harari")
                    .isbn("9780062316097").genre(Genre.HISTORY).publicationYear(2011).build());
            library.addBook(new Book.Builder().bookId("B003").title("Dune").author("Frank Herbert")
                    .isbn("9780441013593").genre(Genre.FICTION).publicationYear(1965).build());

            library.registerMember(new Member("M001", "Anjali Perera", "anjali@example.com", MemberType.STUDENT));
            library.registerMember(new Member("M002", "Dr. Kasun Silva", "kasun@example.com", MemberType.FACULTY));
        } catch (LibraryException e) {
            System.out.println("Error seeding sample data: " + e.getMessage());
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }
}
