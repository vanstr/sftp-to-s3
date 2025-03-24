package dev.foryour.sftp_for_s3.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.OpenOption;
import java.util.Set;

import org.carlspring.cloud.storage.s3fs.S3Path;
import org.carlspring.cloud.storage.s3fs.S3SeekableByteChannel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomS3FileChannel extends FileChannel {
    private SeekableByteChannel byteChannel;

    public CustomS3FileChannel(SeekableByteChannel byteChannel) {
        this.byteChannel = byteChannel;
    }

    public CustomS3FileChannel(S3Path path, Set<? extends OpenOption> options, boolean required) throws IOException {
        this.byteChannel = new S3SeekableByteChannel(path, options, required);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return byteChannel.read(dst);
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        return read(dsts);
    }

    @Override
    public int read(ByteBuffer dst, long position) throws IOException {
        return read(dst);
    }

    @Override
    public int write(ByteBuffer src, long position) throws IOException {
        return byteChannel.write(src);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return byteChannel.write(src);
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        return byteChannel.write(srcs[0]);
    }

    @Override
    public long position() throws IOException {
        return byteChannel.position();
    }

    @Override
    public FileChannel position(long newPosition) throws IOException {
        return new CustomS3FileChannel(byteChannel.position(newPosition));
    }

    @Override
    public long size() throws IOException {
        return byteChannel.size();
    }

    @Override
    public FileChannel truncate(long size) throws IOException {
        return new CustomS3FileChannel(byteChannel.truncate(size));
    }

    @Override
    public void force(boolean metaData) throws IOException {
        log.info("force method is not supported.");
    }

    @Override
    public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
        throw new UnsupportedOperationException("transferTo is not supported.");
    }

    @Override
    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
        throw new UnsupportedOperationException("transferFrom is not supported.");
    }

    @Override
    public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException {
        throw new UnsupportedOperationException("map is not supported.");
    }

    @Override
    public FileLock lock(long position, long size, boolean shared) throws IOException {
        throw new UnsupportedOperationException("lock is not supported.");
    }

    @Override
    public FileLock tryLock(long position, long size, boolean shared) throws IOException {
        throw new UnsupportedOperationException("tryLock is not supported.");
    }

    @Override
    protected void implCloseChannel() throws IOException {
        byteChannel.close();
    }
}