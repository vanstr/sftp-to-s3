package dev.foryour.sftp_for_s3.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "sftp-for-s3")
public class SftpForS3Properties {
    private Integer port;
    private String hostKeyFile;
    private String rootBucket;
    private String defaultHomeDir;
    private List<SftpUser> users;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SftpUser {
        private String username;
        private String password;
        private String homeDir;
    }

}
