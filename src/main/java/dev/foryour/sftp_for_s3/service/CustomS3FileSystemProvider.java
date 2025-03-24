package dev.foryour.sftp_for_s3.service;

import org.carlspring.cloud.storage.s3fs.S3FileSystemProvider;
import org.carlspring.cloud.storage.s3fs.S3Path;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;

public class CustomS3FileSystemProvider extends S3FileSystemProvider {

    @Override
    public FileChannel newFileChannel(
            Path path,
            Set<? extends OpenOption> options,
            FileAttribute<?>... attrs
    ) throws IOException {
        return new CustomS3FileChannel((S3Path) path, options, true);
    }

}
