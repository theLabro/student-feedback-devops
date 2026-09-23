package com.example.feedback;

import java.util.ArrayList;
import java.util.List;

/** Demo-only storage. Records are cleared when the application reloads. */
public class FeedbackStore {
    private final List<Feedback> records = new ArrayList<>();
    public synchronized void add(Feedback feedback) {
        if (records.size() >= 1000) throw new IllegalStateException("The demo is full. Restart the application to clear submissions.");
        records.add(0, feedback);
    }
    public synchronized List<Feedback> all() { return List.copyOf(records); }
}
