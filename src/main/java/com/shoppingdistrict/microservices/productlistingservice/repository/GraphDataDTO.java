package com.shoppingdistrict.microservices.productlistingservice.repository;

import java.util.List;

public class GraphDataDTO {
    private List<NodeDTO> nodes;
    private List<EdgeDTO> edges;

    // Getters and setters
    public List<NodeDTO> getNodes() { return nodes; }
    public void setNodes(List<NodeDTO> nodes) { this.nodes = nodes; }

    public List<EdgeDTO> getEdges() { return edges; }
    public void setEdges(List<EdgeDTO> edges) { this.edges = edges; }
}