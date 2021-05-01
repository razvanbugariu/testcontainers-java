package org.testcontainers.containers;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.junit.Rule;
import org.junit.Test;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class InfluxDBContainerWithUserTest {

    private static final String TEST_VERSION = InfluxDBTestImages.INFLUXDB_TEST_IMAGE.getVersionPart();
    private static final String BUCKET = "test-bucket";
    private static final String ORGANIZATION = "test-organization";
    private static final String ADMIN_TOKEN = "test-admin-token";

    @Rule
    public InfluxDBContainer<?> influxDBContainer = new InfluxDBContainer<>(InfluxDBTestImages.INFLUXDB_TEST_IMAGE)
        .withBucket(BUCKET)
        .withOrganization(ORGANIZATION)
        .withAdminToken(ADMIN_TOKEN);

    @Test
    public void describeDatabases() {
        InfluxDBClient actual = influxDBContainer.getNewInfluxDB();

        assertThat(actual, notNullValue());
        assertThat(actual.getBucketsApi().findBucketByName(BUCKET), notNullValue());
    }

    @Test
    public void checkVersion() {
        InfluxDBClient actual = influxDBContainer.getNewInfluxDB();

        assertThat(actual, notNullValue());

        assertThat(actual.health(), notNullValue());
        assertThat(actual.health().getVersion(), is(TEST_VERSION));
    }

    @Test
    public void queryForWriteAndRead() {
        InfluxDBClient influxDBClient = influxDBContainer.getNewInfluxDB();
        Instant now = Instant.now();
        try (WriteApi writeApi = influxDBClient.getWriteApi()) {
            Point point = Point.measurement("cpu")
                .time(now, WritePrecision.MS)
                .addField("idle", 90L)
                .addField("user", 9L)
                .addField("system", 1L);
            writeApi.writePoint(point);
        }

        String flux = String.format("from(bucket:\"%s\") |> range(start: 0)", BUCKET);

        QueryApi queryApi = influxDBClient.getQueryApi();

        List<FluxTable> result = queryApi.query(flux);

        assertThat(result, notNullValue());
        assertThat(result.size(), not(0));

        influxDBClient.close();
    }
}
