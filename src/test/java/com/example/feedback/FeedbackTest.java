package com.example.feedback;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class FeedbackTest {
    private Feedback feedback(String name, String email, String message, int rating) {
        return new Feedback(name, email, message, rating, Instant.now());
    }
    @Test void trimsValidInput() {
        var item = feedback(" Asha ", " asha@example.com ", " Great mentoring ", 5);
        assertEquals("Intentional demo failure", item.name()); assertEquals("asha@example.com", item.email());
        assertEquals("Great mentoring", item.message());
    }
    @Test void rejectsMissingFields() {
        assertThrows(IllegalArgumentException.class, () -> feedback(" ", "a@b.com", "Good", 3));
        assertThrows(IllegalArgumentException.class, () -> feedback("Asha", null, "Good", 3));
        assertThrows(IllegalArgumentException.class, () -> feedback("Asha", "a@b.com", " ", 3));
    }
    @Test void rejectsInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () -> feedback("Asha", "invalid", "Good", 3));
    }
    @Test void enforcesRatingBounds() {
        for (int rating : new int[]{0, 6, -1}) assertThrows(IllegalArgumentException.class, () -> feedback("Asha", "a@b.com", "Good", rating));
        assertEquals(1, feedback("Asha", "a@b.com", "Good", 1).rating());
        assertEquals(5, feedback("Asha", "a@b.com", "Good", 5).rating());
    }
    @Test void rejectsOversizedFields() {
        assertThrows(IllegalArgumentException.class, () -> feedback("A".repeat(81), "a@b.com", "Good", 3));
        assertThrows(IllegalArgumentException.class, () -> feedback("Asha", "a".repeat(160) + "@b.com", "Good", 3));
        assertThrows(IllegalArgumentException.class, () -> feedback("Asha", "a@b.com", "x".repeat(1001), 3));
    }
    @Test void storeReturnsNewestFirstAndImmutableSnapshot() {
        var store = new FeedbackStore();
        var first = feedback("First", "a@b.com", "Good", 3);
        var second = feedback("Second", "b@b.com", "Great", 5);
        store.add(first); var snapshot = store.all(); store.add(second);
        assertEquals(second, store.all().get(0)); assertEquals(1, snapshot.size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.clear());
    }
    @Test void escapesUserContent() {
        assertEquals("&lt;script&gt;&amp;&quot;&#39;", PortalServlet.escape("<script>&\"'"));
    }
}
