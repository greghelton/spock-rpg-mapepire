# Working sample: ILE RPG subprocedures tested through Db2 UDFs with Spock

This is a Groovy project that relies on Maven to build and execute the Groovy code. This project depends on Spock for testing and it depends on IBM i RPG and DB2/400 SQL to create the native IBM i objects.

## Included functions
- `FormatZip`
- `CalculateMonthlyPayment`

## Project layout

```
CALCSVCS.rpgle               RPG source member
RpgSubprocedureSpec.groovy   The Spock spec
pom.xml                      Maven build - dependencies plus the plugins that compile and run the spec
build.sql                    creates the user defined functions and shows how to test them
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
