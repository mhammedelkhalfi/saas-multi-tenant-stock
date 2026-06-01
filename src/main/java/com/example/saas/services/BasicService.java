package com.example.saas.services;

import com.example.saas.common.PageResponse;

public interface BasicService <I,O>{
    void create(final I request);
    void update(final String id,final I request);
    void delete(final String id);
    O get(final String id);
    PageResponse<O> getAll(int page, int size);
    PageResponse<O> search(String keyword, int page, int size);
}
