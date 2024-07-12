package io.github.opensabe.common.s3.service;

import io.github.opensabe.common.s3.properties.S3Properties;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Log4j2
public class S3AsyncTaskFileService implements AsyncTaskFileService {

    private final FileService s3SyncFileService;

    private final ExecutorService executorService;

    private final S3Properties s3Properties;

    private final AtomicBoolean atomicBoolean = new AtomicBoolean(true);

    public S3AsyncTaskFileService(FileService s3SyncFileService, ExecutorService executorService, S3Properties s3Properties) {
        this.s3SyncFileService = s3SyncFileService;
        this.executorService = executorService;
        this.s3Properties = s3Properties;
    }

    @Override
    public Future<String> upload(Provider<byte[]> provider, String country, String fileName) {
        var lock = atomicBoolean.compareAndSet(true,false);
        if (!lock) {
            throw new IllegalThreadStateException("another task is running");
        }
        return executorService.submit(() -> upload1(provider,country,fileName));
    }

    @Override
    public void upload(Provider<byte[]> provider, String country, String fileName, Consumer<String> consumer) {
        var lock = atomicBoolean.compareAndSet(true,false);
        if (!lock) {
            throw new IllegalThreadStateException("another task is running");
        }
        var future = executorService.submit(() -> upload1(provider,country,fileName));
        executorService.submit(() -> {
            try {
                var path = future.get();
                consumer.accept(path);
            } catch (Throwable e) {
                future.cancel(true);
                log.error("S3AsyncTaskFileService.upload upload file error {}",e.getMessage(),e);
            }
        });
    }

    @Override
    public Future<String> uploadFile(Provider<File> provider, String country, String fileName) {
        var lock = atomicBoolean.compareAndSet(true,false);
        if (!lock) {
            throw new IllegalThreadStateException("another task is running");
        }
        return executorService.submit(() -> uploadFile1(provider,country,fileName));
    }

    @Override
    public void uploadFile(Provider<File> provider, String country, String fileName, Consumer<String> consumer) {
        var lock = atomicBoolean.compareAndSet(true,false);
        if (!lock) {
            throw new IllegalThreadStateException("another task is running");
        }
        var future = executorService.submit(() -> uploadFile1(provider,country,fileName));
        executorService.submit(() -> {
            try {
                var path = future.get();
                consumer.accept(path);
            } catch (Throwable e) {
                future.cancel(true);
                log.error("S3AsyncTaskFileService.upload upload file error {}",e.getMessage(),e);
            }
        });
    }

    private String upload1 (Provider<byte[]> provider, String country, String fileName) {
        try {
            s3SyncFileService.putObject(provider.supply(),s3Properties.getProfile(),country+"/biz/task/"+fileName);
            return String.format(s3Properties.getStaticDomain(),country) + "task/"+fileName;
        }catch (Throwable e) {
            log.error("S3AsyncTaskFileService.upload upload file error {}",e.getMessage(),e);
        }finally {
            atomicBoolean.set(true);
        }
        return null;
    }
    private String uploadFile1 (Provider<File> provider, String country, String fileName) {
        try {
            s3SyncFileService.putObject(provider.supply(),s3Properties.getProfile(),country+"/biz/task/"+fileName);
            return String.format(s3Properties.getStaticDomain(),country) + "task/"+fileName;
        }catch (Throwable e) {
            log.error("S3AsyncTaskFileService.upload upload file error {}",e.getMessage(),e);
        }finally {
            atomicBoolean.set(true);
        }
        return null;
    }
}
