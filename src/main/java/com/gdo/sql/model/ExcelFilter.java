package com.gdo.sql.model;

import java.sql.ResultSet;

public interface ExcelFilter {
    public abstract boolean isRowValid(ResultSet rs);
    public abstract boolean isColumnValid(ResultSet rs, int index);
}
