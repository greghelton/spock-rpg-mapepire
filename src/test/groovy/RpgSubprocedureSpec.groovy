import spock.lang.Specification
import spock.lang.Shared
import spock.lang.Unroll
import io.github.mapepire_ibmi.SqlJob
import io.github.mapepire_ibmi.types.DaemonServer
import io.github.mapepire_ibmi.types.QueryOptions
import io.github.mapepire_ibmi.types.QueryResult
import java.math.RoundingMode

class RpgSubprocedureSpec extends Specification {

    @Shared SqlJob sqlJob

    def setupSpec() {
        def host      = System.getenv('IBMI_HOST')
        def portStr   = System.getenv('IBMI_PORT') ?: '8076'
        def user      = System.getenv('IBMI_USER')
        def password  = System.getenv('IBMI_PASSWORD')
        def ignoreTls = Boolean.parseBoolean(System.getenv('IBMI_IGNORE_UNAUTHORIZED') ?: 'false')

        assert host     : "Environment variable IBMI_HOST must be configured"
        assert user     : "Environment variable IBMI_USER must be configured"
        assert password : "Environment variable IBMI_PASSWORD must be configured"

        int port = portStr.toInteger()

        DaemonServer server = new DaemonServer(host, port, user, password)
        server.setRejectUnauthorized(!ignoreTls)

        sqlJob = new SqlJob()
        sqlJob.connect(server).get()
    }

    def cleanupSpec() {
        // SqlJob#close() is synchronous and returns void - it is not a CompletableFuture.
        sqlJob?.close()
    }

    def "format zip code with 5-digit and 4-digit extension"() {
        given: "the scalar UDF query"
        def sql = "VALUES MYLIB.FORMAT_ZIP(?, ?)"
        def opts = new QueryOptions(false, false, ['75034', '1234'])

        when: "invoked through Mapepire"
        def query = sqlJob.query(sql, opts)
        QueryResult result = query.execute().get()
        query.close().get()

        then: "the returned string matches expected output"
        result.getSuccess()
        firstValue(result).toString().trim() == '75034-1234'
    }

    @Unroll
    def "calculate monthly payment via RPG UDF"() {
        given: "a parameterized call to the monthly payment UDF"
        def sql = "VALUES MYLIB.CALC_MONTHLY_PAYMENT(?, ?, ?)"
        def opts = new QueryOptions(false, false, [principal, rate, months])

        when: "The RPG UDF is invoked through Db2 SQL"
        def query = sqlJob.query(sql, opts)
        QueryResult result = query.execute().get()
        query.close().get()

        then: "Power assertion validates the calculation against the explicit HALF_UP cents policy"
        result.getSuccess()
        // Numeric columns travel over the wire as JSON numbers and land as a java.lang.Double
        // inside the untyped result map (Jackson's default, since USE_BIG_DECIMAL_FOR_FLOATS
        // isn't enabled by mapepire-java). Route through BigDecimal(String) - never
        // "as BigDecimal" / BigDecimal.valueOf(double) directly - so binary floating-point
        // noise can't leak into a cents comparison.
        def actual = new BigDecimal(firstValue(result).toString()).setScale(2, RoundingMode.HALF_UP)
        actual == expected.setScale(2, RoundingMode.HALF_UP)

        where:
        principal | rate   | months || expected
        10000.00  | 0.0525 | 36     || 300.83G
        25000.00  | 0.0650 | 60     || 489.15G
        50000.00  | 0.0475 | 120    || 524.24G
    }

    /**
     * Each UDF call in this spec returns exactly one unaliased column. Db2 for i assigns a
     * generated name to it rather than a predictable one, so read the first (only) value out
     * of the row map positionally instead of depending on that generated name. QueryResult#data
     * rows come back as an ordered (LinkedHashMap) map of column name to value - see
     * mapepire-java's own SqlTest for the confirmed shape.
     */
    private static Object firstValue(QueryResult result) {
        def row = result.getData()[0] as Map
        return row.values().iterator().next()
    }
}
