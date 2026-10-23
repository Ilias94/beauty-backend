package pl.ib.beauty.report.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.ib.beauty.report.service.ReportGeneratorService;

@RestController
@RequestMapping(value = "/api/v1/report", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ReportController {
    private final ReportGeneratorService excelGeneratorService;

    /** The course's participant list as a spreadsheet: names and e-mails, so only its instructor (or an admin). */
    @ApiResponse(content = @Content(schema = @Schema(type = "string", format = "binary")))
    @GetMapping("/excel")
    @PreAuthorize("isAuthenticated() and (hasAuthority('SCOPE_ADMIN') or @securityService.isCourseCreator(#courseId))")
    public ResponseEntity<byte[]> generateReportExcel(@RequestParam Long courseId) {
        byte[] file = excelGeneratorService.generateExcel(courseId);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length));
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=uczestnicy-kurs-%s.xls".formatted(courseId));
        return ResponseEntity.ok().headers(httpHeaders).body(file);
    }
}
