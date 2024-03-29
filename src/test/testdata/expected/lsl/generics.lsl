libsl "1.1.0";
library std version "11" language "Java" url "-";
typealias Int = int32;
automaton A : Int {
    <T, R, Q> fun *.genericFun(): void {
    }

    fun *.unGenericFun(): void {
    }

    <T, R, Q> fun *.genericFunWithParams(a: T, b: Q): R {
    }

    <T, R, Q> fun *.genericFunWithParametrizedArray(a: T, b: Q): array<R> {
    }

    <in T, out R> fun *.copy(from: R, to: T): void {
    }

    <in T, out Q, S> fun *.print(t: T, s: S, args: array<Q>): Q where T: Int, Q: Int {
    }
}