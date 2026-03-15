package com.app.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class DepartmentTreeDto extends DepartmentDto {

    private List<DepartmentTreeDto> children = new ArrayList<>();

    public List<DepartmentTreeDto> getChildren() {
        return children;
    }

    public void setChildren(List<DepartmentTreeDto> children) {
        this.children = children;
    }
}
