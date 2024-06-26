# Development guidelines

# Getting started

To get started you need:

- JDK 17 installed

alternatively a recent IntelliJ IDEA Community Edition version is enough.

The project is built using [Gradle](https://gradle.org), the build is defined using Gradle Kotlin DSL. If you never used
Gradle I kindly suggest to start from [its awesome docs](https://gradle.org/guides/#getting-started).

## Server

The server is [quarkus](https://quarkus.io) application that exposes a REST API implementing Whitefox and 
delta-sharing protocol. If you never worked with Quarkus, have a look at [its awesome doc](https://quarkus.io/get-started/).

The app is developed and run for JVM 17, even if hadoop libraries (which are a dependency of delta-lake kernel) are 
not certified to run on JDK > 11 we are not encountering any issue so far.

As soon as you clone the project you should verify that you are able to build and test locally, to do so you need to 
run the `check` command of Gradle, you can achieve that using either `gradlew` script in the project root (`.
/gradlew check`) or run the same [gradle task from intellij](https://www.jetbrains.com/help/idea/work-with-gradle-tasks.html).
If you're default jvm is not version 17, you can run `gradlew` passing another java home as follows: 
`./gradlew -Dorg.gradle.java.home=<PATH_TO_JAVA_HOME> build`.

Sometimes IntelliJ will tell you have build errors, especially when moving from one branch to the other. The problem 
might be that the auto-generated code (we generate stubs from openapi) is out of date. A simple `compileJava` will 
fix it.

Before every push you ~~can~~ should run locally `./gradlew devCheck` that will both reformat the code and run the 
unit tests.

You can run the server locally with the command: `gradlew server:quarkusDev`

### Windows

Some hadoop related tests do not run on Windows (especially in the Windows CI) so you can find the disabled using junit5
annotation `@DisabledOnOs(WINDOWS)`. If you want to provide a fix for them, feel free to open a PR.

## Code formatting

We use spotless to format and check the style of Java code. Spotless can be run through Gradle: 

- to reformat: `./gradlew spotlessApply`
- to check formatting: `./gradlew spotlessCheck`

## Protocol

 Whitefox protocol and original delta-sharing are kept under [`protocol`](protocols). We use openapi 
 specification to generate code for server and client. Furthermore, the protocol itself is validated using [spectral]
 (https://stoplight.io/open-source/spectral), you can find spectral configuration at `.spectral.yaml`.
 

# Software Engineering guidelines

Some golden rules that we use in this repo:

- always prefer immutability, this helps writing correct concurrent code  
- use constructor-based dependency injection when possible (i.e. do not annotate with `@Inject` any field) this 
  makes testing beans easy without a CDI framework (that makes unit test much faster)
- the "core" code should [make illegal states unrepresentable](https://khalilstemmler.com/articles/typescript-domain-driven-design/make-illegal-states-unrepresentable/)
- never use `null`, use `Optional` to convey optionality, ignore IntelliJ "warning" when passing `Optional` to methods
- throw exceptions and write beautiful error messages
- use `@QuarkusTest` annotation only when you really need the server to be running, otherwise write a "simple" unit 
  test
- don't use `@SessionScoped` it will make our life miserable when trying to scale horizontally
- [use `@ApplicationScoped` everywhere, `@Singleton` when `@ApplicationScoped` does not fit](https://quarkus.io/guides/cdi#applicationscoped-and-singleton-look-very-similar-which-one-should-i-choose-for-my-quarkus-application)
- de-couple different layers of the application as much as possible:
  - REST API classes should deal with auto-generated model classes, convert them (when needed) to the "core" 
    representation and pass it to "services"
  - services should not know anything about the outside world (i.e. REST API) and should deal with "core" models 
    that do not depend on any given serialization format
  - persistence is its own world too, if needed we can create "persistence-related" classes to use ORM tools but 
    these should not interfere in any way with the core ones
  - we will hand-craft (for now, who knows in the future) a lot of `mappers` that are function that go from one 
    "layer" to another (REST API/persistence/core are all layers)

For example, you can see the `DeltaSharesApiImpl` class, it deals with jakarta annotations to define http endpoints, 
it deals with errors thrown by the underlying (core) service and transforms those errors into 500/400 status codes 
and related http response and performs "protocol adaptation" which is transform http requests into core objects, 
then invokes the underlying core service and transforms the output of the service into a http response. `DeltaSharesApiImpl`
has no business logic, it performs only "mappings" between the internal core model (made of methods, exceptions 
classes) to the external world made of json payloads and http response/requests.

On the other hand `DeltaSharesServiceImpl` is business logic, it knows nothing about http, therefore it does not 
depend on any auto-generated code from the openapi spec.

## Documentation

Project documentation is built as a [docusaurus](https://docusaurus.io) microsite.

The doc is published during github actions only from `main` branch, the workflow is configured in `build_doc.yaml`.

To build/test documentation locally you can/should use dear old Gradle. You don't need node or npm installed locally,
to "serve" the documentation server locally you can simply issue:

```
./gradlew docsite:npm_run_start
```

this will start a server on port 3000 on localhost where you can preview the result. When you kill
the gradle terminal (with ctrl+c) the node process will be killed automatically by the custom gradleTask 
`killAllDocusaurus` (that you can also run manually through `./gradlew docsite:killAllDocusaurus`).

The *only* thing that will differ on the published site is that the `protocol` is copied to `docsite/static/protocol` 
in order to have a "working" swagger UI. If you want to reproduce the same locally and have a working swagger UI at 
`https://localhost:3000/whitefox/openapi_whitefox` and `https://localhost:3000/whitefox/openapi_delta-sharing.html` you can
create a symlink as follows: `ln -sfn $PWD/protocol $PWD/docsite/static/protocol`

## Testing

Unit tests must be fast therefore:
- they should not rely on any external service
- they should not need the server to be up running to be executed

If the test you are writing complies with the previous points, go ahead and create a simple junit5/jupiter test.

Otherwise, the project currently features two other type of tests:

- `aws`: tests tagged with `aws` tag require aws connectivity and credentials provided by the user as environment 
  variables or `.env` file
- `integration`: tests tagged with `integration` tag require the server to be up and running. Therefore, every time
  a test is annotated with `@QuarkusTest` it should be also annotated as `@Tag("integration")`.

At the current state all tests are run during the `check` task, but if you want to run only unit tests you can run
the `test` task, while if you want to run integration and aws tests you should run `integrationTest` task.

`core` module should not have any `@QuarkusTest` but can have `@Tag("integration")` tests, while `app` module can have
`@QuarkusTest` tests. `app` module should also feature integration "black-box" tests, that are annotated with 
`@QuarkusIntegrationTest`. They are run using `testNative` gradle task and spin up the application (from the jar 
package produced by `build` task) and run the test. They are useful to test the application in a "production-like" 
manner.

We do not use [test profiles](https://quarkus.io/guides/getting-started-testing#testing_different_profiles) because
they suffer [this issue](https://github.com/quarkusio/quarkus/issues/12498).

# Modules

Tree of gradle modules: <!-- generated with  tree -L 3 -d and some cut and paste -->

```
├── client
├── docsite
└── server
    ├── app
    ├── core
    └── persistence
        └── memory
```

## client

As the name suggests, contains the client classes of the project. 
As of now the client is generated from the openapi spec.

## docsite

Contains a docusaurus microsite that is published on github pages, for more info, have a look [here](#documentation).


## server

server module is split in 3 parts:

### core

Contains the core logic of whitefox, it should have nothing to do with persistence or HTTP APIs implementation.

### persistence

Persistence is then split in different modules based on the persistence technology that is implemented. Only 
"memory" is available right now.

### app

It's the module that depends on persistence and core and implements the HTTP APIs and it's the one actually packaged
and run as a quarkus application.

