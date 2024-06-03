libsl "1.0.0";
library literalTypes;
typealias Int = int32;
automaton A : Int {
    fun *.localVariablesWithLiteralTypes() {
        var a: 5;
        var stringType: "hello";
    }
}