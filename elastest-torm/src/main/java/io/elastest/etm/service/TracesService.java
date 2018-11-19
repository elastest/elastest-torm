package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import io.elastest.etm.dao.TraceRepository;
import io.elastest.etm.model.Enums.LevelEnum;
import io.elastest.etm.model.Enums.StreamType;
import io.elastest.etm.model.Trace;
import io.elastest.etm.utils.UtilTools;
import io.elastest.etm.utils.UtilsService;
import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;

@Service
public class TracesService {
    final Logger logger = getLogger(lookup().lookupClass());

    private final TraceRepository traceRepository;
    private final QueueService queueService;
    private final UtilsService utilsService;

    GrokCompiler grokCompiler;

    @Value("${grok.patterns.file.path}")
    private String grokPatternsFilePath;

    String javaLogLevelExpression = "%{JAVALOGLEVEL:level}";
    String containerNameExpression = "%{CONTAINERNAME:containerName}";
    String monitoringExecExpression = "(\\d+|ext\\d+_e\\d+|s\\d+_e\\d+)";
    String componentExecAndComponentServiceExpression = "^(?<component>(test|sut|dynamic))_?(?<exec>"
            + monitoringExecExpression
            + ")(_(?<componentService>[^_]*(?=_\\d*)?))?";

    String cleanMessageExpression = "^([<]\\d*[>].*)?(?>test_"
            + monitoringExecExpression + "|sut_" + monitoringExecExpression
            + "|dynamic_" + monitoringExecExpression
            + ")\\D*(?>_exec)(\\[.*\\])?[\\s][-][\\s]";

    String startsWithTestOrSutExpression = "^(test|sut)(_)?(\\d*)(.*)?";

    String dockbeatStream = "et_dockbeat";

    @Autowired
    public TracesService(TraceRepository traceRepository,
            QueueService queueService, UtilsService utilsService) {
        this.traceRepository = traceRepository;
        this.queueService = queueService;
        this.utilsService = utilsService;
    }

    @PostConstruct
    private void init() throws IOException {
        grokCompiler = GrokCompiler.newInstance();
        grokCompiler.registerDefaultPatterns();

        InputStream inputStream = getClass()
                .getResourceAsStream("/" + grokPatternsFilePath);
        grokCompiler.register(inputStream, StandardCharsets.UTF_8);
    }

