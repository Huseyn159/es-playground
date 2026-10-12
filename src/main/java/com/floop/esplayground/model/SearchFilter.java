package com.floop.esplayground.model;

import com.floop.esplayground.enums.Operation;

public record SearchFilter(String field, Operation operation, String value) {}
