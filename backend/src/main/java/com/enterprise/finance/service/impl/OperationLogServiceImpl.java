package com.enterprise.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.finance.entity.OperationLog;
import com.enterprise.finance.mapper.OperationLogMapper;
import com.enterprise.finance.service.OperationLogService;
import com.enterprise.finance.vo.OperationLogItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLog(OperationLog operationLog) {
        if (operationLog.getCreateTime() == null) {
            operationLog.setCreateTime(LocalDateTime.now());
        }
        if (operationLog.getDuration() == null || operationLog.getDuration() < 0) {
            operationLog.setDuration(0);
        }
        operationLogMapper.insert(operationLog);
    }

    @Override
    public IPage<OperationLogItemResponse> pageLogs(long page,
                                                    long limit,
                                                    String username,
                                                    String operation,
                                                    LocalDateTime dateFrom,
                                                    LocalDateTime dateTo) {
        Page<OperationLog> pageParam = new Page<>(page, limit);
        QueryWrapper<OperationLog> query = buildQuery(username, operation, dateFrom, dateTo);
        query.orderByDesc("id");

        IPage<OperationLog> result = operationLogMapper.selectPage(pageParam, query);
        List<OperationLogItemResponse> records = result.getRecords().stream().map(this::toResponse).toList();

        Page<OperationLogItemResponse> responsePage = new Page<>();
        responsePage.setCurrent(result.getCurrent());
        responsePage.setSize(result.getSize());
        responsePage.setTotal(result.getTotal());
        responsePage.setRecords(records);
        return responsePage;
    }

    @Override
    public String exportCsv(String username, String operation, LocalDateTime dateFrom, LocalDateTime dateTo) {
        QueryWrapper<OperationLog> query = buildQuery(username, operation, dateFrom, dateTo);
        query.orderByDesc("id").last("limit 10000");
        List<OperationLog> logs = operationLogMapper.selectList(query);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder builder = new StringBuilder();
        builder.append("id,userId,username,operation,method,params,ip,duration,createTime\n");
        for (OperationLog log : logs) {
            builder.append(log.getId()).append(",")
                    .append(log.getUserId() == null ? "" : log.getUserId()).append(",")
                    .append(escapeCsv(log.getUsername())).append(",")
                    .append(escapeCsv(log.getOperation())).append(",")
                    .append(escapeCsv(log.getMethod())).append(",")
                    .append(escapeCsv(log.getParams())).append(",")
                    .append(escapeCsv(log.getIp())).append(",")
                    .append(log.getDuration() == null ? 0 : log.getDuration()).append(",")
                    .append(log.getCreateTime() == null ? "" : formatter.format(log.getCreateTime()))
                    .append("\n");
        }
        return builder.toString();
    }

    private QueryWrapper<OperationLog> buildQuery(String username,
                                                  String operation,
                                                  LocalDateTime dateFrom,
                                                  LocalDateTime dateTo) {
        return new QueryWrapper<OperationLog>()
                .like(StringUtils.hasText(username), "username", username)
                .like(StringUtils.hasText(operation), "operation", operation)
                .ge(dateFrom != null, "create_time", dateFrom)
                .le(dateTo != null, "create_time", dateTo);
    }

    private OperationLogItemResponse toResponse(OperationLog log) {
        OperationLogItemResponse item = new OperationLogItemResponse();
        item.setId(log.getId());
        item.setUserId(log.getUserId());
        item.setUsername(log.getUsername());
        item.setOperation(log.getOperation());
        item.setMethod(log.getMethod());
        item.setParams(log.getParams());
        item.setIp(log.getIp());
        item.setDuration(log.getDuration());
        item.setCreateTime(log.getCreateTime());
        return item;
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (!value.contains(",") && !value.contains("\"") && !value.contains("\n")) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
