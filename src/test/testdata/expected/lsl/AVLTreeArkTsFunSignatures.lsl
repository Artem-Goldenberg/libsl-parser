libsl "1.1.0";
library std version "11" language "unknown" url "-";

typealias int = int32;
typealias Object = any;
typealias boolean = bool;

type Comparable <T> is java.lang.Comparable for any  where T: any{
    fun *.compareTo(t: T): int;
}

type AVLTree is java.util.AvlTreeType for any {
}

type Node <K, V> is java.util.Node for any  where K: in Comparable<any>, V: any{
}

automaton NodeAutomaton : Node<K, V> {
    constructor NodeAutomaton(@target() self: Node<K, V>, left: Node<K, V>, k: K, v: V, height: int, right: Node<K, V>) {
    }
}

automaton AVLTreeAutomaton : AVLTree {
    proc _isEmpty <K, V> (n: Node<K, V>): boolean where K: in Comparable<Object>, V: any {
        result = n == null;
    }
    
    proc _isLeaf <K, V> (n: Node<K, V>): boolean where K: in Comparable<Object>, V: any {
    }

    fun *.newEmpty <K, V> (@target() self: AVLTree): Node<K, V> where K: in Comparable<Object>, V: any {
        result = null;
    }

    fun *.isEmpty <K, V> (@target() self: AVLTree, n: Node<K, V>): boolean where K: in Comparable<Object>, V: any {
        result = _isEmpty<K, V>(n);
    }

    fun *.isLeaf <K, V> (@target() self: AVLTree, n: Node<K, V>): boolean where K: in Comparable<Object>, V: any {
        result = _isLeaf<K, V>(n);
    }

    fun *.isNode <K, V> (@target() self: AVLTree, n: Node<K, V>): boolean where K: in Comparable<Object>, V: any {
    }

    fun *.height <K, V> (@target() self: AVLTree, n: Node<K, V>): int where K: in Comparable<Object>, V: any {
        if (_isEmpty<K, V>(n)) 
            result = 0;
        else
            if (_isLeaf<K, V>(n)) 
                result = 1;
    }
    
    fun *.newNode <K, V> (@target() self: AVLTree, left: Node<K, V>, k: K, v: V, height: int, right: Node<K, V>): Node<K, V> where K: in Comparable<Object>, V: any {
    }
}