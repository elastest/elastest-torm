package io.elastest.etm.model.external;

import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.elastest.etm.model.Enums.ProtocolEnum;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ExternalPrometheus extends ExternalMonitoringDB {

    public interface ExternalPrometheusView {
    }

    /* ************************** */
    /* ****** Constructors ****** */
    /* ************************** */

    public ExternalPrometheus() {
    }

    public ExternalPrometheus(Long id, String ip, String port, String path,
            ProtocolEnum protocol, String user, String pass) {
        super(id, ip, port, path, protocol, user, pass);
    }

    public ExternalPrometheus(ExternalPrometheus externalPrometheus) {
        super(externalPrometheus);
    }

    /* *************************** */
    /* *** Getters and setters *** */
    /* *************************** */

    @Override
    public String toString() {
        return "ExternalPrometheus [toString()=" + super.toString() + "]";
    }

}
