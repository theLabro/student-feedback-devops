package com.example.feedback;

import java.time.Instant;

public record Feedback(String name, String email, String message, int rating, Instant createdAt) {
    public Feedback {
        name = required(name, "Name", 80);
        email = required(email, "Email", 160);
        message = required(message, "Feedback", 1000);
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating must be between 1 and 5.");
        if (createdAt == null) throw new IllegalArgumentException("Submission time is required.");
    }

    private static String required(String value, String label, int max) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " is required.");
        String trimmed = value.strip();
        if (trimmed.length() > max) throw new IllegalArgumentException(label + " must be at most " + max + " characters.");
        return trimmed;
    }
}
