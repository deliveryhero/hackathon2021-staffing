package com.deliveryhero.service;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

import com.deliveryhero.models.Demand;
import com.deliveryhero.models.Employee;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataService {
    public List<Demand> getDemandData() throws IOException {
        final List<Demand> result = new ArrayList<>();
        final CSVReader csvReader =
                new CSVReaderBuilder(new FileReader("data/se-borlange/demand.csv")).withSkipLines(1).build();
        for (final String[] row : csvReader.readAll()) {
            result.add(createDemand(row));
        }
        return result;
    }

    public List<Employee> getEmployeeData() throws IOException {
        final List<Employee> result = new ArrayList<>();
        final CSVReader csvReader =
                new CSVReaderBuilder(new FileReader("data/se-borlange/employees.csv")).withSkipLines(1).build();
        for (final String[] row : csvReader.readAll()) {
            result.add(createEmployee(row));
        }
        return result;
    }

    private LocalDateTime getTimestamp(final String[] row) {
        return LocalDateTime.of(
                parseInt(row[2]),
                parseInt(row[3]),
                parseInt(row[4]),
                parseInt(row[5]),
                parseInt(row[6]),
                parseInt(row[7]));
    }

    private Demand createDemand(final String[] row) {
        return new Demand(
                row[0],
                Instant.ofEpochSecond(parseInt(row[1])),
                getTimestamp(row),
                parseInt(row[8]),
                parseFloat(row[9]),
                parseFloat(row[10]));
    }
    
    private Employee createEmployee(final String[] row) {
        return new Employee(
                row[0],
                row[1],
                parseEmpData(row[2]),
                parseEmpData(row[3]),
                parseEmpData(row[4]),
                parseEmpData(row[5]),
                parseEmpData(row[6]),
                parseEmpData(row[7]),
                parseEmpData(row[8]),
                parseEmpData(row[9]),
                parseEmpData(row[10])
                );
    }

    private static int parseEmpData(final String cellContent) {
        if (cellContent == null) {
            return 0;
        }
        final String num = cellContent.replaceAll("[\\D]", "");
        return num.trim().isEmpty() ? 0 : Integer.parseInt(num);
    }
}