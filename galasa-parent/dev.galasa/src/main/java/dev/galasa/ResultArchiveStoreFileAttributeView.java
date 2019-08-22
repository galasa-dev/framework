package dev.galasa;

import java.nio.file.attribute.FileAttributeView;

import javax.validation.constraints.NotNull;

public class ResultArchiveStoreFileAttributeView implements FileAttributeView {

    private final ResultArchiveStoreContentType contentType;

    public ResultArchiveStoreFileAttributeView(@NotNull ResultArchiveStoreContentType contentType) {
        this.contentType = contentType;
    }

    @Override
    public String name() {
        return "ras";
    }

    public ResultArchiveStoreContentType getContentType() {
        return this.contentType;
    }

}
