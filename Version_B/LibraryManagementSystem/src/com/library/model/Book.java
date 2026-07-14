package com.library.model;

import java.util.Objects;

/**
 * Represents a book in the catalog.
 * Uses a nested Builder so optional fields (genre, year) don't require
 * a telescoping constructor, and a nested Genre enum to categorize books.
 */
public class Book {

    /** Category of a book. Nested here so no extra file is needed. */
    public enum Genre {
        FICTION, NON_FICTION, SCIENCE, TECHNOLOGY, HISTORY, BIOGRAPHY, OTHER
    }

    private final String bookId;
    private final String title;
    private final String author;
    private final String isbn;
    private final Genre genre;
    private final int publicationYear;
    private boolean available;

    private Book(Builder builder) {
        this.bookId = builder.bookId;
        this.title = builder.title;
        this.author = builder.author;
        this.isbn = builder.isbn;
        this.genre = builder.genre;
        this.publicationYear = builder.publicationYear;
        this.available = true;
    }

    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public Genre getGenre() { return genre; }
    public int getPublicationYear() { return publicationYear; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        return bookId.equals(((Book) o).bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId);
    }

    @Override
    public String toString() {
        return String.format("[%s] \"%s\" by %s (%d, %s) - %s",
                bookId, title, author, publicationYear, genre, available ? "Available" : "Borrowed");
    }

    /** Serializes this book to one CSV line for file persistence. */
    public String toCsv() {
        return String.join(",", bookId, title, author, isbn, genre.name(),
                String.valueOf(publicationYear), String.valueOf(available));
    }

    /** Recreates a Book from a CSV line produced by {@link #toCsv()}. */
    public static Book fromCsv(String line) {
        String[] p = line.split(",", -1);
        Book book = new Builder()
                .bookId(p[0]).title(p[1]).author(p[2]).isbn(p[3])
                .genre(Genre.valueOf(p[4])).publicationYear(Integer.parseInt(p[5]))
                .build();
        book.setAvailable(Boolean.parseBoolean(p[6]));
        return book;
    }

    /** Builder for {@link Book}. */
    public static class Builder {
        private String bookId;
        private String title;
        private String author;
        private String isbn;
        private Genre genre = Genre.OTHER;
        private int publicationYear;

        public Builder bookId(String v) { this.bookId = v; return this; }
        public Builder title(String v) { this.title = v; return this; }
        public Builder author(String v) { this.author = v; return this; }
        public Builder isbn(String v) { this.isbn = v; return this; }
        public Builder genre(Genre v) { this.genre = v; return this; }
        public Builder publicationYear(int v) { this.publicationYear = v; return this; }

        public Book build() {
            if (bookId == null || title == null || author == null) {
                throw new IllegalStateException("bookId, title, and author are required.");
            }
            return new Book(this);
        }
    }
}
