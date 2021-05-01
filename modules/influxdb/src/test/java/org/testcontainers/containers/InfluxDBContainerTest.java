package org.testcontainers.containers;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.HealthCheck;
import org.junit.ClassRule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class InfluxDBContainerTest {

    @ClassRule
    public static InfluxDBContainer<?> influxDBContainer = new InfluxDBContainer<>(InfluxDBTestImages.INFLUXDB_TEST_IMAGE);

    @Test
    public void getUrl() {
        String actual = influxDBContainer.getUrl();

        assertThat(actual, notNullValue());
    }

    @Test
    public void getNewInfluxDB() {
        InfluxDBClient actual = influxDBContainer.getNewInfluxDB();

        assertThat(actual, notNullValue());
        assertThat(actual.health(), notNullValue());
        assertThat(actual.health().getStatus(), is(HealthCheck.StatusEnum.PASS));
    }

    @Test
    public void getLivenessCheckPort() {
        Integer actual = influxDBContainer.getLivenessCheckPort();

        assertThat(actual, notNullValue());
    }

    @Test
    public void isRunning() {
        boolean actual = influxDBContainer.isRunning();

        assertThat(actual, is(true));
    }
}
