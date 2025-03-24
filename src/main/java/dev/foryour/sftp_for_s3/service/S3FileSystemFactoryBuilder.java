package dev.foryour.sftp_for_s3.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.carlspring.cloud.storage.s3fs.S3FileSystem;
import org.carlspring.cloud.storage.s3fs.S3FileSystemProvider;
import org.springframework.util.StringUtils;

import dev.foryour.sftp_for_s3.service.SftpForS3Properties.SftpUser;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class S3FileSystemFactoryBuilder {

    private List<SftpUser> sftpUsers;
    private String bucketName;
    private String defaultHomeDir;
    private S3FileSystemProvider provider;

    public S3FileSystemFactoryBuilder withS3FileSystemProvider(S3FileSystemProvider provider) {
        this.provider = provider;
        return this;
    }

    public S3FileSystemFactoryBuilder withHomeDirs(List<SftpUser> sftpUsers) {
        this.sftpUsers = sftpUsers;
        return this;
    }

    public S3FileSystemFactoryBuilder withDefaultHomeDir(String defaultHomeDir) {
        this.defaultHomeDir = defaultHomeDir;
        return this;
    }

    public S3FileSystemFactoryBuilder withBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public VirtualFileSystemFactory build() throws IOException {
        if (this.bucketName == null) {
            throw new IllegalArgumentException("Bucket name is required");
        }
        S3Client s3Client = S3Client.builder().build();
        if(this.provider == null) {
            this.provider = new S3FileSystemProvider();
        }
        S3FileSystem s3FileSystem = new S3FileSystem(this.provider, null, s3Client, URI.create("s3:///").getHost());

        log.info("Available buckets:");
        s3FileSystem.getRootDirectories().forEach(p -> log.info("{}", p));

        Path bucketPath = s3FileSystem.getPath("/" + this.bucketName);
        VirtualFileSystemFactory newFileSystem = new VirtualFileSystemFactory(bucketPath);

        log.info("Folders in root:");
        Files.list(newFileSystem.getDefaultHomeDir()).forEach(folder -> log.info("{}", folder));

        if (!this.sftpUsers.isEmpty()) {
            setUserHomeDirs(newFileSystem);
        }

        return newFileSystem;
    }

    private void setUserHomeDirs(VirtualFileSystemFactory newFileSystem) {
       this.sftpUsers.forEach(user -> {
            Path path;
            if (StringUtils.hasText(user.getHomeDir())) {
                path = newFileSystem.getDefaultHomeDir().resolve(user.getHomeDir() + "/");
            } else {
                path = newFileSystem.getDefaultHomeDir().resolve(this.defaultHomeDir + "/");
            }
            if (!Files.exists(path)) {
                try {
                    Files.createDirectory(path);
                    log.info("Home folder created: {}", path);
                } catch (Exception e) {
                    log.error("Error creating home folder: {}", path, e);
                }
            }
            newFileSystem.setUserHomeDir(user.getUsername(), path);
        });
    }

}
