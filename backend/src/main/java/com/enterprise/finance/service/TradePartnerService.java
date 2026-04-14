package com.enterprise.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.finance.common.BusinessException;
import com.enterprise.finance.entity.TradePartner;
import com.enterprise.finance.mapper.TradePartnerMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TradePartnerService {

    private final TradePartnerMapper tradePartnerMapper;

    public TradePartnerService(TradePartnerMapper tradePartnerMapper) {
        this.tradePartnerMapper = tradePartnerMapper;
    }

    public Page<TradePartner> page(int pageNum, int pageSize, String keyword, Integer partnerType, Integer status) {
        Page<TradePartner> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TradePartner> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(keyword), q -> q.like(TradePartner::getPartnerCode, keyword.trim())
                .or()
                .like(TradePartner::getPartnerName, keyword.trim()));
        wrapper.eq(partnerType != null, TradePartner::getPartnerType, partnerType);
        wrapper.eq(status != null, TradePartner::getStatus, status);
        wrapper.orderByDesc(TradePartner::getId);
        return tradePartnerMapper.selectPage(page, wrapper);
    }

    public TradePartner getById(Long id) {
        TradePartner item = tradePartnerMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(404, "Trade partner does not exist");
        }
        return item;
    }

    public Long create(TradePartner request) {
        validate(request, null);
        tradePartnerMapper.insert(request);
        return request.getId();
    }

    public void update(Long id, TradePartner request) {
        TradePartner exists = getById(id);
        request.setId(exists.getId());
        validate(request, id);
        tradePartnerMapper.updateById(request);
    }

    public void delete(Long id) {
        getById(id);
        tradePartnerMapper.deleteById(id);
    }

    private void validate(TradePartner request, Long id) {
        if (request == null) {
            throw new BusinessException(400, "Trade partner data is required");
        }
        if (!StringUtils.hasText(request.getPartnerCode())) {
            throw new BusinessException(400, "Partner code is required");
        }
        if (!StringUtils.hasText(request.getPartnerName())) {
            throw new BusinessException(400, "Partner name is required");
        }
        if (request.getPartnerType() == null) {
            request.setPartnerType(1);
        }
        if (request.getStatus() == null) {
            request.setStatus(1);
        }

        LambdaQueryWrapper<TradePartner> wrapper = new LambdaQueryWrapper<TradePartner>()
                .eq(TradePartner::getPartnerCode, request.getPartnerCode().trim());
        if (id != null) {
            wrapper.ne(TradePartner::getId, id);
        }
        if (tradePartnerMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "Partner code already exists");
        }

        request.setPartnerCode(request.getPartnerCode().trim());
        request.setPartnerName(request.getPartnerName().trim());
    }
}
