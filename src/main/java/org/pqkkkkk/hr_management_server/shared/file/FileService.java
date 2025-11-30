package org.pqkkkkk.hr_management_server.shared.file;

import java.util.List;

import org.pqkkkkk.hr_management_server.shared.Constants.SupportedFileFormat;

public interface FileService<T> {
    public byte[] exportListToFile(List<T> dataList);
    public SupportedFileFormat getFileFormat();
}
