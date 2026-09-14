# Working sample: ILE RPG subprocedures tested through Db2 UDFs with Spock

This sample is meant to be run from an IBM i environment with Mapepire enabled and a valid Db2 UDF deployment.
This project is a minimal, coherent example of calling exported RPG subprocedures from tests defined in Spock via Mapepire and Db2 SQL functions.

The goal is to keep one canonical contract: the SQL UDFs and the RPG exports match the same names and parameter lists used by the test.

## Included functions
- `FormatZip`
- `CalculateMonthlyPayment`

## Canonical SQL function definitions

```sql
CREATE OR REPLACE FUNCTION MYLIB.FORMAT_ZIP (
    ZIP5 CHAR(5),
    ZIP4 CHAR(4)
)
RETURNS CHAR(10)
LANGUAGE RPGLE
DETERMINISTIC
NO SQL
EXTERNAL NAME 'MYLIB/CALCSVCS(FORMATZIP)'
PARAMETER STYLE GENERAL;

CREATE OR REPLACE FUNCTION MYLIB.CALC_MONTHLY_PAYMENT (
    PRINCIPAL DECIMAL(15,2),
    RATE      DECIMAL(5,4),
    MONTHS    INTEGER
)
RETURNS DECIMAL(15,2)
LANGUAGE RPGLE
DETERMINISTIC
NO SQL
EXTERNAL NAME 'MYLIB/CALCSVCS(CALCULATEMONTHLYPAYMENT)'
PARAMETER STYLE GENERAL;
```

## Example SQL checks

```sql
VALUES MYLIB.FORMAT_ZIP('75034', '1234');
-- '75034-1234'

VALUES MYLIB.CALC_MONTHLY_PAYMENT(10000.00, 0.0525, 36);
-- 300.83
```

## Project layout

```
CALCSVCS.rpgle                                  RPG source member, deployed to IBM i separately
src/test/groovy/RpgSubprocedureSpec.groovy      The Spock spec
```

## Groovy test pattern

The Spock test in `RpgSubprocedureSpec.groovy` uses parameterized SQL calls to invoke the UDFs through the Mapepire connection, via the real `mapepire-java` (`io.github.mapepire-ibmi:mapepire-sdk`) API:

```groovy
def opts = new QueryOptions(false, false, [principal, rate, months])
def query = sqlJob.query(sql, opts)
QueryResult result = query.execute().get()
query.close().get()

result.getSuccess()
```

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
