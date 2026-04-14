package com.enterprise.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.finance.entity.OperationLog;
import com.enterprise.finance.vo.OperationLogItemResponse;

import java.time.LocalDateTime;

public interface OperationLogService {

    void saveLog(OperationLog operationLog);

    IPage<OperationLogItemResponse> pageLogs(long page,
                                             long limit,
                                             String username,
                                             String operation,
                                             LocalDateTime dateFrom,
                                             LocalDateTime dateTo);

    String exportCsv(String username, String operation, LocalDateTime dateFrom, LocalDateTime dateTo);
}
