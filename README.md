# Working sample: ILE RPG subprocedures tested through Db2 UDFs with Spock

This sample is meant to be run from an IBM i environment with Mapepire enabled and a valid Db2 UDF deployment.
This project is a minimal, coherent example of calling exported RPG subprocedures from tests defined in Spock via Mapepire and Db2 SQL functions.

The goal is to keep one canonical contract: the SQL UDFs and the RPG exports match the same names and parameter lists used by the test.

## Included functions
- `FormatZip`
- `CalculateMonthlyPayment`

## Project layout

```
CALCSVCS.rpgle               RPG source member, deployed to IBM i separately
RpgSubprocedureSpec.groovy   The Spock spec
pom.xml                      Maven dependency declarations (no build wired up - see below)
```

## Running the tests

`pom.xml` declares the dependencies but nothing here invokes Maven to
compile or run them, so running the spec still means compiling it yourself
and handing it to JUnit Platform's console launcher, which is what actually
executes a Spock spec - Spock plugs into JUnit Platform as a `TestEngine`
rather than being invoked directly:

```bash
groovyc -cp "groovy-4.0.33.jar:spock-core-2.4-groovy-4.0.jar:mapepire-sdk-0.1.3.jar:junit-platform-console-standalone-1.14.1.jar" \
        -d build/classes RpgSubprocedureSpec.groovy

java -jar junit-platform-console-standalone-1.14.1.jar \
     --select-class RpgSubprocedureSpec \
     --classpath "build/classes:groovy-4.0.33.jar:spock-core-2.4-groovy-4.0.jar:mapepire-sdk-0.1.3.jar:<mapepire-sdk's transitive jars>"
```

The first command compiles `RpgSubprocedureSpec.groovy` against the jars
corresponding to the dependencies declared in `pom.xml`. The second runs it: `junit-platform-console-standalone`
is a separate, self-contained "fat jar" (`org.junit.platform:junit-platform-console-standalone:1.14.1`)
that bundles the JUnit Platform Launcher and a command-line front end, and
`--select-class` tells it to run one specific class rather than scanning the
whole classpath for tests. You'd need to download each jar above from Maven
Central yourself (or resolve them with Grape, Maven, or another dependency
manager) since nothing here fetches them automatically.

## Notes
- The SQL function name is the public contract that the test calls.
- The `EXTERNAL NAME` must match the exported RPG subprocedure name exactly.
- The project is intentionally small and coherent so it can serve as a working sample, not a mixed collection of older examples.

## Authentication

Mapepire's wire protocol only ever authenticates with a plain IBM i user/password pair - confirmed directly against the source of both `mapepire-java` (`DaemonServer` has `host`/`port`/`user`/`password`/`rejectUnauthorized`/`ca` fields, nothing else) and `mapepire-server` (`SystemConnection` authenticates via jt400's `new AS400(system, user, password)`). There's no token field anywhere to switch to, so this project uses `IBMI_PASSWORD` directly.

## Local setup example

```bash
export IBMI_HOST="my-ibmi-host.domain.com"
export IBMI_PORT="8076"
export IBMI_USER="TESTUSER"
export IBMI_PASSWORD="your-strong-password"
export IBMI_IGNORE_UNAUTHORIZED="true"

export JAVA_HOME=/QOpenSys/QIBM/ProdData/JavaVM/jdk17/64bit
export PATH=$JAVA_HOME/bin:/QOpenSys/pkgs/bin:$PATH
```
