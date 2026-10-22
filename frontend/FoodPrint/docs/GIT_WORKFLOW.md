# FoodPrint Git Workflow

This document describes branch management, commit conventions, and the Code Review process for team collaboration.

---

## 1. Branch Conventions

| Branch | Description |
|------|------|
| `master` / `main` | Main branch containing reviewed, releasable changes; **do not edit code directly here** |
| `feature/xxx` | New features, such as `feature/login`, `feature/camera-scan` |
| `fix/xxx` | Bug fixes, such as `fix/crash-on-android` |
| `refactor/xxx` | Refactoring, such as `refactor/navigation` |

---

## 2. Daily Workflow

### 1. Pull the Latest Code

Sync the main branch before starting work:

```bash
git checkout master
git pull origin master
```

### 2. Create a Feature Branch

Create a new branch from the latest `master`:

```bash
git checkout -b feature/your-feature-name
```

Example:

```bash
git checkout -b feature/profile-edit
```

### 3. Develop and Commit

Work on the branch and make small, semantic commits:

```bash
# View changes
git status
git diff

# Stage files to commit (do not commit node_modules, .env, etc.)
git add src/screens/ProfileScreen.tsx
# or stage all changes: git add .

# Commit (use a concise imperative sentence)
git commit -m "feat(profile): Add profile editing screen"
```

**Suggested commit message format:**

- `feat(module): description` — new feature
- `fix(module): description` — bug fix
- `refactor(module): description` — refactoring
- `docs: description` — documentation
- `style: description` — formatting or styling without logic changes

### 4. Push to Remote and Open a PR

When pushing the branch for the first time:

```bash
git push -u origin feature/your-feature-name
```

For later changes on the same branch:

```bash
git push
```

Then go to **GitHub**: 

1. Open the repository page and click the "Compare & pull request" banner.
2. Alternatively, go to **Branches** → find your branch → **New pull request**.
3. Set **Base** to `master` and **Compare** to your branch.
4. Enter a PR title and description covering changes, testing, and any screenshots or recordings.
5. Assign a teammate as Reviewer and click **Create pull request**.

### 5. Code Review

- **Reviewer:** inspect the diff in **Files changed**, click "+" next to a line to comment, then select **Review changes** → **Approve** or **Request changes** and add a summary.
- **Author:** address comments and push more commits to the same branch; the PR updates automatically. Request another review once discussions are resolved.

### 6. Merge and Cleanup

- Merge on GitHub with **Merge pull request**, or use **Squash and merge** to combine commits.
- Delete the remote branch after merging using the GitHub **Delete branch** prompt.
- Switch back to the main branch locally and delete the merged feature branch:

```bash
git checkout master
git pull origin master
git branch -d feature/your-feature-name
```

---

## 3. Common Command Reference

| Operations | Command |
|------|------|
| View the current branch | `git branch` |
| View status and uncommitted changes | `git status`, `git diff` |
| Create and switch to a branch | `git checkout -b feature/xxx` |
| Switch branches | `git checkout master` |
| Stage all changes | `git add .` |
| Commit | `git commit -m "Message"` |
| Push the current branch | `git push` or `git push -u origin branch-name` |
| Pull the current branch | `git pull` |
| Discard unstaged working tree changes | `git checkout -- filename` |
| View commit history | `git log --oneline` |

---

## 4. Pre-commit Checks

Before merging into `master`, run these checks locally:

```bash
pnpm lint
pnpm format:check
pnpm start   # Confirm it runs correctly
```

If issues occur:

```bash
pnpm lint:fix
pnpm format
```

Then run `git add` and `git commit` and push, reducing formatting comments during review.

---

## 5. Optional: Protect the Main Branch (GitHub)

To require PRs and reviews for changes to the main branch:

1. Repository **Settings** → **Branches**; 
2. Select **Add branch protection rule** and enter `master` or `main` as the branch name.
3. Enable **Require a pull request before merging** and optionally **Require approvals** (for example, 1).
4. Save. Direct pushes to `master` are blocked; changes must go through a PR and review.

The shared team workflow becomes: create branch → commit → PR → Code Review → merge.
