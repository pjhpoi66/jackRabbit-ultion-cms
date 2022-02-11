package com.ultion.cms.test;

import lombok.NonNull;
import org.apache.jackrabbit.api.binary.BinaryUpload;

import java.net.URI;

public class BinaryUploadImpl implements BinaryUpload {


    @Override
    public @NonNull Iterable<URI> getUploadURIs() {
        return null;
    }

    @Override
    public long getMinPartSize() {
        return 0;
    }

    @Override
    public long getMaxPartSize() {
        return 0;
    }

    @Override
    public @NonNull String getUploadToken() {
        return null;
    }
}
