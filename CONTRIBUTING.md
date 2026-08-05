# Contributing to Occtax-mobile

Thank you for your interest in contributing to **Occtax-mobile**! 🎉

This repository contains the Android mobile application for the
[_Occtax_](https://github.com/PnX-SI/GeoNature) module of the
[_GeoNature_](https://github.com/PnX-SI/GeoNature) biodiversity data management platform.
This application allows field naturalists to record taxon observations directly on the ground. It
relies on [gn_mobile_maps](https://github.com/PnX-SI/gn_mobile_maps) to display observations on an
interactive map - including support for embedded offline tile layers (MBTiles / GeoJSON) when no
network is available - and on [gn_mobile_core](https://github.com/PnX-SI/gn_mobile_core) for data
synchronization with the _GeoNature_ server.

Every contribution, whether you're a developer or not, helps improve the project.

---

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Non-Developer Contributions](#non-developer-contributions)
  - [Reporting a Bug](#reporting-a-bug)
  - [Suggesting a New Feature or Improvement](#suggesting-a-new-feature-or-improvement)
  - [Developer Contributions](#developer-contributions)
- [Setting Up the Development Environment](#setting-up-the-development-environment)
- [Branching Strategy](#branching-strategy)
- [Submitting a Pull Request](#submitting-a-pull-request)
- [Coding Standards](#coding-standards)
- [Testing](#testing)
- [Documentation](#documentation)
- [Commit Message Guidelines](#commit-message-guidelines)

---

## Code of Conduct

This project is open and welcoming to everyone. By participating, you agree to maintain a respectful
and constructive environment. Please be kind, inclusive, and professional in all interactions.

---

## How Can I Contribute?

### Non-Developer Contributions

You don't need to write code to contribute! Here are ways you can help:

- **Report bugs** you encounter while using the application in the field or in the lab.
- **Suggest improvements** to existing features or the documentation.
- **Propose new features** that would be useful to the naturalist community.
- **Improve documentation**, fix typos, clarify explanations, add examples.
- **Share feedback**, tell us what works well and what doesn't from a field usage perspective.
- **Translate** documentation or UI strings to another language.

All these contributions are made through **GitHub Issues** (see below).

---

### Reporting a Bug

If you find a bug, please [open a new issue](../../issues/new/choose) and select the **Bug Report**
template.

Before opening a new issue, please:

1. **Search existing issues** to avoid duplicates - your bug may already be reported.
2. Make sure you are using the latest available version of the application.

A good bug report should include:

| Field                  | What to provide                                                                              |
|------------------------|----------------------------------------------------------------------------------------------|
| **Title**              | A short, descriptive title                                                                   |
| **Description**        | A clear description of the unexpected behavior                                               |
| **Steps to reproduce** | Step-by-step instructions to reproduce the bug                                               |
| **Expected behavior**  | What you expected to happen                                                                  |
| **Actual behavior**    | What actually happened                                                                       |
| **Screenshots / logs** | Any relevant screenshot, stack trace, or log output                                          |
| **Environment**        | Android version, device model, application version, _GeoNature_ server version               |
| **Settings**           | Relevant excerpt of `settings_occtax.json` (redact any sensitive URL or credentials)         |
| **Additional context** | Any other relevant information (connectivity conditions, offline layers used, taxon list…)   |

> **Tip:** The more detail you provide, the easier it is for maintainers to reproduce and fix the
> issue. Bugs observed in specific field conditions (no network, large taxon list, etc.) are
> especially helpful to document precisely.

---

### Suggesting a New Feature or Improvement

Have an idea to make Occtax-mobile better? We'd love to hear it!

Please [open a new issue](../../issues/new/choose) and select the appropriate template:

- **Feature Request** - for a brand-new capability (e.g. support for a new map layer format, a new
  input step, batch import of observations…).
- **Improvement / Enhancement** - for an improvement of an existing feature (e.g. better offline
  map performance, improved taxon search, refined observation form UX…).

A good feature request should include:

| Field                       | What to provide                                         |
|-----------------------------|---------------------------------------------------------|
| **Title**                   | A concise title                                         |
| **Problem / motivation**    | Why is this feature needed? What problem does it solve? |
| **Proposed solution**       | Describe your idea in as much detail as possible        |
| **Alternatives considered** | Have you considered other approaches?                   |
| **Additional context**      | Mockups, links to similar implementations, references…  |

> **Note:** Feature requests are not guaranteed to be implemented. The maintainers will review them
> and prioritize according to the project roadmap. Features that require changes in
> [gn_mobile_core](https://github.com/PnX-SI/gn_mobile_core) or
> [gn_mobile_maps](https://github.com/PnX-SI/gn_mobile_maps) may need to be opened in those
> repositories as well.

---

### Developer Contributions

If you want to contribute code, please follow the workflow described in the sections below.

---

## Setting Up the Development Environment

### Prerequisites

- **Android Studio** (latest stable release recommended)
- **JDK 17** or later
- **Android SDK** with API level 29+ installed
- A GitHub account with access to GitHub Packages (for dependencies)

### Fork & Clone

1. **Fork** the repository by clicking the *Fork* button on GitHub.
2. **Clone** your fork locally:

   ```bash
   git clone https://github.com/<your-username>/gn_mobile_occtax.git
   cd gn_mobile_occtax
   ```

3. Add the upstream remote:

   ```bash
   git remote add upstream https://github.com/PnX-SI/gn_mobile_occtax.git
   ```

### Configure GitHub Packages Access

This project depends on libraries published to GitHub Packages
([gn_mobile_maps](https://github.com/PnX-SI/gn_mobile_maps) and
[gn_mobile_core](https://github.com/PnX-SI/gn_mobile_core)). You must provide your GitHub
credentials in `local.properties` (at the root of the project):

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_PERSONAL_ACCESS_TOKEN
```

> Generate a Personal Access Token (PAT) with `read:packages` scope in
> *GitHub Settings -> Developer settings -> Personal access tokens*.

> **Important:** Never commit `local.properties` to version control - it is already listed in
> `.gitignore`.

### Build the Project

```bash
./gradlew clean assembleDebug
```

The project contains the following module:

| Module    | Description                                                                                                                       |
|-----------|-----------------------------------------------------------------------------------------------------------------------------------|
| `occtax`  | The main application module - observation input form, taxon list, map display (offline & online layers), data synchronization UI  |

### Run on a Device or Emulator

Connect an Android device (API 29+) or start an emulator, then:

```bash
./gradlew :occtax:installDebug
```

---

## Branching Strategy

| Branch               | Purpose                                             |
|----------------------|-----------------------------------------------------|
| `master`             | Latest stable release                               |
| `develop`            | Integration branch - base your work here            |
| `feat/<short-name>`  | New feature (e.g. `feat/offline-geojson-layer`)     |
| `fix/<short-name>`   | Bug fix (e.g. `fix/crash-on-empty-taxon-list`)      |
| `docs/<short-name>`  | Documentation update                                |
| `chore/<short-name>` | Build, tooling, dependency updates                  |

Always branch off `develop` (or `master` for hotfixes):

```bash
git fetch upstream
git checkout -b feat/my-feature upstream/develop
```

---

## Submitting a Pull Request

1. **Sync your fork** with the upstream before starting:

   ```bash
   git fetch upstream
   git rebase upstream/develop
   ```

2. Implement your changes, following the [coding standards](#coding-standards) below.

3. **Run all tests** and make sure they pass:

   ```bash
   ./gradlew test
   ```

4. **Push** your branch to your fork:

   ```bash
   git push origin feat/my-feature
   ```

5. Open a **Pull Request** against the `develop` branch of the upstream repository.

6. Fill in the PR template:
   - Reference the related issue(s) with `Closes #<issue-number>`.
   - Describe *what* you changed and *why*.
   - Include screenshots or screen recordings if the change affects the UI or the map.
   - Mention if the change requires an update of `settings_occtax.json` or the documentation.

7. A maintainer will review your PR. Be prepared to address feedback and push additional commits.

> **Note:** Small, focused PRs are much easier to review than large ones. When in doubt, split your
> work into multiple smaller PRs.

---

## Coding Standards

This project is written in **Kotlin** and follows the official
[Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).

- Use **4-space indentation** (no tabs).
- Keep lines under **120 characters**.
- Write **self-documenting code**: use meaningful names for variables, functions, and classes.
- Add **KDoc comments** to all public API elements (classes, functions, properties).
- Avoid introducing new dependencies unless strictly necessary; discuss in an issue first.
- Dependency injection is handled via **Hilt**, follow existing patterns.
- Prefer **coroutines** to callbacks for asynchronous operations.
- The UI follows the **MVVM** pattern with `ViewModel` + `LiveData` / `StateFlow`; follow
  existing patterns.
- Map-related code must go through the abstractions provided by
  [gn_mobile_maps](https://github.com/PnX-SI/gn_mobile_maps) - do not add direct mapping library
  calls in the application code.

---

## Testing

- All new features and bug fixes **must** include unit tests.
- Tests are located in the `src/test/` directory of the `occtax` module.
- The project uses **JUnit 4**, [**MockK**](https://mockk.io), and [**Robolectric**](https://robolectric.org).
- Run all tests with:

  ```bash
  ./gradlew test
  ```

- Run tests for the `occtax` module only:

  ```bash
  ./gradlew :occtax:test
  ```

---

## Documentation

- Keep the `README.md` up to date with your changes.
- If you add or modify a settings parameter, update the **Parameters description** table in
  `README.md`.
- If you modify the observation input workflow, update `docs/input_workflow.adoc` and the
  corresponding PlantUML diagram in `docs/uml/`.
- Use [PlantUML](https://plantuml.com) diagrams where appropriate to illustrate flows or
  architecture.
- User-facing documentation changes (installation guide, user guide) are maintained in
  `docs/installation-fr.adoc` and `docs/utilisation-fr.adoc` (in French).

---

## Commit Message Guidelines

This project follows the [Conventional Commits](https://www.conventionalcommits.org/)
specification.

```
<type>(<scope>): <short description>

[optional body]

[optional footer: Closes #<issue-number>]
```

### Types

| Type       | When to use                                          |
|------------|------------------------------------------------------|
| `feat`     | A new feature                                        |
| `fix`      | A bug fix                                            |
| `docs`     | Documentation changes only                           |
| `style`    | Code style changes (formatting, missing semicolons…) |
| `refactor` | Code refactoring without feature change or bug fix   |
| `perf`     | Performance improvements                             |
| `test`     | Adding or updating tests                             |
| `chore`    | Build process, tooling, or dependency updates        |

### Scopes (non-exhaustive)

| Scope      | Area of the application                               |
|------------|-------------------------------------------------------|
| `input`    | Observation input form and steps                      |
| `taxa`     | Taxon list, search and filters                        |
| `map`      | Map display, offline layers, POI management           |
| `sync`     | Data synchronization with the _GeoNature_ server      |
| `settings` | Application settings parsing and management           |
| `auth`     | Authentication and login                              |

Or directly reference the issue number from GitHub.

### Examples

```
feat(taxa): add boolean operator support in full-text taxon search

Closes #87

fix(map): prevent crash when MBTiles file is missing at startup

Closes #102

docs: update offline layers configuration in README.md

chore: bump gn_mobile_maps dependency to 1.5.0

feat(#91): full-text query string support about searching taxa`
```

---

## Questions?

If you have any question that is not covered here, feel free to
[open a discussion](../../issues/new) or reach out via the existing issue tracker.

Thank you for helping make Occtax-mobile better! 🌿

