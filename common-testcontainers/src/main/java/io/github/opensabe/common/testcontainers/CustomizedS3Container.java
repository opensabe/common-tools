package io.github.opensabe.common.testcontainers;

import com.github.dockerjava.api.command.InspectContainerResponse;
import lombok.SneakyThrows;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

import java.util.concurrent.TimeUnit;

public class CustomizedS3Container extends GenericContainer<CustomizedS3Container> {
    public static final int S3_PORT = 4566;

    public CustomizedS3Container() {
        super("localstack/localstack");
    }

    @Override
    protected void configure() {
        withExposedPorts(S3_PORT);
    }

    @Override
    @SneakyThrows
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        Container.ExecResult execResult = null;
        //直到执行成功
        while (execResult == null || execResult.getExitCode() != 0) {
            execResult = execInContainer("awslocal", "s3api", "create-bucket", "--bucket", "test");
            System.out.println("stdout: " + execResult.getStdout());
            System.out.println("stderr: " + execResult.getStderr());
            TimeUnit.SECONDS.sleep(1);
        }
        System.out.println("S3 started at port: " + getS3Port());
    }

    @Override
    public void stop() {
        super.stop();
        System.out.println("S3 stopped");
    }

    public int getS3Port() {
        return getMappedPort(S3_PORT);
    }
}
