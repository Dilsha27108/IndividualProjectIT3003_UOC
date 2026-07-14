package com.library.service;

import com.library.exception.LibraryException;
import com.library.exception.LibraryException.*;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Transaction;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core business logic for the library: catalog/member management,
 * issuing and returning books with overdue fine calculation, a
 * reservation waitlist, stream-based reporting, and CSV file persistence.
 */
public class Library {
    private final Map<String, Book> books = new LinkedHashMap<>();
    private final Map<String, Member> members = new LinkedHashMap<>();
    private final Map<String, Transaction> activeLoans = new HashMap<>();       // bookId -> loan transaction
    private final Map<String, Queue<String>> waitlists = new HashMap<>();       // bookId -> queue of memberIds
    private final List<Transaction> history = new ArrayList<>();

    // ---------------- Catalog & member management ----------------

    public void addBook(Book book) throws LibraryException {
        if (books.containsKey(book.getBookId())) {
            throw new DuplicateIdException("A book with ID " + book.getBookId() + " already exists.");
        }
        books.put(book.getBookId(), book);
    }

    public void removeBook(String bookId) throws LibraryException {
        Book book = getBookOrThrow(bookId);
        if (!book.isAvailable()) {
            throw new BookUnavailableException("Cannot remove \"" + book.getTitle() + "\" - it is currently borrowed.");
        }
        books.remove(bookId);
    }

    public void registerMember(Member member) throws LibraryException {
        if (members.containsKey(member.getMemberId())) {
            throw new DuplicateIdException("A member with ID " + member.getMemberId() + " already exists.");
        }
        members.put(member.getMemberId(), member);
    }

    public List<Book> getAllBooks() { return new ArrayList<>(books.values()); }
    public List<Member> getAllMembers() { return new ArrayList<>(members.values()); }

    // ---------------- Borrow / return / reserve ----------------

    public void issueBook(String bookId, String memberId, LocalDate today) throws LibraryException {
        Book book = getBookOrThrow(bookId);
        Member member = getMemberOrThrow(memberId);

        if (!book.isAvailable()) {
            throw new BookUnavailableException("Book \"" + book.getTitle() + "\" is already borrowed.");
        }
        if (!member.canBorrowMore()) {
            throw new BorrowLimitExceededException(member.getName() + " (" + member.getType()
                    + ") has reached their borrowing limit of " + member.getType().getMaxBooksAllowed() + ".");
        }

        book.setAvailable(false);
        member.addBorrowedBook(bookId);
        LocalDate dueDate = today.plusDays(member.getType().getLoanPeriodDays());
        Transaction loan = new Transaction(bookId, memberId, Transaction.Type.BORROW, today, dueDate);
        activeLoans.put(bookId, loan);
        history.add(loan);
    }

    /** Returns a book and reports the fine owed (0.0 if returned on time). */
    public double returnBook(String bookId, String memberId, LocalDate today) throws LibraryException {
        Book book = getBookOrThrow(bookId);
        Member member = getMemberOrThrow(memberId);
        Transaction loan = activeLoans.remove(bookId);

        if (loan == null || !loan.getMemberId().equals(memberId)) {
            throw new LibraryException(member.getName() + " does not currently hold \"" + book.getTitle() + "\".");
        }

        long overdueDays = loan.daysOverdue(today);
        double fine = overdueDays * member.getType().getFinePerDay();

        book.setAvailable(true);
        member.removeBorrowedBook(bookId);

        Transaction returnRecord = new Transaction(bookId, memberId, Transaction.Type.RETURN, today, null);
        returnRecord.setFineCharged(fine);
        history.add(returnRecord);

        notifyNextInWaitlist(bookId);
        return fine;
    }

    /** Joins a member to a book's waitlist when the book is currently unavailable. */
    public void reserveBook(String bookId, String memberId, LocalDate today) throws LibraryException {
        Book book = getBookOrThrow(bookId);
        getMemberOrThrow(memberId);

        if (book.isAvailable()) {
            throw new LibraryException("\"" + book.getTitle() + "\" is available now - no need to reserve.");
        }
        waitlists.computeIfAbsent(bookId, k -> new LinkedList<>()).add(memberId);
        history.add(new Transaction(bookId, memberId, Transaction.Type.RESERVE, today, null));
    }

    private void notifyNextInWaitlist(String bookId) {
        Queue<String> queue = waitlists.get(bookId);
        if (queue != null && !queue.isEmpty()) {
            String nextMemberId = queue.poll();
            Member member = members.get(nextMemberId);
            if (member != null) {
                System.out.println(">> Notice: " + member.getName() + " is next in line and has been notified.");
            }
        }
    }

    // ---------------- Reporting (streams + a small generic helper) ----------------

    public List<Book> searchByTitle(String keyword) {
        String lower = keyword.toLowerCase();
        return books.values().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    /** Generic helper: sorts any list with a given comparator without mutating the original. */
    public static <T> List<T> sortedCopy(Collection<T> items, Comparator<T> comparator) {
        List<T> copy = new ArrayList<>(items);
        copy.sort(comparator);
        return copy;
    }

    public List<Book> sortBooksByTitle() {
        return sortedCopy(books.values(), Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));
    }

    public List<Book> sortBooksByYearDescending() {
        return sortedCopy(books.values(), Comparator.comparingInt(Book::getPublicationYear).reversed());
    }

    public List<Transaction> getOverdueLoans(LocalDate today) {
        return activeLoans.values().stream()
                .filter(t -> t.daysOverdue(today) > 0)
                .collect(Collectors.toList());
    }

    public Map<String, Long> countBooksByGenre() {
        return books.values().stream()
                .collect(Collectors.groupingBy(b -> b.getGenre().name(), Collectors.counting()));
    }

    public List<Transaction> getHistory() { return new ArrayList<>(history); }

    // ---------------- File persistence (CSV) ----------------

    public void saveBooksToFile(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Book book : books.values()) {
                writer.write(book.toCsv());
                writer.newLine();
            }
        }
    }

    public void loadBooksFromFile(String filePath) throws IOException, LibraryException {
        Path path = Path.of(filePath);
        if (!Files.exists(path)) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) addBook(Book.fromCsv(line));
            }
        }
    }

    public void saveMembersToFile(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Member member : members.values()) {
                writer.write(member.toCsv());
                writer.newLine();
            }
        }
    }

    public void loadMembersFromFile(String filePath) throws IOException, LibraryException {
        Path path = Path.of(filePath);
        if (!Files.exists(path)) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) registerMember(Member.fromCsv(line));
            }
        }
    }

    // ---------------- Private helpers ----------------

    private Book getBookOrThrow(String bookId) throws NotFoundException {
        Book book = books.get(bookId);
        if (book == null) throw new NotFoundException("No book found with ID " + bookId);
        return book;
    }

    private Member getMemberOrThrow(String memberId) throws NotFoundException {
        Member member = members.get(memberId);
        if (member == null) throw new NotFoundException("No member found with ID " + memberId);
        return member;
    }
}
