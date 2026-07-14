package com.library.exception;

/**
 * Base checked exception for all library domain errors. More specific
 * problems are represented as nested static subclasses below, so callers
 * can catch precisely the failure they care about while everything still
 * lives in this one file, matching the required project structure.
 */
public class LibraryException extends Exception {

    public LibraryException(String message) {
        super(message);
    }

    /** Thrown when a book or member ID does not exist. */
    public static class NotFoundException extends LibraryException {
        public NotFoundException(String message) { super(message); }
    }

    /** Thrown when trying to borrow a book that is already out. */
    public static class BookUnavailableException extends LibraryException {
        public BookUnavailableException(String message) { super(message); }
    }

    /** Thrown when a member has reached their borrowing limit. */
    public static class BorrowLimitExceededException extends LibraryException {
        public BorrowLimitExceededException(String message) { super(message); }
    }

    /** Thrown when a duplicate ID is registered for a book or member. */
    public static class DuplicateIdException extends LibraryException {
        public DuplicateIdException(String message) { super(message); }
    }
}
