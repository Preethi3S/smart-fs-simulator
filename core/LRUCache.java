package core;

import java.util.*;

public class LRUCache<K, V> {
    private final int capacity;
    private final Map<K, Node<K, V>> map = new HashMap<>();
    private final DoublyLinkedList<K, V> list = new DoublyLinkedList<>();

    public LRUCache(int capacity) {
        this.capacity = capacity;
    }

    public V get(K key) {
        if (!map.containsKey(key)) return null;
        Node<K, V> node = map.get(key);
        list.moveToFront(node);
        return node.value;
    }

    public void put(K key, V value) {
        if (map.containsKey(key)) {
            Node<K, V> node = map.get(key);
            node.value = value;
            list.moveToFront(node);
        } else {
            if (map.size() >= capacity) {
                Node<K, V> last = list.removeLast();
                map.remove(last.key);
            }
            Node<K, V> newNode = new Node<>(key, value);
            list.addFirst(newNode);
            map.put(key, newNode);
        }
    }

    private static class Node<K, V> {
        K key; V value;
        Node<K, V> prev, next;
        Node(K key, V value) { this.key = key; this.value = value; }
    }

    private static class DoublyLinkedList<K, V> {
        Node<K, V> head, tail;

        public void addFirst(Node<K, V> node) {
            if (head == null) {
                head = tail = node;
            } else {
                node.next = head;
                head.prev = node;
                head = node;
            }
        }

        public void moveToFront(Node<K, V> node) {
            if (node == head) return;
            if (node == tail) {
                tail = tail.prev;
                tail.next = null;
            } else {
                node.prev.next = node.next;
                node.next.prev = node.prev;
            }
            node.next = head;
            node.prev = null;
            head.prev = node;
            head = node;
        }

        public Node<K, V> removeLast() {
            Node<K, V> node = tail;
            if (tail.prev != null) {
                tail = tail.prev;
                tail.next = null;
            } else {
                head = tail = null;
            }
            return node;
        }
    }
}