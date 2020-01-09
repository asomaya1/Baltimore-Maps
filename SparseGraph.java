package hw8;

import exceptions.InsertionException;
import exceptions.PositionException;
import exceptions.RemovalException;

import java.util.ArrayList;
import java.util.List;

/**
    An implementation of a directed graph using incidence lists
    for sparse graphs where most things aren't connected.
    @param <V> Vertex element type.
    @param <E> Edge element type.
*/
public class SparseGraph<V, E> implements Graph<V, E> {

    // Class for a vertex of type V
    private final class VertexNode<V> implements Vertex<V> {
        V data;
        Graph<V, E> owner;
        List<Edge<E>> outgoing;
        List<Edge<E>> incoming;
        Object label;
        double dist;

        VertexNode(V v) {
            this.data = v;
            this.outgoing = new ArrayList<>();
            this.incoming = new ArrayList<>();
            this.label = null;
            this.dist = Double.POSITIVE_INFINITY;
        }

        @Override
        public V get() {
            return this.data;
        }

        @Override
        public void put(V v) {
            this.data = v;
        }
    }

    //Class for an edge of type E
    private final class EdgeNode<E> implements Edge<E> {
        E data;
        Graph<V, E> owner;
        VertexNode<V> from;
        VertexNode<V> to;
        Object label;

        // Constructor for a new edge
        EdgeNode(VertexNode<V> f, VertexNode<V> t, E e) {
            this.from = f;
            this.to = t;
            this.data = e;
            this.label = null;
        }

        @Override
        public E get() {
            return this.data;
        }

        @Override
        public void put(E e) {
            this.data = e;
        }

    }

    private List<Vertex<V>> vertices;
    private List<Edge<E>> edges;

    /** Constructor for instantiating a graph. */
    public SparseGraph() {
        this.vertices = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    // Checks vertex belongs to this graph
    private void checkOwner(VertexNode<V> toTest) {
        if (toTest.owner != this) {
            throw new PositionException();
        }
    }

    // Checks edge belongs to this graph
    private void checkOwner(EdgeNode<E> toTest) {
        if (toTest.owner != this) {
            throw new PositionException();
        }
    }

    // Converts the vertex back to a VertexNode to use internally
    private VertexNode<V> convert(Vertex<V> v) throws PositionException {
        try {
            VertexNode<V> gv = (VertexNode<V>) v;
            this.checkOwner(gv);
            return gv;
        } catch (ClassCastException ex) {
            throw new PositionException();
        }
    }

    // Converts and edge back to a EdgeNode to use internally
    private EdgeNode<E> convert(Edge<E> e) throws PositionException {
        try {
            EdgeNode<E> ge = (EdgeNode<E>) e;
            this.checkOwner(ge);
            return ge;
        } catch (ClassCastException ex) {
            throw new PositionException();
        }
    }

    @Override
    public Vertex<V> insert(V v) {
        // TODO
        VertexNode<V> newNode = new VertexNode<>(v);
        newNode.owner = this;
        vertices.add(newNode);
        return newNode;
    }

    @Override
    public Edge<E> insert(Vertex<V> from, Vertex<V> to, E e)
            throws PositionException, InsertionException {
        // TODO
        //standard convert and check owner on vertices
        VertexNode<V> fromNode = convert(from);
        VertexNode<V> toNode = convert(to);
        checkOwner(fromNode);
        checkOwner(toNode);
        //check if self-loop exists
        if (fromNode.equals(toNode)) {
            //System.out.println("threw exception A");
            throw new InsertionException();
        }
        //check if edge is duplicate
        for (int i =  0; i < fromNode.outgoing.size(); i++) {
            EdgeNode<E> cur = convert(fromNode.outgoing.get(i));
            if (cur.to.equals(toNode)) {
                //System.out.println("threw exception B");
                throw new InsertionException();
            }
        }
        //add edge 'from' to 'to'
        EdgeNode<E> newEdge = new EdgeNode<E>(fromNode, toNode, e);
        newEdge.owner = this;
        edges.add(newEdge);
        fromNode.outgoing.add(newEdge);
        toNode.incoming.add(newEdge);
        return newEdge;
    }

    @Override
    public V remove(Vertex<V> v) throws PositionException,
            RemovalException {
        // TODO
        //standard convert and check owner on vertices
        VertexNode<V> remNode = convert(v);
        checkOwner(remNode);
        //if there is at least one neighbor of v
        if (!(remNode.outgoing.isEmpty() && remNode.incoming.isEmpty())) {
            throw new RemovalException();
        }
        V val = remNode.data;
        //delete vertex from graph
        remNode.owner = null;
        vertices.remove(v);
        return val;
    }

    @Override
    public E remove(Edge<E> e) throws PositionException {
        // TODO
        //standard convert and check owner on edge
        EdgeNode<E> remEdge = convert(e);
        checkOwner(remEdge);
        E val = remEdge.data;
        //delete edge from adjacent vertices
        //Vertex<V> from = from(remEdge);
        VertexNode<V> fromNode = convert(from(remEdge));
        //Vertex<V> to = to(remEdge);
        VertexNode<V> toNode = convert(to(remEdge));
        toNode.incoming.remove(remEdge);
        fromNode.outgoing.remove(remEdge);

        //delete edge from graph
        remEdge.owner = null;
        edges.remove(remEdge);
        return val;
    }

    @Override
    public Iterable<Vertex<V>> vertices() {
        // TODO
        List<Vertex<V>> vertexCopy = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            vertexCopy.add(vertices.get(i));
        }
        return vertexCopy;
    }

