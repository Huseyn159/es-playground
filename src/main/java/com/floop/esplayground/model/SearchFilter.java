package com.floop.esplayground.model;

import com.floop.esplayground.enums.Operation;

import java.util.List;

public record SearchFilter(String field, Operation operation, String value, List<String> values) {}
