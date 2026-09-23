package com.example.feedback;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@WebServlet(urlPatterns = {"/", "/feedback", "/health", "/assets/*"})
public class PortalServlet extends HttpServlet {
    private final FeedbackStore store = new FeedbackStore();
    private final String revision = loadRevision();

    private static String loadRevision() {
        var properties = new java.util.Properties();
        try (var stream = PortalServlet.class.getResourceAsStream("/build.properties")) {
            if (stream != null) properties.load(stream);
        } catch (IOException e) { return "unknown"; }
        String value = properties.getProperty("revision", "local");
        return value.matches("[a-zA-Z0-9._-]{1,80}") ? value : "unknown";
    }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setHeader("Cache-Control", "no-store");
        if ("/assets".equals(req.getServletPath())) {
            String path = req.getPathInfo();
            if (path == null || !java.util.Set.of("/portal.css", "/message.svg", "/star.svg", "/arrow-right.svg").contains(path)) {
                res.sendError(404); return;
            }
            try (var asset = getServletContext().getResourceAsStream("/assets" + path)) {
                if (asset == null) { res.sendError(404); return; }
                res.setContentType(path.endsWith(".css") ? "text/css;charset=UTF-8" : "image/svg+xml");
                res.setHeader("X-Content-Type-Options", "nosniff");
                asset.transferTo(res.getOutputStream());
            }
            return;
        }
        if ("/health".equals(req.getServletPath())) {
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().print("{\"status\":\"UP\",\"application\":\"student-feedback\",\"version\":\"1.0.0\",\"revision\":\"" + revision + "\"}");
            return;
        }
        if (!"/".equals(req.getServletPath()) && !"/feedback".equals(req.getServletPath())) {
            res.sendError(404); return;
        }
        render(req, res, null);
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        req.setCharacterEncoding("UTF-8");
        if (!"/feedback".equals(req.getServletPath())) { res.sendError(405); return; }
        var session = req.getSession(false);
        if (session == null || req.getParameter("token") == null
                || !req.getParameter("token").equals(session.getAttribute("token"))) {
            res.sendError(403, "Reload the form and try again."); return;
        }
        try {
            int rating;
            try { rating = Integer.parseInt(req.getParameter("rating")); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Choose a rating from 1 to 5."); }
            store.add(new Feedback(req.getParameter("name"), req.getParameter("email"),
                    req.getParameter("message"), rating, Instant.now()));
            session.setAttribute("submitted", true);
            res.setStatus(303);
            res.setHeader("Location", req.getContextPath() + "/");
        } catch (IllegalArgumentException e) {
            res.setStatus(400); render(req, res, e.getMessage());
        } catch (IllegalStateException e) {
            res.setStatus(503); render(req, res, e.getMessage());
        }
    }

