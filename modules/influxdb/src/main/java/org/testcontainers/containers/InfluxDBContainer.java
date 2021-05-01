package org.testcontainers.containers;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.Set;

/**
 * See <a href="https://store.docker.com/images/influxdb">https://store.docker.com/images/influxdb</a>
 */
public class InfluxDBContainer<SELF extends InfluxDBContainer<SELF>> extends GenericContainer<SELF> {

    public static final Integer INFLUXDB_PORT = 8086;

    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("influxdb");
    private static final String DEFAULT_TAG = "2.0.6";

    @Deprecated
    public static final String VERSION = DEFAULT_TAG;

    private String bucket = "init.bucket";
    private String username = "any";
    private String password = "any.password";
    private String organization = "org";
    private String adminToken = "admin.token";

    /**
     * @deprecated use {@link InfluxDBContainer(DockerImageName)} instead
     */
    @Deprecated
    public InfluxDBContainer() {
        this(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));
    }

    /**
     * @deprecated use {@link InfluxDBContainer(DockerImageName)} instead
     */
    @Deprecated
    public InfluxDBContainer(final String version) {
        this(DEFAULT_IMAGE_NAME.withTag(version));
    }

    public InfluxDBContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);

        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);

        waitStrategy = new WaitAllStrategy()
            .withStrategy(Wait.forHttp("/health").forStatusCode(200))
            .withStrategy(Wait.forListeningPort());

        addExposedPort(INFLUXDB_PORT);
    }

    @Override
    protected void configure() {
        addEnv("DOCKER_INFLUXDB_INIT_USERNAME", username);
        addEnv("DOCKER_INFLUXDB_INIT_PASSWORD", password);
        addEnv("DOCKER_INFLUXDB_INIT_MODE", "setup");
        addEnv("DOCKER_INFLUXDB_INIT_BUCKET", bucket);
        addEnv("DOCKER_INFLUXDB_INIT_ORG", organization);
        addEnv("DOCKER_INFLUXDB_INIT_ADMIN_TOKEN", adminToken);
    }

    @Override
    public Set<Integer> getLivenessCheckPortNumbers() {
        return Collections.singleton(getMappedPort(INFLUXDB_PORT));
    }

    /**
     * Set env variable `DOCKER_INFLUXDB_INIT_BUCKET`.
     *
     * @param bucket Automatically initializes a bucket with the name of this environment variable.
     * @return a reference to this container instance
     */
    public SELF withBucket(final String bucket) {
        this.bucket = bucket;
        return self();
    }

    /**
     * Set env variable `DOCKER_INFLUXDB_INIT_ADMIN_TOKEN`.
     *
     * @param adminToken Automatically initializes a bucket that can be accessed using this token.
     * @return a reference to this container instance
     */
    public SELF withAdminToken(final String adminToken) {
        this.adminToken = adminToken;
        return self();
    }


    /**
     * Set env variable `DOCKER_INFLUXDB_INIT_ORG`.
     *
     * @param organization Automatically initializes a organization with the name of this environment variable.
     * @return a reference to this container instance
     */
    public SELF withOrganization(final String organization) {
        this.organization = organization;
        return self();
    }


    /**
     * Set env variable `DOCKER_INFLUXDB_INIT_USERNAME`.
     *
     * @param username The name of a user to be created with no privileges. If `INFLUXDB_DB` is set, this user will
     *                 be granted read and write permissions for that bucket.
     * @return a reference to this container instance
     */
    public SELF withUsername(final String username) {
        this.username = username;
        return self();
    }

    /**
     * Set env variable `DOCKER_INFLUXDB_INIT_PASSWORD`.
     *
     * @param password The password for the user configured with `DOCKER_INFLUXDB_INIT_USERNAME`. If this is unset, a random password
     *                 is generated and printed to standard out.
     * @return a reference to this container instance
     */
    public SELF withPassword(final String password) {
        this.password = password;
        return self();
    }

    /**
     * @return a url to influxDb
     */
    public String getUrl() {
        return "http://" + getHost() + ":" + getLivenessCheckPort();
    }

    /**
     * @return a influxDb client
     */
    public InfluxDBClient getNewInfluxDB() {
        return InfluxDBClientFactory.create(getUrl(), adminToken.toCharArray(), organization, bucket);
    }
}
