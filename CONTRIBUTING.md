# Contributing to kotlinx.newsboatparser

First of all, thanks for taking the time to contribute to the project!

Following is a basic set of guidelines for contributing to this repository and instructions to make it as easy as possible.

> Parts of this guidelines are taken from https://github.com/atom/atom/blob/master/CONTRIBUTING.md

#### Table of contents

[Code of Conduct](#code-of-conduct)
[Asking questions](#asking-questions)
[Styleguides](#styleguides)
 - [Commit message](#commit-messages)
 - [Lint](#lint)
[Additional info](#additional-info)

## Code of Conduct

Code of conduct is [available in the repository](CODE_OF_CONDUCT.md)

## Asking questions

For asking questions, please make sure to use [**Discussions**](https://github.com/esensar/kotlinx-newsboat-parser/discussions) instead of **Issues**.

## Styleguides

### Commit messages
 - Use the present tense ("Add feature" not "Added feature")
 - Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
 - Limit the first line to 72 characters or less
 - Reference issues and pull requests liberally after the first line
 - Project uses [Karma commit message format](http://karma-runner.github.io/6.0/dev/git-commit-msg.html)

### Lint

This project uses [ktlint](https://ktlint.github.io/), with [ktlint-gradle](https://github.com/JLLeitschuh/ktlint-gradle) plugin. This makes it easy to set up ktlint locally (ktlint is run on every pull request, together with full test suite).

To run ktlint locally, run:
```
./gradlew ktlintCheck
```

It is also possible to fix some of the issues automatically, by running:
```
./gradlew ktlintFormat
```

It is recommended to set up git pre-commit hook to format all changed files:
```
./gradlew addKtlintFormatGitPreCommitHook
```

It is also possible to do **only** check, instead of format:
```
./gradlew addKtlintCheckGitPreCommitHook
```

If using IntelliJ IDEA IDE, ktlint rules can also be applied to IntelliJ IDEA project:
```
./gradlew ktlintApplyToIdea
```

## Additional info

Since this project just parses newsboat configuration and urls format, make sure any changes don't break compatibility with official documentation: https://newsboat.org/releases/2.24/docs/newsboat.html#_newsboat_configuration_commands
