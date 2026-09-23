# Git workflow

- `main`: stable application; deploy from here once the Jenkins pipeline is configured.
- `dev`: integrate tested changes before merging into main.
- `feature/<name>`: short-lived work branched from dev.

Example future change:

```bash
git switch dev
git switch -c feature/improve-feedback-validation
# Make a focused change and run Maven tests.
git add src
git commit -m "feat: improve feedback validation"
git switch dev
git merge --no-ff feature/improve-feedback-validation
# Validate the integrated build before promoting.
git switch main
git merge --no-ff dev
```

Use `feat:`, `fix:`, `ci:`, `docs:`, and `test:` prefixes for clear commit messages.
Keep real incremental history from this point forward. Do not manufacture historical
commits for work that was already completed before Git initialization.

Generated WAR files, build output, and backup artifacts are excluded from Git.
Jenkins will archive the build artifacts. Never commit credentials or tokens.
