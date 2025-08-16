package pl.ib.beauty.report.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/excel")
    public ResponseEntity<byte[]> generateReportExcel(@RequestParam Long courseId) {
        byte[] file = excelGeneratorService.generateExcel(courseId);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length));
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=report.xls");
        return ResponseEntity.ok().headers(httpHeaders).body(file);
    }

    @GetMapping("/csv")
    public ResponseEntity<byte[]> generateReportCsv(@RequestParam Long courseId) {

        byte[] csvFile = excelGeneratorService.generateCsv(courseId);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(csvFile.length));
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=report%s.csv".formatted(courseId));
        return ResponseEntity.ok().headers(httpHeaders).body(csvFile);
    }
}
