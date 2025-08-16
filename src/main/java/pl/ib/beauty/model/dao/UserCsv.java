package pl.ib.beauty.model.dao;

import com.opencsv.bean.CsvBindByName;

public record UserCsv(
        @CsvBindByName(column = "email")
        String email,
        @CsvBindByName(column = "firstName")
        String firstName,
        @CsvBindByName(column = "lastName")
        String lastName) {
}
