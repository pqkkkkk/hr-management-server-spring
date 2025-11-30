package org.pqkkkkk.hr_management_server.shared.file;

import java.util.HashMap;
import java.util.Map;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.ExportedUser;
import org.pqkkkkk.hr_management_server.shared.Constants.SupportedFileFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileServiceFactory {
    @Bean
    public ExcelFileService<ExportedUser> excelFileServiceForUser() {
        return new ExcelFileService<>();
    }

    @Bean
    public PdfFileService<ExportedUser> pdfFileServiceForUser() {
        return new PdfFileService<>();
    }

    @Bean(name = "profileFileServices")
    public Map<SupportedFileFormat, FileService<ExportedUser>> profileFileServices(
            ExcelFileService<ExportedUser> excelService,
            PdfFileService<ExportedUser> pdfService) {
        Map<SupportedFileFormat, FileService<ExportedUser>> map = new HashMap<>();
        map.put(SupportedFileFormat.EXCEL, excelService);
        map.put(SupportedFileFormat.PDF, pdfService);
        return map;
    }
}
