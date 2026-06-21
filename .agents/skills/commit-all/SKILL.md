---
name: commit-all
description: Group changes into semantic commits and push
---

Group all current changes into semantic commits and push the changes to the remote repository.

## Rules:

- First inspect the full repository state:
  - `git status --short`
  - `git diff --stat`
  - `git diff`
  - `git log --oneline -10`
- Identify related file groups by intent: feature, fix, refactor, tests, docs, chore or config.
- Create multiple commits when there are independent changes. Do not mix unrelated changes into the same commit.
- Use clear, semantic, concise commit messages that follow the [Conventional Commits](https://www.conventionalcommits.org/) specification. Do not create commit messages with several lines. One descriptive line is more than enough.
- Before commiting, check for sensitive or suspicious files (`.env`, tokens, credentials, keys, secrets...). If any are found, stop and ask.
- Include new, modified and deleted files that belong to each group.
- Do not revert existing changes.
- Do not use `--no-verify`.
- Do not ammend commits.
- Do not force push.
- Do NOT add yourself as co-author. Never append a `Co-Authored-By: Claude ...` trailer (or any other co-author trailer for the executing model) to commit messages. Commit messages must contain only the semantic description — no co-author lines, no "Generated with Claude Code" footers, no agent attribution of any kind.
- Do not include commit body explaining the changes.

## Flow

1. Show the proposed commit plan with the files included in each commit.
2. If the grouping is clear, continue. If there is some ambiguity, ask for clarification before proceeding.
3. For each group:
  - Add only the files that belong to this group with `git add <files>`.
  - Create the commit with a semantic commit message.
4. Once all commits have been created, wait for the user to approve the commits before pushing with `git push`.
5. When finished, summarize the commits created and the changes pushed to the remote repository.
