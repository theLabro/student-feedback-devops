# Student Feedback Portal

Certification project: Java 17 Maven WAR application for Tomcat 10.1.

## Project links

- Source and submission link: https://github.com/theLabro/student-feedback-devops
- Portal on the demonstration machine: http://localhost:8081/student-feedback/
- Jenkins on the demonstration machine: http://localhost:8080/job/student-feedback-devops/

The localhost links work only on the machine running the project. GitHub hosts
the source code; it does not run this Java application. GitHub Pages supports
static sites and cannot execute this WAR or its feedback and health endpoints.

## CI/CD workflow

GitHub `main` → Checkout → Build → Test → Package → Archive → Deploy → Verify.

Jenkins polls GitHub every two minutes while the local environment is running.
A change on `main`, including a README change, should start a build automatically.
The WAR embeds its Git commit, and the health check must return that same revision
before a deployment is accepted. Failed tests stop deployment. The deployment
helper retains the previous WAR for recovery if verification fails.

Build #1 successfully ran every stage with seven passing tests. Its
[console log](docs/evidence/jenkins-build-1-success.txt) records the result.
Automatic triggering and failure/recovery demonstrations are being validated
separately; the first manually started build alone does not prove those behaviors.

See [Jenkins setup](docs/jenkins-setup.md) and [Git workflow](docs/git-workflow.md).

## Design

The updated interface follows the selected green-and-cream concept: open two-column
layout, sage summary strip, accessible five-choice rating group, and readable dates.
Below 680px, the form and feedback feed stack vertically. There is no JavaScript dependency.

- Styling: `src/main/webapp/assets/portal.css`
- Page markup and form handling: `src/main/java/com/example/feedback/PortalServlet.java`
- Icons: Tabler Icons (MIT), bundled locally with `src/main/webapp/assets/tabler-LICENSE.txt`.
- Main application (redesign deployed): http://localhost:8081/student-feedback/

Rebuild and deploy `student-feedback.war` using the steps below to update the original
application URL. Redeployment clears the original application's in-memory submissions.

## Features

- Feedback form with server-side name, email, message, and rating validation.
- Confirmation after submission, newest-first list, count, and average rating.
- Email addresses are not displayed in the list; user text is HTML-escaped.
- Session token protects form submissions. This is an unauthenticated local demo.
- `/health` returns HTTP 200 with JSON status, application name, version, and build revision.
- In-memory storage (up to 1,000 submissions) resets on restart/redeployment.

Dataset not required; primary project input is source-code changes.
Use fictional details in demos. Do not expose this classroom demo as a public production service.

## Build in Ubuntu

```bash
mkdir -p ~/projects
git clone https://github.com/theLabro/student-feedback-devops.git ~/projects/student-feedback-portal
cd ~/projects/student-feedback-portal
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
mvn clean verify
```

Output: `target/student-feedback.war`. Test reports: `target/surefire-reports/`.
Build on Ubuntu's Linux filesystem to avoid WAR packaging permission issues on Windows mounts.
The Servlet API is provided by Tomcat rather than bundled inside the WAR.

## Initial manual deployment

This establishes a healthy baseline before enabling automated Jenkins deployment.

```bash
sudo install -m 0644 target/student-feedback.war /var/lib/tomcat10/webapps/student-feedback.war
curl --fail http://localhost:8081/student-feedback/health
```

Allow Tomcat a few seconds to deploy. Open http://localhost:8081/student-feedback/.
Submit feedback and confirm the success message, list, count, and average update.

## Remaining certification work

Capture an automatically triggered build, a failed-test deployment gate, and a
verified rollback/recovery demonstration. Include the resulting evidence and a
5–10 minute walkthrough video with the repository submission. Feedback is stored
in memory: rollback restores application code, not previously submitted feedback.
