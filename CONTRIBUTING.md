# Contributor guide

## About this document

This guide is for people who would like to be involved in building Whitefox.

## How can I help?

Whitefox follows a standard
[fork and pull](https://help.github.com/articles/using-pull-requests/)
model for contributions via GitHub pull requests.

Software development guidelines that we employ can be found [here](docs/development_guidelines.md).

Below is a list of the steps that might be involved in an ideal
contribution. If you don't have the time to go through every step,
contribute what you can, and someone else will probably be happy to
follow up with any polishing that may need to be done.

If you want to touch up some documentation or fix typos, feel free to
skip these steps and jump straight to submitting a pull request.

1. open/find an issue that you want to address
2. open a PR:
   1. reference the original issue
   2. address the issue
   3. add tests
   4. add documentation
3. mention a committer (@tmnd1991) and ask for a review


# IDE setup

To get started with Whitefox development, you need JDK 17 installed. Since the project is managed with Gradle, any 
IDE that works with Gradle v8.3 will be a good fit. We internally use IntelliJ IDEA Community Edition.

# Protocol

The protocol is developed using openapi 3.0 and a textual description in markdwon format.

We copy-paste delta-sharing openapi protocol from the [original repo](https://github.com/delta-io/delta-sharing/blob/main/delta-sharing-protocl-api-description.yml) and the same happens for the [textual representation](https://github.com/delta-io/delta-sharing/blob/main/PROTOCOL.md).

Both protocols are checked on every push by spectral using `.spectral.yaml` ruleset, they are also deployed as github pages.
