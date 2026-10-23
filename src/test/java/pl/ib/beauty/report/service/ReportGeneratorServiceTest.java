package pl.ib.beauty.report.service;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import pl.ib.beauty.model.Status;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.Payment;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.repository.CourseRepository;
import pl.ib.beauty.repository.PaymentRepository;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportGeneratorServiceTest {

    private final CourseRepository courseRepository = mock(CourseRepository.class);
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final ReportGeneratorService service = new ReportGeneratorService(courseRepository, paymentRepository);

    @Test
    void listsParticipantsByLastNameWithWhatTheyPaid() throws Exception {
        User zofia = User.builder().id(1L).firstName("Zofia").lastName("Wiśniewska").email("zofia@test.pl").build();
        User anna = User.builder().id(2L).firstName("Anna").lastName("Testowa").email("anna@test.pl").build();
        Course course = Course.builder().id(16L).title("Stylizacja rzęs").maxParticipants(12)
                .startDate(LocalDateTime.of(2026, 10, 28, 10, 0)).endDate(LocalDateTime.of(2026, 10, 29, 17, 0))
                .participants(new ArrayList<>(List.of(zofia, anna))).build();
        when(courseRepository.findById(16L)).thenReturn(Optional.of(course));
        when(paymentRepository.findByCourseIdAndStatus(16L, Status.SUCCESS)).thenReturn(List.of(
                Payment.builder().user(anna).amount(new BigDecimal("450.00")).status(Status.SUCCESS).build()));

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(service.generateExcel(16L)))) {
            Sheet sheet = workbook.getSheet("Uczestnicy");

            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Stylizacja rzęs");
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("28.10.2026 10:00 – 29.10.2026 17:00");
            assertThat(sheet.getRow(2).getCell(0).getStringCellValue()).isEqualTo("Zapisanych: 2 z 12");
            assertThat(sheet.getRow(4).getCell(4).getStringCellValue()).isEqualTo("Zapłacono (zł)");
            // Sorted by last name: Testowa before Wiśniewska.
            assertThat(sheet.getRow(5).getCell(2).getStringCellValue()).isEqualTo("Testowa");
            assertThat(sheet.getRow(5).getCell(4).getNumericCellValue()).isEqualTo(450.0);
            assertThat(sheet.getRow(6).getCell(2).getStringCellValue()).isEqualTo("Wiśniewska");
            assertThat(sheet.getRow(6).getCell(4).getStringCellValue()).isEqualTo("—");
        }
    }
}
