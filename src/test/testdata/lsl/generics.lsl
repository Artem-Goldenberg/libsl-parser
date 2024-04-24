///#! pragma: non-synthesizable
libsl "1.1.0";

library std
    version "11"
    language "Java"
    url "-";

typealias Int = int32;

// automata
type HashMap  <K, V>
    is java.util.HashMap
    for java.util.Map
    where
        K: any,
        V: any
{
}

type HashMapInOuParams  <in K, out V>
    is java.util.HashMap
    for java.util.Map
    where
        K: any,
        V: any
{
    fun remove (key: K, value: V): void;
}


define action <T> PLAIN_GENERIC_ACTION(x: Int, s: T): T where T: any;

automaton A
(
)
: HashMap <K, V>
{

    proc _genericFun <T, R, Q> (): void where T: any, R: any, Q: any
    {
    }

    proc _unGenericFun(): void {
    }

    proc _genericFunWithParams <T, R, Q> (a: T, b: Q): R where T: any, R: Int, Q: any {
    }

    proc _genericFunWithParametrizedArray <T, R, Q> (a: T, b: Q): array<R> where T: any, R: Int, Q: any {
    }

    proc _copy <T, R> (from: R, to: T): void where T: in any, R: out Int {
    }

    proc _print <T, S, Q> (t: T, s: S, args: array<Q>): Q where T: in Int, S: any, Q: out Int {
    }

    fun *.genericFun <T, R, Q> (): void where T: any, R: any, Q: any
    {
    }

    fun *.unGenericFun(): void {
    }

    fun *.genericFunWithParams <T, R, Q> (a: T, b: Q): R where T: any, R: Int, Q: any {
    }

    fun *.genericFunWithParametrizedArray <T, R, Q> (a: T, b: Q): array<R> where T: any, R: Int, Q: any {
    }

    fun *.copy <T, R> (from: R, to: T): void where T: in any, R: out Int {
    }

    fun *.print <T, S, Q> (t: T, s: S, args: array<Q>): Q where T: in Int, S: any, Q: out Int {
    }
}