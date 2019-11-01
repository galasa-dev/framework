/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa;

import java.nio.file.OpenOption;

public class SetContentType implements OpenOption {

    private final ResultArchiveStoreContentType contentType;

    public SetContentType(ResultArchiveStoreContentType contentType) {
        this.contentType = contentType;
    }

    public ResultArchiveStoreContentType getContentType() {
        return contentType;
    }

}
