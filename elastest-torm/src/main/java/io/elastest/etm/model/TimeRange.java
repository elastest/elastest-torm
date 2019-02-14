package io.elastest.etm.model;

import java.util.Date;

public class TimeRange {
    Date gt;
    Date gte;
    Date lt;
    Date lte;

    public TimeRange() {
    }

    public TimeRange(TimeRange timeRange) {
        this.gt = timeRange.gt;
        this.gte = timeRange.gte;
        this.lt = timeRange.lt;
        this.lte = timeRange.lte;
    }

    public Date getGt() {
        return gt;
    }

    public void setGt(Date gt) {
        this.gt = gt;
    }

    public Date getGte() {
        return gte;
    }

    public void setGte(Date gte) {
        this.gte = gte;
    }

    public Date getLt() {
        return lt;
    }

    public void setLt(Date lt) {
        this.lt = lt;
    }

    public Date getLte() {
        return lte;
    }

    public void setLte(Date lte) {
        this.lte = lte;
    }

    @Override
    public String toString() {
        return "TimeRange [gt=" + gt + ", gte=" + gte + ", lt=" + lt + ", lte="
                + lte + "]";
    }

    public boolean isEmpty() {
        return this.gt == null && this.gte == null && this.lt == null
                && this.lte == null;
    }

}
