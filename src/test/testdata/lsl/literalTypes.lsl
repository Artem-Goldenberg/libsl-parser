libsl "1.0.0";
library literalTypes;

typealias Int = int32;
typealias Five = 5;

automaton A : Int {

    val defaultAnyFlag: 4 = 4;
    
    proc compositeTypesProc_1(): 666 {
    }
    
    fun *.localVariablesWithLiteralTypes(): 6 {
        var a: 5;
        var b: 5;
        var stringType_1: "hello";
        val stringType_2: "d" = "d";
        val doubleLiteralType_1: 2.1 = 2.1;
        val doubleLiteralType_2: -2.1E-5 = -2.1E-5;
        var integerLiteralArray: array<5>;
        val charType_1: 'F' = 'F';
        var literalMap: map<'U', map<"anyString", -2.1E-5>>;
        val literalBoolean_1: false = false;
        var literalBoolean_2: true;
        var nullType: null;
        var typealiasFiveType: Five = 5;
        result = 6;
    }
}