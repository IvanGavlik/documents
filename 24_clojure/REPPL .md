# REPL

## sybalius

✅ a step-by-step guide to a “perfect” REPL workflow in Cursive
✅ examples of advanced REPL usage for real projects
✅ how to integrate REPL-driven development into web or backend apps
✅ a cheatsheet of send-to-REPL keyboard shortcuts



In Clojure, a programmer will typically use the REPL for a wide spectrum of programming tasks, when in another language she would turn to other sorts of tools. Such tasks include:

    launching local development environments,
    running automated test suites,
    one-off database queries and interventions,
    debugging,
    orchestrating remote machines,
    getting familiar with libraries and APIs,
    …​and many forms of exploration.

REPL for ClojureScript project
    - project uses (take a look at spathi vs dnyari)

REPL for docker projects

REPL with AI intergration

deep dive into REPL - how it is implemented how it works

## “perfect” REPL workflow in Cursive

### Start REPLY the right way

Always start from the project, not from a single namespace.
Run → REPL → REPL for project

Ensure that
- all dependencies are loaded
- dev namespace is available
- hot-reloading tools (tools.namepsace) work
- configuration isconsistent

#### dev namespace

Helper namespace only for local development and tightly integrated with the REPL
Usually as control room for: running, reloading, testing, starting/stoping system, working with data interactively
Centralize REPL utility functions

Typical tool namespace
* integrates with tools.namspeace.repl
* stores system lifecycle funciton (integrant, component, mount, aero...)
* repl commands: clear logs, run DB migrations, load sample data, open poratal, 

Example have

(ns dev
  (:require
    [clojure.tools.namespace.repl :refer [refresh]]
    [myapp.system :as system]
    [portal.api :as portal]))

##### Clojure’s user namespace vs a custom dev namespace 

user is the default namespace that Clojure enters when you start a REPL without specifying anything else.

t exists so you have somewhere to stand before loading your real code.

🧪 Good for:
    Quick experiments
    Testing a small function
    One-time REPL evals
    REPL bootstrapping
    
🚫 Not good for:
    Running your system
    Start/stop server logic
    Reset workflows
    Developer tools
    Code reload workflows

Why?

Because you don't want to pollute the built-in namespace, and you don't want startup logic to be tied to a namespace you don't control.


The dev namespace is:
✔ created by you
✔ put into dev/dev.clj or src/dev/dev.clj
✔ loaded only in development
✔ part of the real REPL workflow

This namespace typically contains:

🔧 REPL “dashboard” functions:

(start) – starts your system
(stop) – stops everything
(reset) – reloads changed namespaces and restarts system
(seed) – load sample data
(migrations) – run DB migrations
(open-portal) – launch Portal inspector
(benchmark) – run criterium benchmarks
(test) – run tests

Some beginners put things into user.clj at the root of the project.

This is almost always a bad idea:

❌ It's tightly coupled to REPL startup
❌ It pollutes a global namespace
❌ It cannot have dev-only dependencies (danger for production)
❌ Tools like tools.namespace.repl don’t work well there
❌ You lose separation between "app code" and "dev tools"

Large open-source Clojure projects never use a custom user ns.
They always have a dev namespace.

How teams typically set it up 

deps.end
:aliases
{:dev {:extra-paths ["dev"]
       :extra-deps {clj-commons/tools.namespace ...
                    djblue/portal ...
                    criterium/criterium ...}}}
dev/dev.clj:
(ns dev
  (:require
    [clojure.tools.namespace.repl :refer [refresh]]
    [myapp.system :as system]
    [portal.api :as portal]))

in the REPL
clj -A:dev

##### Alternatives for tools.namespace

* clj-reload
* for components integrant.repl component.repl mount juxt/clip
* file watching hawk
* tests driven kaocha eftest

##### Most common

🔵 1. Reloading & System Lifecycle Tools
These handle hot reloading, system startup, system teardown, and component lifecycle:
clojure.tools.namespace
integrant.repl component.repl mount juxt/clip
reloaded.repl (legacy but still used)
aero (if config needed for dev)

🔵 2. Data Inspectors & Debugging Tools
Developers use these to inspect data, visualize structures, and debug functions:
Portal or Reveal
FlowStorm debugger
Tap system (tap>) helpers
clojure.tools.trace
spyscope
scope-capture
pretty printers: puget, fipp

🔵 3. REPL Workflow Enhancers
Libraries that make experimentation smoother:
Dirac (ClojureScript Chrome DevTools integration)
nREPL middlewares (extra tooling)
cider-nrepl (even in IntelliJ environments sometimes)
piggieback (ClojureScript REPLs)
re-frame-10x (CLJS apps)
criterium (benchmarking at REPL)

🔵 4. File-Watching & Automation Helpers
Used to auto-reload, watch directories, run tests on file changes:
hawk
tools.namespace.repl/refresh wrappers
kaocha + kaocha.watch
guard / fsnotify / babashka fswatch (scripts)

🔵 5. Local Development Databases & Test Data
Tools for interacting with databases directly from the REPL:
next.jdbc
honey.sql (building queries)
ragtime (migrations)
migratus

H2, SQLite, or Datomic dev-local
faker libraries:
clj-faker
lambdaisland/faker

🔵 6. HTTP / API Utilities
Tools used for manual API testing or calling external services:
clj-http
hato
http-kit client
curl wrappers via babashka

🔵 7. Logging, Metrics & Observability in Dev
Helpers to tweak log levels, inspect metrics, etc.:
Timbre
Logback config overrides
metrics-clojure
OpenTelemetry exporters (dev mode only)

🔵 8. Development-Time Config & Secrets
Libraries to load a special dev config or secrets:
aero
cprop
yogthos/config
dotenv (.env) via
hjava-jdotenv
babashka’s dotenv support

🔵 9. Local Shell Automation
Used for tasks like spinning up Docker or running migrations:
babashka
clojure.java.shell
shell helpers (like dps, dcup, dcdown)

🔵 10. Notebook / Documentation Tools
These support computational notebooks or dev docs:
Clerk
Notespace
Oz / Hanami (visualization)
Monk / Gorilla REPL (older but still around)

🔵 11. Mocking & Stubbing Tools
Used to mock out services while prototyping:
with-redefs (built-in)
mockfn
manifold + aleph test helpers
wiremock (through Java interop)

🔵 12. Testing Tools Used From dev/ REPL
Often exposed in dev namespace for quick execution:
Kaocha
Eftest
test-refresh
midje (legacy but common in old codebases)

🔵 13. Misc Developer Utilities
These are generic helpers frequently included in a dev namespace:
cheshire (JSON)
clojure.data.csv (CSV helpers)
tools.cli (running commands from REPL)
refactor-nrepl tooling
medley (general helpers)

humane-test-output (pretty tests)

### Open there tabs: source, test, REPL

* Alt + ← / Alt + → to switch files

### Master shrortcuts
* Evaluate top-level form
* Evaluate the form “before cursor”
* Send selection to REPL
* Jump to REPL

Write a function -> evaluate it -> try in REPL 

### Use inspectors (Portal or Reveal)

 (portal.api/open)
 (add-tap #'portal.api/submit)
 (tap> some-big-map)

### Use comment as "scratchpad"

### Reload code safely using tool namespace

Add a dev namespace have insise tools.namespace

### Run tests for REPL 
(require 'my.ns-test :reload)
(clojure.test/run-tests 'my.ns-test)
or use keyboard shortcuts

### Additional tips
* turn on structual editing
* minimize switching to REPL stay in editor (REPL just as output)
* use def insdie comment for temporary binding
* save sample data in .edn files
    (slurp "dev-resources/sample.edn")
