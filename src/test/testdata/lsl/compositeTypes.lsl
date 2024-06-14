libsl "1.0.0";
library simple;

typealias Int = int32;


automaton A : Int {

    fun *.compositeTypes() {
        var a: Int | Long | Byte = 5;
        var b: Int | Long | Byte = 5;
        var c: Int | Long & Byte = 5;
        var d: array<array<Int>> | Byte = 6;
    }
    
}