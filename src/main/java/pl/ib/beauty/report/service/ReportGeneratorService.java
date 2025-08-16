package pl.ib.beauty.report.service;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import pl.ib.beauty.mapper.UserMapper;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dao.UserCsv;
import pl.ib.beauty.service.CourseService;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportGeneratorService {
    private final CourseService courseService;
    private final UserMapper userMapper;

    @SneakyThrows
    public byte[] generateExcel(Long courseId) {
        Workbook workbook = WorkbookFactory.create(false);
        Sheet sheet = workbook.createSheet("Course Participants");
        int rowIndex = 0;
        Row headRow = sheet.createRow(rowIndex);
        headRow.createCell(0).setCellValue("Name");
        headRow.createCell(1).setCellValue("Last Name");
        headRow.createCell(2).setCellValue("Email");

        List<User> courseParticipants = courseService.getCourseParticipants(courseId);

        for (User courseParticipant : courseParticipants) {
            rowIndex++;
            Row row = sheet.createRow(rowIndex);
            row.createCell(0).setCellValue(courseParticipant.getFirstName());
            row.createCell(1).setCellValue(courseParticipant.getLastName());
            row.createCell(2).setCellValue(courseParticipant.getEmail());
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    @SneakyThrows
    public byte[] generateCsv(Long courseId) {
        List<User> courseParticipants = courseService.getCourseParticipants(courseId);
        List<UserCsv> userCsvs = courseParticipants.stream().map(userMapper::userToCsv)
                .toList();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream);
        StatefulBeanToCsv<UserCsv> beanToCsv = new StatefulBeanToCsvBuilder<UserCsv>(outputStreamWriter).withApplyQuotesToAll(false).build();
        beanToCsv.write(userCsvs);
        outputStreamWriter.flush();
        return byteArrayOutputStream.toByteArray();
    }
}
