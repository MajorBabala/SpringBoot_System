package com.enterprise.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enterprise.finance.entity.Dept;
import com.enterprise.finance.mapper.DeptMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeptService {

    private final DeptMapper deptMapper;

    public DeptService(DeptMapper deptMapper) {
        this.deptMapper = deptMapper;
    }

    public List<Dept> listEnabledDepts() {
        return deptMapper.selectList(
                new QueryWrapper<Dept>()
                        .eq("status", 1)
                        .orderByAsc("sort")
                        .orderByAsc("id")
        );
    }
}
