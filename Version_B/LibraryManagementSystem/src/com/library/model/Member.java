package com.library.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a library member. Instead of separate subclasses per member
 * type, a nested {@link MemberType} enum drives the borrowing rules
 * (limit, loan period, fine rate) - a lightweight strategy-by-enum approach
 * that keeps everything in one file.
 */
public class Member {

    /** Type of member, each with its own borrowing rules. */
    public enum MemberType {
        STUDENT(3, 14, 0.50),
        FACULTY(8, 30, 1.00);

        private final int maxBooksAllowed;
        private final int loanPeriodDays;
        private final double finePerDay;

        MemberType(int maxBooksAllowed, int loanPeriodDays, double finePerDay) {
            this.maxBooksAllowed = maxBooksAllowed;
            this.loanPeriodDays = loanPeriodDays;
            this.finePerDay = finePerDay;
        }

        public int getMaxBooksAllowed() { return maxBooksAllowed; }
        public int getLoanPeriodDays() { return loanPeriodDays; }
        public double getFinePerDay() { return finePerDay; }
    }

    private final String memberId;
    private final String name;
    private final String email;
    private final MemberType type;
    private final List<String> borrowedBookIds = new ArrayList<>();

    public Member(String memberId, String name, String email, MemberType type) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.type = type;
    }

    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public MemberType getType() { return type; }
    public List<String> getBorrowedBookIds() { return borrowedBookIds; }

    public void addBorrowedBook(String bookId) { borrowedBookIds.add(bookId); }
    public void removeBorrowedBook(String bookId) { borrowedBookIds.remove(bookId); }

    public boolean canBorrowMore() {
        return borrowedBookIds.size() < type.getMaxBooksAllowed();
    }

    /** Serializes this member to one CSV line for file persistence. */
    public String toCsv() {
        return String.join(",", memberId, name, email, type.name());
    }

    /** Recreates a Member from a CSV line produced by {@link #toCsv()}. */
    public static Member fromCsv(String line) {
        String[] p = line.split(",", -1);
        return new Member(p[0], p[1], p[2], MemberType.valueOf(p[3]));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) - %s - Borrowed: %d/%d",
                memberId, name, email, type, borrowedBookIds.size(), type.getMaxBooksAllowed());
    }
}
