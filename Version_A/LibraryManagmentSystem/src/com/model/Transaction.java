package com.library.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    public enum Type { BORROW, RETURN }

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String bookId;
    private final String memberId;
    private final Type type;
    private final LocalDateTime timestamp;

    public Transaction(String bookId, String memberId, Type type) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("%s | %s | Book: %s | Member: %s",
                timestamp.format(FORMATTER), type, bookId, memberId);
    }
}
