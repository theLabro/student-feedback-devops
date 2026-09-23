# Jenkins CI/CD setup

## Architecture

GitHub main -> Jenkins checkout -> Maven compile -> unit tests -> WAR package ->
archive -> Tomcat deploy -> health check for the exact Git revision.

Jenkins polls GitHub every two minutes using `H/2 * * * *`. It builds only when
the configured branch changes. This works while Windows, WSL, and Jenkins are running;
GitHub cannot directly call localhost, so no public webhook or tunnel is required.

## Host prerequisites

Ubuntu 24.04, Jenkins running with Java 21, JDK 17 at
`/usr/lib/jvm/java-17-openjdk-amd64`, Maven, Git, Python 3, curl, unzip, sudo, flock,
and Tomcat 10 on port 8081. Application builds use JDK 17 independently of Jenkins's runtime.

Install the reviewed helper once from the repository:

```bash
sudo bash scripts/install-deployment-helper.sh
```

It installs root-owned helper files and a narrow sudo rule allowing Jenkins to
invoke deploy, accept, and rollback as the Tomcat user. It grants no root shell,
service-management privileges, or general-purpose sudo. GitHub is public and
deployment is local, so neither action needs a stored password. If private Git
access is later needed, use a Jenkins credential ID; never embed a token in Git.

## Job configuration

- Job name **student-feedback-devops** (required by the fixed deployment path).
- Type: Pipeline.
- Definition: Pipeline script from SCM.
- SCM: Git; URL https://github.com/theLabro/student-feedback-devops.git.
- Branch specifier: `*/main`.
- Script path: `Jenkinsfile`; lightweight checkout enabled.
- Run Build Now once to load the pipeline and register polling.

The small local certification demo runs on the built-in Jenkins node. Production
systems should use isolated agents. The job must use its default Linux workspace,
`/var/lib/jenkins/workspace/student-feedback-devops`; no custom workspace or concurrent builds.

## Quality gates and evidence

Failed Maven tests stop packaging and deployment. Missing WAR fails packaging.
Jenkins publishes JUnit reports, fingerprints and archives the WAR, and saves
commit details, WAR checksum and successful health response as build artifacts.
Builds have a 20-minute overall timeout; 15 logs and 10 sets of artifacts are retained.

Every WAR includes its Git revision in `WEB-INF/classes/build.properties`, exposed
by `/health`. Verification waits for that revision, avoiding a false success from
the previous application while Tomcat reloads. A pending-deployment marker survives
interruption. Concurrent pipeline builds are disabled and helper operations use a lock.

Any unsuccessful run after deployment starts attempts rollback. The helper restores
the previous WAR and verifies its revision before clearing the pending marker.
The original pre-pipeline WAR is recognized as revision `local`.

## Recovery

If a deployment is interrupted or rollback fails, read the console log and Tomcat logs.
Then recover using:

```bash
sudo -u tomcat /usr/local/sbin/student-feedback-release rollback
curl --fail http://localhost:8081/student-feedback/health
```

Do not clear the pending marker manually to hide failed recovery. If Tomcat itself
is stopped, restore the service first. The helper requires a healthy baseline WAR
before deploying; this project already has one from the manual validation stage.

Deployments reset in-memory feedback; WAR rollback restores application code, not data.
Changing helper source in Git does not automatically update its installed root-owned
copy: review changes and re-run the installer explicitly.
