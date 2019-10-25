package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.elastest.etm.model.MonitoringQuery;

@Service
public class AsyncMonitoringService {
    protected final Logger logger = getLogger(lookup().lookupClass());

    private AbstractMonitoringService monitoringService;
    private DatabaseSessionManager dbmanager;

    public AsyncMonitoringService(AbstractMonitoringService monitoringService,
            DatabaseSessionManager dbmanager) {
        this.monitoringService = monitoringService;
        this.dbmanager = dbmanager;
    }

    @Async
    public void compareLogsPairAsync(MonitoringQuery body, String comparison,
            String view, String timeout, String processId) throws Exception {
        dbmanager.bindSession();
        monitoringService.compareLogsPairAsync(body, comparison, view, timeout,
                processId);
        dbmanager.unbindSession();
    }

    @Async
    public void deleteMonitoringDataByIndicesAsync(List<String> indices) {
        dbmanager.bindSession();
        monitoringService.deleteMonitoringDataByIndices(indices);
        dbmanager.unbindSession();
    }
}
