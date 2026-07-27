package com.bestrookies.portfolio.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;

final class PaginationHeaders {

    private PaginationHeaders() {
    }

    static HttpHeaders from(Page<?> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Page-Number", String.valueOf(page.getNumber()));
        headers.add("X-Page-Size", String.valueOf(page.getSize()));
        headers.add("X-Total-Elements", String.valueOf(page.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(page.getTotalPages()));
        headers.add("X-Has-Next", String.valueOf(page.hasNext()));
        headers.add("X-Has-Previous", String.valueOf(page.hasPrevious()));
        return headers;
    }
}

