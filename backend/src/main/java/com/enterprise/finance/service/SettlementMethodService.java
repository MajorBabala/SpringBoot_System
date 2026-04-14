package com.enterprise.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.finance.common.BusinessException;
import com.enterprise.finance.entity.SettlementMethod;
import com.enterprise.finance.mapper.SettlementMethodMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SettlementMethodService {

    private final SettlementMethodMapper settlementMethodMapper;

    public SettlementMethodService(SettlementMethodMapper settlementMethodMapper) {
        this.settlementMethodMapper = settlementMethodMapper;
    }

    public Page<SettlementMethod> page(int pageNum, int pageSize, String keyword, Integer status) {
        Page<SettlementMethod> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SettlementMethod> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(keyword), q -> q.like(SettlementMethod::getMethodCode, keyword.trim())
                .or()
                .like(SettlementMethod::getMethodName, keyword.trim()));
        wrapper.eq(status != null, SettlementMethod::getStatus, status);
        wrapper.orderByAsc(SettlementMethod::getSort).orderByDesc(SettlementMethod::getId);
        return settlementMethodMapper.selectPage(page, wrapper);
    }

    public SettlementMethod getById(Long id) {
        SettlementMethod item = settlementMethodMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(404, "Settlement method does not exist");
        }
        return item;
    }

    public Long create(SettlementMethod request) {
        validate(request, null);
        settlementMethodMapper.insert(request);
        return request.getId();
    }

    public void update(Long id, SettlementMethod request) {
        SettlementMethod exists = getById(id);
        request.setId(exists.getId());
        validate(request, id);
        settlementMethodMapper.updateById(request);
    }

    public void delete(Long id) {
        getById(id);
        settlementMethodMapper.deleteById(id);
    }

    private void validate(SettlementMethod request, Long id) {
        if (request == null) {
            throw new BusinessException(400, "Settlement method data is required");
        }
        if (!StringUtils.hasText(request.getMethodCode())) {
            throw new BusinessException(400, "Method code is required");
        }
        if (!StringUtils.hasText(request.getMethodName())) {
            throw new BusinessException(400, "Method name is required");
        }
        if (request.getStatus() == null) {
            request.setStatus(1);
        }
        if (request.getSort() == null) {
            request.setSort(0);
        }

        LambdaQueryWrapper<SettlementMethod> wrapper = new LambdaQueryWrapper<SettlementMethod>()
                .eq(SettlementMethod::getMethodCode, request.getMethodCode().trim());
        if (id != null) {
            wrapper.ne(SettlementMethod::getId, id);
        }
        if (settlementMethodMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "Method code already exists");
        }

        request.setMethodCode(request.getMethodCode().trim());
        request.setMethodName(request.getMethodName().trim());
    }
}
