package io.github.opensabe.common.testcontainers;

import com.github.dockerjava.api.command.InspectContainerResponse;
import lombok.SneakyThrows;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

import java.util.concurrent.TimeUnit;

public class CustomizedDynamoDBContainer extends GenericContainer<CustomizedDynamoDBContainer> {
    public static final int DYNAMODB_PORT = 8000;

    public CustomizedDynamoDBContainer() {
        super("amazon/dynamodb-local");
    }

    @Override
    protected void configure() {
        withExposedPorts(DYNAMODB_PORT);
    }

    @Override
    @SneakyThrows
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        Container.ExecResult execResult = null;
        //直到执行成功
        while (execResult == null || execResult.getExitCode() != 0) {
            TimeUnit.SECONDS.sleep(1);
            execResult = execInContainer("curl", "http://localhost:" + DYNAMODB_PORT);
            System.out.println("stdout: " + execResult.getStdout());
            System.out.println("stderr: " + execResult.getStderr());
        }
        System.out.println("DynamoDB started at port: " + getDynamoDBPort());
    }

    @Override
    public void stop() {
        super.stop();
        System.out.println("DynamoDB stopped");
    }

    public int getDynamoDBPort() {
        return getMappedPort(DYNAMODB_PORT);
    }
}
