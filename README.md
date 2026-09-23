# Student Feedback Portal

Certification project: Java 17 Maven WAR application for Tomcat 10.1.

## Design

The updated interface follows the selected green-and-cream concept: open two-column
layout, sage summary strip, accessible five-choice rating group, and readable dates.
Below 680px, the form and feedback feed stack vertically. There is no JavaScript dependency.

- Styling: `src/main/webapp/assets/portal.css`
- Page markup and form handling: `src/main/java/com/example/feedback/PortalServlet.java`
- Icons: Tabler Icons (MIT), bundled locally with `src/main/webapp/assets/tabler-LICENSE.txt`.
- Verified redesign preview: http://localhost:8081/student-feedback-preview/
- Preview uses a separate in-memory store from the original application.
- Main application (redesign deployed): http://localhost:8081/student-feedback/

Rebuild and deploy `student-feedback.war` using the steps below to update the original
application URL. Redeployment clears the original application's in-memory submissions.

## Features

- Feedback form with server-side name, email, message, and rating validation.
- Confirmation after submission, newest-first list, count, and average rating.
- Email addresses are not displayed in the list; user text is HTML-escaped.
- Session token protects form submissions. This is an unauthenticated local demo.
- `/health` returns HTTP 200 with JSON status, application name, and version.
- In-memory storage (up to 1,000 submissions) resets on restart/redeployment.

Dataset not required; primary project input is source-code changes.
Use fictional details in demos. Do not expose this classroom demo as a public production service.

## Build in Ubuntu

```bash
mkdir -p ~/projects/student-feedback-portal
cp -r /mnt/d/Developement/DevOps/student-feedback-portal/src ~/projects/student-feedback-portal/
cp /mnt/d/Developement/DevOps/student-feedback-portal/pom.xml ~/projects/student-feedback-portal/
cd ~/projects/student-feedback-portal
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
mvn clean verify
```

Output: `target/student-feedback.war`. Test reports: `target/surefire-reports/`.
Build on Ubuntu's Linux filesystem to avoid WAR packaging permission issues on Windows mounts.
The Servlet API is provided by Tomcat rather than bundled inside the WAR.

## Initial manual deployment

This validates the application before we add automated Jenkins deployment.

```bash
sudo install -m 0644 target/student-feedback.war /var/lib/tomcat10/webapps/student-feedback.war
curl --fail http://localhost:8081/student-feedback/health
```

Allow Tomcat a few seconds to deploy. Open http://localhost:8081/student-feedback/.
Submit feedback and confirm the success message, list, count, and average update.

## Remaining certification work

Git remote and branch history; Jenkins checkout/build/test/package/archive/deploy/verify;
deployment credentials and permissions; rollback automation and demo; successful and
failed run evidence; final documentation and 5–10 minute explanation video.
These are not completed merely by building the application.