    public Map<String, String> processGrokExpression(String message,
            String expression) {
        Grok compiledPattern = grokCompiler.compile(expression);
        Map<String, Object> map = compiledPattern.match(message).capture();
        Map<String, String> resultMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                resultMap.put(entry.getKey(), (String) entry.getValue());
            }

        }
        // As <String,String> Map
        return resultMap;
    }

    public Trace cleanCommonFields(Trace trace, String message) {
        // Message
        if (message != null) {
            message = message.replaceAll(cleanMessageExpression, "");
            trace.setMessage(message);
        }

        // Change containerName and component dashes to underscores
        if (trace.getContainerName() != null) {
            trace.setContainerName(
                    trace.getContainerName().replaceAll("-", "_"));
        }
        if (trace.getComponent() != null) {
            trace.setComponent(trace.getComponent().replaceAll("-", "_"));
        }

        if (trace.getComponentService() != null) {
            trace.setComponentService(
                    trace.getComponentService().replaceAll("-", "_"));
        }
        return trace;
    }

    public Trace matchesLevelAndContainerNameFromMessage(Trace trace,
            String message) {
        if (message != null) {
            // Level
            Map<String, String> levelMap = processGrokExpression(message,
                    javaLogLevelExpression);
            try {
                LevelEnum level = LevelEnum.fromValue(levelMap.get("level"));
                trace.setLevel(level);
            } catch (Exception e) {

            }

            // Container Name

            String containerName = this.getContainerNameFromMessage(message);
            if (containerName != null) {
                trace.setContainerName(containerName);
            }
        }
        return trace;
    }

    public String getContainerNameFromMessage(String message) {
        if (message != null) {
            // Container Name
            Map<String, String> containerNameMap = processGrokExpression(
                    message, containerNameExpression);
            return containerNameMap.get("containerName");
        } else {
            return null;
        }
    }

    public void saveTrace(Trace trace) {
        synchronized (this.traceRepository) {
            this.traceRepository.save(trace);
        }
    }

    /* *********** */
    /* *** TCP *** */
    /* *********** */

    public void processTcpTrace(String message, Date timestamp) {
        logger.trace("Processing TCP trace {} with timestamp {}", message,
                timestamp);

        if (message != null && !message.isEmpty()) {
            try {
                Trace trace = new Trace();
                trace.setEtType("et_logs");
                trace.setStream("default_log");
                trace.setStreamType(StreamType.LOG);

                // Timestamp
                trace.setTimestamp(
                        utilsService.getIso8601UTCDateFromDate(timestamp));

                // If message, set level and container name
                trace = this.matchesLevelAndContainerNameFromMessage(trace,
                        message);

                // Exec, Component and Component Service
                Map<String, String> componentExecAndComponentServiceMap = processGrokExpression(
                        trace.getContainerName(),
                        componentExecAndComponentServiceExpression);
                if (componentExecAndComponentServiceMap != null
                        && !componentExecAndComponentServiceMap.isEmpty()) {
                    trace.setExec(
                            componentExecAndComponentServiceMap.get("exec"));
                    trace.setComponent(componentExecAndComponentServiceMap
                            .get("component"));
                    trace.setComponentService(
                            componentExecAndComponentServiceMap
                                    .get("componentService"));
                }

                trace = cleanCommonFields(trace, message);

                if (trace.getComponentService() != null) {
                    trace.setComponent(trace.getComponent() + "_"
                            + trace.getComponentService());
                }

                logger.trace("Trace: {}", trace);
                this.saveTrace(trace);
                this.queueService.sendTrace(trace);
            } catch (Exception e) {
                logger.trace("Error on processing TCP trace {}: ", message, e);
            }
        }
    }

    /* ************* */
    /* *** Beats *** */
    /* ************* */

    public Trace setInitialBeatTraceData(Map<String, Object> dataMap)
            throws ParseException {
        Trace trace = new Trace();
        trace.setComponent((String) dataMap.get("component"));
        trace.setComponentService((String) dataMap.get("componentService"));
        trace.setContainerName((String) dataMap.get("containerName"));
        trace.setEtType((String) dataMap.get("et_type"));
        trace.setExec((String) dataMap.get("exec"));
        trace.setLevel(LevelEnum.fromValue((String) dataMap.get("level")));
        trace.setMessage((String) dataMap.get("message"));
        trace.setMetricName((String) dataMap.get("metricName"));
        trace.setStream((String) dataMap.get("stream"));
        trace.setStreamType(
                StreamType.fromValue((String) dataMap.get("stream_type")));

        String timestampAsStr = (String) dataMap.get("@timestamp");
        if (timestampAsStr == null) {
            timestampAsStr = utilsService.getIso8601UTCStrFromDate(new Date());
        }

        trace.setTimestamp(
                utilsService.getIso8601UTCDateFromStr(timestampAsStr));
        trace.setUnit((String) dataMap.get("unit"));
        trace.setUnits((String) dataMap.get("units"));

        return trace;
    }

    @SuppressWarnings("unchecked")
    public boolean processBeatTrace(Map<String, Object> dataMap,
            boolean fromDockbeat) {
        logger.trace("Processing BEATS trace {}", dataMap.toString());
        boolean procesed = false;

        if (dataMap != null && !dataMap.isEmpty()) {

            try {
                Trace trace = setInitialBeatTraceData(dataMap);
                trace.setRawData(dataMap.get("raw_data").toString());

                // Ignore Packetbeat from EIM temporally
                if (trace.getStream() != null
                        && "et_packetbeat".equals(trace.getStream())) {
                    return false;
                }

                if (fromDockbeat) {
                    trace.setStream(dockbeatStream);
                }
                // If message, set level and container name
                trace = this.matchesLevelAndContainerNameFromMessage(trace,
                        (String) dataMap.get("message"));

                if (trace.getLevel() == null && dataMap.containsKey("level")) {
                    try {
                        LevelEnum level = LevelEnum
                                .fromValue(dataMap.get("level").toString());
                        trace.setLevel(level);
                    } catch (Exception e) {
                    }

                }

                String component = trace.getComponent();

                // Docker
                String[] containerNameTree = new String[] { "docker",
                        "container", "name" };
                String containerName = (String) UtilTools.getMapFieldByTreeList(
                        dataMap, Arrays.asList(containerNameTree));
                if (containerName != null) {
                    trace.setContainerName(containerName);
                    // Metricbeat
                    if (dataMap.get("metricset") != null) {
                        if (component != null) {
                            trace.setComponent(component + "_" + containerName);
                        }
                    } else {// Filebeat
                        if (component == null) {
                            // from etm filebeat, discard non sut/test
                            // containers
                            if (!containerName
                                    .matches(startsWithTestOrSutExpression)) {
                                logger.error(
                                        "Filebeat trace without component and container name {} does not matches sut/test, discarding",
                                        containerName);
                                return false;
                            }
                        } else {
                            trace.setComponent(component + "_" + containerName);
                        }
                        if (dataMap.get("json") != null) {

                            String[] jsonLogTree = new String[] { "json",
                                    "log" };

                            String message = (String) UtilTools
                                    .getMapFieldByTreeList(dataMap,
                                            Arrays.asList(jsonLogTree));

                            if (message != null) {
                                trace.setMessage(message);
                            }

                        } else {
                            String log = (String) dataMap.get("log");
                            if (log != null) {
                                trace.setMessage(log);
                            }

                        }
                    }
                }

                // Exec, Component and Component Service
                if (trace.getContainerName() != null) {
                    Map<String, String> componentExecAndComponentServiceMap = processGrokExpression(
                            trace.getContainerName(),
                            componentExecAndComponentServiceExpression);
                    if (componentExecAndComponentServiceMap != null
                            && !componentExecAndComponentServiceMap.isEmpty()) {
                        trace.setExec(componentExecAndComponentServiceMap
                                .get("exec"));
                        trace.setComponent(componentExecAndComponentServiceMap
                                .get("component"));
                        trace.setComponentService(
                                componentExecAndComponentServiceMap
                                        .get("componentService"));
                    }
                }

                trace = cleanCommonFields(trace, trace.getMessage());

                if (trace.getMessage() != null) {
                    trace.setStreamType(StreamType.LOG);
                }

                // Its Metric
                if (trace.getStreamType() == null
                        || !trace.getStreamType().equals(StreamType.LOG)) {
                    // Dockbeat
                    if (trace.getStream() == null) {
                        return false;
                    }
                    if (trace.getStream().equals(dockbeatStream)) {
                        if (trace.getContainerName() != null
                                && trace.getContainerName().matches(
                                        startsWithTestOrSutExpression)) {
                            trace.setStreamType(StreamType.COMPOSED_METRICS);
                            if (trace.getComponentService() != null) {
                                trace.setComponent(trace.getComponent() + "_"
                                        + trace.getComponentService());
                            }
                            trace.setEtType((String) dataMap.get("type"));
                            trace.setMetricName(trace.getEtType());
                            trace.setContentFromLinkedHashMap(
                                    (LinkedHashMap<Object, Object>) dataMap
                                            .get(trace.getEtType()));

                        } else {
                            logger.trace(
                                    "Dockbeat trace container name {} does not matches sut/test, discarding",
                                    trace.getContainerName());
                            return false;
                        }
                    } else {
                        if (dataMap.get("metricset") != null) {
                            String[] metricsetModuleTree = new String[] {
                                    "metricset", "module" };
                            String metricsetModule = (String) UtilTools
                                    .getMapFieldByTreeList(dataMap,
                                            Arrays.asList(metricsetModuleTree));

                            String[] metricsetNameTree = new String[] {
                                    "metricset", "name" };
                            String metricsetName = (String) UtilTools
                                    .getMapFieldByTreeList(dataMap,
                                            Arrays.asList(metricsetNameTree));

                            String metricName = metricsetModule + "_"
                                    + metricsetName;
                            trace.setEtType(metricName);
                            trace.setMetricName(metricName);

                            String[] contentTree = new String[] {
                                    metricsetModule, metricsetName };
                            LinkedHashMap<Object, Object> content = (LinkedHashMap<Object, Object>) UtilTools
                                    .getMapFieldByTreeList(dataMap,
                                            Arrays.asList(contentTree));

                            trace.setContentFromLinkedHashMap(content);

                            if (trace.getStreamType() == null) {
                                trace.setStreamType(
                                        StreamType.COMPOSED_METRICS);
                            }

                        } else {
                            // HTTP custom metrics
                            try {
                                trace.setContentFromLinkedHashMap(
                                        (LinkedHashMap<Object, Object>) dataMap
                                                .get(trace.getEtType()));
                            } catch (ClassCastException cce) {
                                try {
                                    trace.setContent((String) dataMap
                                            .get(trace.getEtType()));
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                } else { // log
                    trace.setEtType("et_logs");

                    if (trace.getStream() == null) {
                        trace.setStream("default_log");
                    }

                    if (trace.getComponentService() != null) {
                        trace.setComponent(trace.getComponent() + "_"
                                + trace.getComponentService());
                    }
                }

                logger.trace("Trace: {}", trace);
                this.saveTrace(trace);
                procesed = true;
                this.queueService.sendTrace(trace);
            } catch (Exception e) {
                logger.error("Error on processing Beat trace {}: ", dataMap, e);
            }
        }
        return procesed;
    }

    /* ************ */
    /* *** HTTP *** */
    /* ************ */

    @SuppressWarnings("unchecked")
    public void processHttpTrace(Map<String, Object> dataMap) {
        logger.trace("Processing HTTP trace {}", dataMap.toString());
        if (dataMap != null && !dataMap.isEmpty()) {
            List<String> messages = (List<String>) dataMap.get("messages");
            // Multiple messages
            if (messages != null) {
                logger.trace("Is multiple message trace. Spliting...");
                for (String message : messages) {
                    Map<String, Object> currentMap = new HashMap<>();
                    currentMap.putAll(dataMap);
                    currentMap.remove("messages");
                    currentMap.put("message", message);
                    this.processHttpTrace(currentMap);
                    return;
                }
            } else {
                this.processBeatTrace(dataMap, false);
            }
        }
    }

    /* ********************* */
    /* *** Elasticsearch *** */
    /* ********************* */
    public Map<String, Object> convertExternalElasticsearchTrace(
            Map<String, Object> dataMap) {
        logger.trace("Converting external Elasticsearch trace {}",
                dataMap.toString());
        if (dataMap != null && !dataMap.isEmpty()) {
            
            // Add raw data
            try {
                Gson gson = new Gson();
                String json = gson.toJson(dataMap);
                dataMap.put("raw_data", json);
            } catch (Exception e) {
            }
            

            // Stream
            if (dataMap.containsKey("log_type")) {
                dataMap.put("stream", dataMap.get("log_type"));
                dataMap.put("stream_type", StreamType.LOG.toString());
            } else if (dataMap.containsKey("type")) {
                dataMap.put("stream", dataMap.get("log_type"));
            } else {
                return dataMap;
            }

            // Message
            if (dataMap.containsKey("description")) {
                dataMap.put("stream_type", StreamType.LOG.toString());
                dataMap.put("message", dataMap.get("description"));
            } else if (dataMap.containsKey("description_clean")) {
                dataMap.put("stream_type", StreamType.LOG.toString());
                dataMap.put("message", dataMap.get("description_clean"));
            }

            // Level
            if (dataMap.containsKey("severity")) {
                dataMap.put("stream_type", StreamType.LOG.toString());
                LevelEnum level = LevelEnum
                        .fromValue(dataMap.get("severity").toString());
                if (level != null) {
                    dataMap.put("level", level.toString());
                }
            } else if (dataMap.containsKey("severity_unified")) {
                dataMap.put("stream_type", StreamType.LOG.toString());
                LevelEnum level = LevelEnum
                        .fromValue(dataMap.get("severity_unified").toString());
                if (level != null) {
                    dataMap.put("level", level.toString());
                }
            }

        }
        return dataMap;
    }
}
