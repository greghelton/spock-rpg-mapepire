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

## Notes
- The SQL function name is the public contract that the test calls.
- The `EXTERNAL NAME` must match the exported RPG subprocedure name exactly.

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
