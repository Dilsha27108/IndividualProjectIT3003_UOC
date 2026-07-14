package com.library.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Records a borrow, return, or reservation event. When type is BORROW,
 * the due date is stored so an overdue fine can be computed on return.
 */
public class Transaction {

    /** Kind of event being recorded. */
    public enum Type { BORROW, RETURN, RESERVE }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String bookId;
    private final String memberId;
    private final Type type;
    private final LocalDate date;
    private final LocalDate dueDate; // only meaningful for BORROW
    private double fineCharged;      // only meaningful for RETURN

    public Transaction(String bookId, String memberId, Type type, LocalDate date, LocalDate dueDate) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.type = type;
        this.date = date;
        this.dueDate = dueDate;
    }

    public String getBookId() { return bookId; }
    public String getMemberId() { return memberId; }
    public Type getType() { return type; }
    public LocalDate getDate() { return date; }
    public LocalDate getDueDate() { return dueDate; }
    public double getFineCharged() { return fineCharged; }
    public void setFineCharged(double fineCharged) { this.fineCharged = fineCharged; }

    /** Days overdue as of the given date (0 if not overdue or no due date set). */
    public long daysOverdue(LocalDate asOf) {
        if (dueDate == null) return 0;
        long diff = ChronoUnit.DAYS.between(dueDate, asOf);
        return Math.max(diff, 0);
    }

    @Override
    public String toString() {
        String base = String.format("%s | %-7s | Book: %s | Member: %s",
                date.format(FORMATTER), type, bookId, memberId);
        if (type == Type.BORROW && dueDate != null) {
            base += " | Due: " + dueDate.format(FORMATTER);
        }
        if (type == Type.RETURN && fineCharged > 0) {
            base += String.format(" | Fine: $%.2f", fineCharged);
        }
        return base;
    }
}
