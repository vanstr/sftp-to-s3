package dev.foryour.sftp_for_s3.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.ServerBuilder;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class SftpForS3ServerStarter {

    private final SftpForS3Properties sftpServerProperties;
    private final SshServer sftpServer;


    public SftpForS3ServerStarter(SftpForS3Properties sftpServerProperties) {
        this.sftpServerProperties = sftpServerProperties;
        this.sftpServer = ServerBuilder.builder()
                .build();
    }

    @PostConstruct
    public void start() throws IOException, InterruptedException {
        log.info("Configuring SFTP server");

        sftpServer.setPort(sftpServerProperties.getPort());

        VirtualFileSystemFactory fileSystemFactory = new S3FileSystemFactoryBuilder()
                .withS3FileSystemProvider(new CustomS3FileSystemProvider())
                .withBucketName(sftpServerProperties.getRootBucket())
                .withHomeDirs(sftpServerProperties.getUsers())
                .withDefaultHomeDir(sftpServerProperties.getDefaultHomeDir())
                .build();

        sftpServer.setFileSystemFactory(fileSystemFactory);

        SimpleGeneratorHostKeyProvider keyPairProvider = new SimpleGeneratorHostKeyProvider(Path.of(sftpServerProperties.getHostKeyFile()));
        keyPairProvider.setOverwriteAllowed(false);
        sftpServer.setKeyPairProvider(keyPairProvider);

        SftpSubsystemFactory sftpSubsystemFactory = new SftpSubsystemFactory.Builder()
//                .withUnsupportedAttributePolicy(UnsupportedAttributePolicy.Ignore)
                .build();
        sftpServer.setSubsystemFactories(Collections.singletonList(sftpSubsystemFactory));


        SftpUserPasswordAuthenticator passwordAuthenticator = new SftpUserPasswordAuthenticator(sftpServerProperties);
        sftpServer.setPasswordAuthenticator(passwordAuthenticator);

        log.info("Starting SFTP server");
        sftpServer.start();
        log.info("SFTP server started");

        // TODO replace with proper shutdown
        Thread.sleep(180000);
    }

    @PreDestroy
    public void stop() throws IOException {
        if (sftpServer.isStarted()) {
            log.info("Stopping SFTP server");
            sftpServer.stop();
            log.info("SFTP server stopped");
        } else {
            log.info("SFTP server is not started");
        }

    }

    private static class SftpUserPasswordAuthenticator implements PasswordAuthenticator {
        private final List<SftpForS3Properties.SftpUser> users;

        public SftpUserPasswordAuthenticator(SftpForS3Properties props) {
            this.users = props.getUsers();
        }

        @Override
        public boolean authenticate(String username, String password, ServerSession session) throws PasswordChangeRequiredException, AsyncAuthException {
            return users.stream()
                    .anyMatch(u -> u.getUsername().equals(username) && u.getPassword().equals(password));
        }
    }
}
