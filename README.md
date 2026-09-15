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
pom.xml                      Maven build - dependencies plus the plugins that compile and run the spec
```

## Running the tests

```bash
mvn test
```

`pom.xml` has a `<build>` section wiring this up: the `gmavenplus-plugin`
compiles `RpgSubprocedureSpec.groovy` (Maven core only compiles `.java` by
itself), and `maven-surefire-plugin` runs it - Spock plugs into the JUnit
Platform as a `TestEngine`, and Surefire auto-detects the
`junit-platform-launcher` dependency already declared above to run it, with
no extra provider configuration needed. Maven resolves every dependency
(including mapepire-sdk's own transitive ones, like Jackson and
Java-WebSocket) from Maven Central automatically.

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
