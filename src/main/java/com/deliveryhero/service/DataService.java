package com.deliveryhero.service;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

import com.deliveryhero.models.Demand;
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
            result.add(new Demand(
                    row[0],
                    Instant.ofEpochSecond(parseInt(row[1])),
                    getTimestamp(row),
                    parseInt(row[8]),
                    parseFloat(row[9]),
                    parseFloat(row[10])));
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
}
