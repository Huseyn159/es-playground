package com.floop.esplayground.model;

import java.util.List;

public record SearchResult(long total, List<Contact> items) {}
