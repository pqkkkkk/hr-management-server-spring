package org.pqkkkkk.hr_management_server.shared;

public class Constants {
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";
    public static final Integer DEFAULT_PAGE_SIZE = 10;
    public static final Integer DEFAULT_PAGE_NUMBER = 1;

    public enum SortDirection {
        ASC,
        DESC
    }

    public enum SupportedFileFormat {
        EXCEL(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        PDF(".pdf", "application/pdf");

        private final String fileExtension;
        private final String contentType;

        SupportedFileFormat(String fileExtension, String contentType) {
            this.fileExtension = fileExtension;
            this.contentType = contentType;
        }

        public String getFileExtension() {
            return fileExtension;
        }

        public String getContentType() {
            return contentType;
        }
    }
    
}
