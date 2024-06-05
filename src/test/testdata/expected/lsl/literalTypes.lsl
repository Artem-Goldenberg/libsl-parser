libsl "1.0.0";
library literalTypes;
typealias Int = int32;
automaton A : Int {
    fun *.localVariablesWithLiteralTypes(): 6 {
        var a: 5;
        val stringType_1: "hello";
        val stringType_2: "d" = "d";
        val doubleLiteralType_1: 2.1 = 2.1;
        val doubleLiteralType_2: -2.1E-5 = -2.1E-5;
        var integerLiteralArray: array<5>;
        val charType_1: 'F' = 'F';
        var literalMap: map<'U', map<"anyString", -2.1E-5>>;
        val literalBoolean_1: false = false;
        var literalBoolean_2: true;
        var nullType: null;
        result = 6;
    }
}