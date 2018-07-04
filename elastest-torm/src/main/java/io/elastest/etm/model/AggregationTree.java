package io.elastest.etm.model;

import java.util.ArrayList;
import java.util.List;

public class AggregationTree {
    String name;
    List<AggregationTree> children;

    public AggregationTree() {
        children = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AggregationTree> getChildren() {
        return children;
    }

    public void setChildren(List<AggregationTree> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "{name: \"" + name + "\", children: " + children.toString()
                + "}";
    }

}