    static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }


    private void render(HttpServletRequest req, HttpServletResponse res, String error) throws IOException {
        var session = req.getSession();
        if (session.getAttribute("token") == null) session.setAttribute("token", UUID.randomUUID().toString());
        boolean submitted = Boolean.TRUE.equals(session.getAttribute("submitted"));
        session.removeAttribute("submitted");
        var records = store.all();
        String average = records.isEmpty() ? "—" : String.format(java.util.Locale.ROOT, "%.1f", records.stream().mapToInt(Feedback::rating).average().orElse(0));
        String base = escape(req.getContextPath());
        res.setContentType("text/html;charset=UTF-8");
        res.setHeader("Cache-Control", "no-store");
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("X-Frame-Options", "DENY");
        var out = res.getWriter();
        out.print("<!doctype html><html lang=\"en\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><meta name=\"theme-color\" content=\"#174c3c\"><title>Student Feedback Portal</title><link rel=\"stylesheet\" href=\"" + base + "/assets/portal.css\"></head><body>");
        out.print("""
            <a class="skip-link" href="#main">Skip to content</a>
            <header class="site-header"><div class="wrap brand"><strong>Campus</strong><span class="slash">/</span>Student voice</div></header>
            <main id="main" class="wrap"><h1>Your experience matters.</h1>
            <p class="intro">Share what worked, what could improve, and what you’d like to see next.</p>
            """);
        if (submitted) out.print("<div class=\"notice\" role=\"status\">Thank you! Your feedback has been submitted successfully.</div>");
        if (error != null) out.print("<div class=\"notice error\" role=\"alert\">" + escape(error) + "</div>");
        out.print("<section class=\"stats\" aria-label=\"Feedback summary\"><div class=\"stat\"><div class=\"icon-circle\"><img class=\"icon\" src=\"" + base + "/assets/message.svg\" alt=\"\"></div><div><strong>" + records.size() + "</strong><span>" + (records.size() == 1 ? "submission" : "submissions") + "</span></div></div><div class=\"stat\"><div class=\"icon-circle\"><img class=\"icon\" src=\"" + base + "/assets/star.svg\" alt=\"\"></div><div><strong>" + average + "</strong><span>average rating / 5</span></div></div></section>");
        out.print("<div class=\"grid\"><section class=\"form-section\" aria-labelledby=\"form-heading\"><h2 id=\"form-heading\">Share your feedback</h2><p class=\"muted\" id=\"form-help\">All fields are required. Use demo details only.</p><form method=\"post\" aria-describedby=\"form-help\" action=\"" + base + "/feedback\">");
        out.print("<input type=\"hidden\" name=\"token\" value=\"" + escape((String) session.getAttribute("token")) + "\">");
        out.print("<div class=\"field\"><label for=\"name\">Name</label><input type=\"text\" id=\"name\" name=\"name\" placeholder=\"Enter your name\" autocomplete=\"name\" maxlength=\"80\" required value=\"" + escape(error == null ? "" : req.getParameter("name")) + "\"></div>");
        out.print("<div class=\"field\"><label for=\"email\">Email</label><input id=\"email\" name=\"email\" type=\"email\" placeholder=\"Enter your email\" autocomplete=\"email\" maxlength=\"160\" required value=\"" + escape(error == null ? "" : req.getParameter("email")) + "\"></div>");
        out.print("<fieldset><legend>Overall experience</legend><div class=\"rating-options\">");
        String[] labels = {"Poor", "Fair", "Good", "Very good", "Excellent"};
        for (int i = 1; i <= 5; i++) {
            out.print("<label class=\"rating-choice\"><input type=\"radio\" name=\"rating\" value=\"" + i + "\" required" + (error != null && String.valueOf(i).equals(req.getParameter("rating")) ? " checked" : "") + "><span class=\"rating-tile\"><strong>" + i + "</strong><span>" + labels[i - 1] + "</span></span></label>");
        }
        out.print("</div></fieldset><div class=\"field\"><label for=\"message\">Your feedback</label><textarea id=\"message\" name=\"message\" placeholder=\"Tell us about your experience...\" maxlength=\"1000\" required>" + escape(error == null ? "" : req.getParameter("message")) + "</textarea></div><button type=\"submit\">Submit feedback<img src=\"" + base + "/assets/arrow-right.svg\" alt=\"\"></button></form></section>");
        out.print("<section class=\"voices\" aria-labelledby=\"voices-heading\"><h2 id=\"voices-heading\">Student voices</h2><p class=\"muted\">Latest submissions first. Email addresses are kept off this page.</p><div class=\"feed\">");
        if (records.isEmpty()) out.print("<div class=\"empty\"><strong>Start the conversation.</strong><p>No feedback yet. Share your experience and help make the next one better.</p></div>");
        var dateFormat = java.time.format.DateTimeFormatter.ofPattern("dd MMM uuuu", java.util.Locale.ENGLISH).withZone(java.time.ZoneOffset.UTC);
        for (Feedback item : records) out.print("<article><div class=\"feedback-heading\"><strong>" + escape(item.name()) + "</strong><span class=\"rating\">" + item.rating() + " / 5</span></div><p>" + escape(item.message()) + "</p><time datetime=\"" + item.createdAt() + "\" title=\"Submission date (UTC)\">" + dateFormat.format(item.createdAt()) + "</time></article>");
        out.print("</div></section></div><footer class=\"site-footer\"><span>Demo data resets on restart or redeployment.</span><span class=\"brand\"><strong>Campus</strong> / Student voice</span></footer></main></body></html>");
    }
}
