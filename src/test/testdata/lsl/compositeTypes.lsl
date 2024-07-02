libsl "1.0.0";
library simple;

typealias Byte = int8;
typealias Int = int32;
typealias Long = int64;

annotation Throws(
    exceptionTypes: Int | Long = 0
);

type StructureType {
    var field: Int | Long;
}

var globalFlag: Int | Long = 1;

automaton ListAutomaton (var size: Int | Long | 666, var anyListFlag: Int | Long & Byte) : Int | Long & Byte | 55  & "hello" | 's' {

    val defaultSize: Int | Long | 16 | 32 | "sixteen" = 16;

    proc compositeTypesProc_2(obj: Int | Long, obj2: 5, obj3: Int | Long & Byte): Int | Long | 666 {
    }
    
    fun *.compositeTypes_1(): Int | Long {
        var a: Int | Long | Byte = 5;
        var b: Int | Long | null = 5;
        var c: Int | Long & Byte = 5;
        var d: array<array<Int>> | Byte = 6;
        var e: array<array<Int>> | Byte | 5 | "anyString" = 5;
        result = 4;
    }
    
    fun *.compositeTypesReturnType_2(obj: Int | Long, obj2: 5, obj3: Int | Long & Byte): Int | Long | 666 {
    
    }
    
}