    @Override
    public Iterable<Edge<E>> edges() {
        // TODO
        List<Edge<E>> edgeCopy = new ArrayList<>();
        for (int i = 0; i < edges.size(); i++) {
            edgeCopy.add(edges.get(i));
        }
        return edgeCopy;
    }

    @Override
    public Iterable<Edge<E>> outgoing(Vertex<V> v) throws PositionException {
        // TODO
        //standard convert and check owner on vertex
        VertexNode<V> node = convert(v);
        checkOwner(node);
        List<Edge<E>> outgoing = new ArrayList<>();
        for (int i = 0; i < node.outgoing.size(); i++) {
            outgoing.add(node.outgoing.get(i));
        }
        return outgoing;
    }

    @Override
    public Iterable<Edge<E>> incoming(Vertex<V> v) throws PositionException {
        // TODO
        //standard convert and check owner on vertex
        VertexNode<V> node = convert(v);
        checkOwner(node);
        List<Edge<E>> incoming = new ArrayList<>();
        for (int i = 0; i < node.incoming.size(); i++) {
            incoming.add(node.incoming.get(i));
        }
        return incoming;
    }

    @Override
    public Vertex<V> from(Edge<E> e) throws PositionException {
        // TODO
        //standard convert and check owner on edge
        EdgeNode<E> edge = convert(e);
        checkOwner(edge);
        return edge.from;
    }

    @Override
    public Vertex<V> to(Edge<E> e) throws PositionException {
        // TODO
        //standard convert and check owner on edge
        EdgeNode<E> edge = convert(e);
        checkOwner(edge);
        return edge.to;
    }

    @Override
    public void label(Vertex<V> v, Object l) throws PositionException {
        // TODO
        //standard convert and check owner on vertex
        VertexNode<V> node = convert(v);
        checkOwner(node);
        node.label = l;
    }

    @Override
    public void label(Edge<E> e, Object l) throws PositionException {
        // TODO
        //standard convert and check owner on edge
        EdgeNode<E> edge = convert(e);
        checkOwner(edge);
        edge.label = l;
    }

    @Override
    public Object label(Vertex<V> v) throws PositionException {
        // TODO
        //standard convert and check owner on vertex
        VertexNode<V> node = convert(v);
        checkOwner(node);
        return node.label;
    }

    @Override
    public Object label(Edge<E> e) throws PositionException {
        // TODO
        //standard convert and check owner on edge
        EdgeNode<E> edge = convert(e);
        checkOwner(edge);
        return edge.label;
    }

    @Override
    public void clearLabels() {
        // TODO
        for (int i = 0; i < vertices.size(); i++) {
            VertexNode<V> cur = convert(vertices.get(i));
            cur.label = null;
        }

        for (int i = 0; i < edges.size(); i++) {
            EdgeNode<E> cur = convert(edges.get(i));
            cur.label = null;
        }

    }

    private String vertexString(Vertex<V> v) {
        return "\"" + v.get() + "\"";
    }

    private String verticesToString() {
        StringBuilder sb = new StringBuilder();
        for (Vertex<V> v : this.vertices) {
            sb.append("  ").append(vertexString(v)).append("\n");
        }
        return sb.toString();
    }

    private String edgeString(Edge<E> e) {
        return String.format("%s -> %s [label=\"%s\"]",
                this.vertexString(this.from(e)),
                this.vertexString(this.to(e)),
                e.get());
    }

    private String edgesToString() {
        String edgs = "";
        for (Edge<E> e : this.edges) {
            edgs += "    " + this.edgeString(e) + ";\n";
        }
        return edgs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph {\n")
                .append(this.verticesToString())
                .append(this.edgesToString())
                .append("}");
        return sb.toString();
    }

    /**
     * Method to set distance of vertex.
     * @param v vertex whose distance is to be set
     * @param distance double to be set as vertex distance
     */
    public void setDist(Vertex<V> v, double distance) {
        VertexNode<V> vertex = convert(v);
        vertex.dist = distance;
    }

    /**
     * Method to get distance of vertex.
     * @param v vertex whose distance is desired.
     * @return double distance
     */
    public double getDist(Vertex<V> v) {
        VertexNode<V> vertex = convert(v);
        return vertex.dist;
    }

}
