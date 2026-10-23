package pl.ib.beauty.report.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ib.beauty.model.Status;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.Payment;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.repository.CourseRepository;
import pl.ib.beauty.repository.PaymentRepository;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportGeneratorService {
    private static final String[] HEADERS = {"Lp.", "Imię", "Nazwisko", "E-mail", "Zapłacono (zł)", "Obecność"};
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final CourseRepository courseRepository;
    private final PaymentRepository paymentRepository;

    /**
     * The participant list an instructor prints or keeps for the day: the course and its dates on top, then one
     * row per person with what they paid and an empty "Obecność" column to tick.
     */
    @SneakyThrows
    @Transactional(readOnly = true)
    public byte[] generateExcel(Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        Map<Long, BigDecimal> paid = paymentRepository.findByCourseIdAndStatus(courseId, Status.SUCCESS).stream()
                .collect(Collectors.toMap(payment -> payment.getUser().getId(), Payment::getAmount, BigDecimal::add));
        List<User> participants = course.getParticipants().stream()
                .sorted(Comparator.comparing(User::getLastName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        try (Workbook workbook = WorkbookFactory.create(false)) {
            Sheet sheet = workbook.createSheet("Uczestnicy");
            CellStyle bold = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            bold.setFont(boldFont);

            Row title = sheet.createRow(0);
            title.createCell(0).setCellValue(course.getTitle());
            title.getCell(0).setCellStyle(bold);
            String dates = course.getStartDate() == null ? "" : course.getStartDate().format(DATE)
                    + (course.getEndDate() == null ? "" : " – " + course.getEndDate().format(DATE));
            sheet.createRow(1).createCell(0).setCellValue(dates);
            sheet.createRow(2).createCell(0).setCellValue("Zapisanych: %d z %d".formatted(participants.size(), course.getMaxParticipants()));

            Row head = sheet.createRow(4);
            for (int i = 0; i < HEADERS.length; i++) {
                head.createCell(i).setCellValue(HEADERS[i]);
                head.getCell(i).setCellStyle(bold);
            }
            int rowIndex = 5;
            for (User participant : participants) {
                Row row = sheet.createRow(rowIndex);
                row.createCell(0).setCellValue(rowIndex - 4);
                row.createCell(1).setCellValue(orEmpty(participant.getFirstName()));
                row.createCell(2).setCellValue(orEmpty(participant.getLastName()));
                row.createCell(3).setCellValue(orEmpty(participant.getEmail()));
                BigDecimal amount = paid.get(participant.getId());
                if (amount != null) {
                    row.createCell(4).setCellValue(amount.doubleValue());
                } else {
                    // Enrolled without a recorded payment, e.g. a free course or added by hand.
                    row.createCell(4).setCellValue("—");
                }
                row.createCell(5).setCellValue("");
                rowIndex++;
            }
            sheet.createFreezePane(0, 5);
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            sheet.setColumnWidth(5, Math.max(sheet.getColumnWidth(5), 14 * 256));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
