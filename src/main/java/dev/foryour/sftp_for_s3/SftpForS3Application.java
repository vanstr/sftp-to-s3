package dev.foryour.sftp_for_s3;

import dev.foryour.sftp_for_s3.service.SftpForS3Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(SftpForS3Properties.class)
@SpringBootApplication
public class SftpForS3Application {

	public static void main(String[] args) {
		SpringApplication.run(SftpForS3Application.class, args);
	}

}
