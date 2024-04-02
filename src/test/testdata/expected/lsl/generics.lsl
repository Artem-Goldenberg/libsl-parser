libsl "1.1.0";
library std version "11" language "Java" url "-";
typealias Int = int32;
type HashMap <K, V> is java.util.HashMap for java.util.Map {
}
automaton A : Int {
    generic fun *.genericFun(): void where T: any, R: any, Q: any {
    }

    fun *.unGenericFun(): void {
    }

    generic fun *.genericFunWithParams(a: T, b: Q): R where T: any, R: Int, Q: any {
    }

    generic fun *.genericFunWithParametrizedArray(a: T, b: Q): array<R> where T: any, R: Int, Q: any {
    }

    generic fun *.copy(from: R, to: T): void where T: in any, R: out Int {
    }

    generic fun *.print(t: T, s: S, args: array<Q>): Q where T: in Int, Q: out Int, S: any {
    }
}