package com.example.eventservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Slice;

import java.util.function.Function;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CriteriaUtil {

    public static <T, R> Page<R> mapPage(Slice<T> slice, Function<T, R> mapper) {
        var content = slice.getContent().stream()
                .map(mapper)
                .collect(Collectors.toList());

        return new PageImpl<>(content);
    }
}
