package io.elastest.etm.model;

import java.util.Map;

public class TJobSupportService extends SupportService {
    private boolean selected;

    public TJobSupportService() {
        super();
    }

    public TJobSupportService(String id, String name, String shortName,
            Map<String, Object> config, boolean selected) {
        super(id, name, shortName, config);
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return "TJobSupportService [selected=" + selected + "]" + " "
                + super.toString();
    }

}
