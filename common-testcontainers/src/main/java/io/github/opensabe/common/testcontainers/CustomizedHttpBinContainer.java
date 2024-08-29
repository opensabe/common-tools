package io.github.opensabe.common.testcontainers;

import com.github.dockerjava.api.command.InspectContainerResponse;
import lombok.SneakyThrows;
import org.testcontainers.containers.GenericContainer;

import java.util.concurrent.TimeUnit;

public class CustomizedHttpBinContainer extends GenericContainer<CustomizedHttpBinContainer> {

    public static final int HTTP_BIN_PORT = 8080;

    public CustomizedHttpBinContainer() {
        super("mccutchen/go-httpbin");
    }

    @Override
    protected void configure() {
        withExposedPorts(HTTP_BIN_PORT);
    }

    @Override
    @SneakyThrows
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        ExecResult execResult = null;
        //直到执行成功
        while (execResult == null || execResult.getExitCode() != 0) {
            TimeUnit.SECONDS.sleep(1);
            execResult = execInContainer("curl", "http://localhost:" + HTTP_BIN_PORT);
            System.out.println("stdout: " + execResult.getStdout());
            System.out.println("stderr: " + execResult.getStderr());
        }
        System.out.println("DynamoDB started at port: " + getHttpBinPort());
    }

    @Override
    public void stop() {
        super.stop();
        System.out.println("HTTP_BIN stopped");
    }

    public int getHttpBinPort() {
        return getMappedPort(HTTP_BIN_PORT);
    }
}
