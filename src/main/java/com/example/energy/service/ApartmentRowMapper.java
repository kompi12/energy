package com.example.energy.service;

import com.example.energy.model.Apartment;

import java.io.IOException;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Stream;

@FunctionalInterface
public interface ApartmentRowMapper<T> {
    Stream<T> map(Apartment apartment, List<YearMonth> months) throws IOException;;
}
