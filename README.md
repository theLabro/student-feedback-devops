# Student Feedback Portal

Java 17 student feedback application built with Maven and deployed to Tomcat 10.1 through Jenkins.

## Features

- Validated feedback form with a five-point rating.
- Recent feedback, submission count, and average rating.
- HTML escaping and session-based form protection; email addresses are not shown publicly.
- `/health` reports status, version, and deployed Git revision.
- In-memory storage resets on restart or redeployment.

## Build and run

Requires Ubuntu, Java 17, Maven, Git, and Tomcat 10.1.

```bash
git clone https://github.com/theLabro/student-feedback-devops.git
cd student-feedback-devops
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
mvn clean verify
sudo install -m 0644 target/student-feedback.war /var/lib/tomcat10/webapps/student-feedback.war
```

With Tomcat configured on port 8081, open http://localhost:8081/student-feedback/.
Health endpoint: http://localhost:8081/student-feedback/health.
These addresses require the application to be running locally.

## Jenkins pipeline

Checkout → Build → Test → Package → Archive → Deploy → Verify.

The `student-feedback-devops` job reads `Jenkinsfile` from GitHub `main` and polls
for changes every two minutes. Failed tests stop later stages. Jenkins archives
the WAR and build evidence, then checks the deployed revision before accepting
the release. The deployment helper retains the previous WAR for recovery.

Jenkins uses Java 21 in this environment; application builds use Java 17.
Deployment also requires Python 3, sudo, unzip, and flock. Install the helper
with `sudo bash scripts/install-deployment-helper.sh` after establishing a healthy
initial deployment. The job uses its default workspace at
`/var/lib/jenkins/workspace/student-feedback-devops` and narrowly scoped permission
to invoke the helper as the Tomcat user.

## Project files

- `src/main/`: application code, resources, and interface assets.
- `src/test/`: JUnit tests.
- `pom.xml`: Maven build and dependencies.
- `Jenkinsfile`: CI/CD pipeline.
- `scripts/`: deployment, recovery, health verification, and helper installation.

Tabler SVG icons use the MIT license included with the assets.
Dataset not required; primary project input is source-code changes.
