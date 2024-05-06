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

type HashMapInOuParams  <K, V>
    is java.util.HashMap
    for java.util.Map
    where
        K: in any,
        V: out any
{
    fun remove (key: K, value: V): void;
}


define action <T> PLAIN_GENERIC_ACTION(x: Int, s: T): T where T: any;

automaton A
(
)
: HashMap <K, V>
{

    proc _genericProc <T, R, Q> (): void where T: any, R: any, Q: any
    {
    }

    proc _unGenericProc(): void {
    }

    proc _genericProcWithParams <T, R, Q> (a: T, b: Q): R where T: any, R: Int, Q: any {
    }

    proc _genericProcWithParametrizedArray <T, R, Q> (a: T, b: Q): array<R> where T: any, R: Int, Q: any {
    }

    proc _copyProc <T, R> (from: R, to: T): void where T: in any, R: out Int {
    }

    proc _printProc <T, S, Q> (t: T, s: S, args: array<Q>): Q where T: in Int, S: any, Q: out Int {
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

    fun *.procUsage (@target self: HashMap <K, V>): void {
        _genericFunWithParams<Int, Int, Int>(4, 5);
    }

    fun *.genericTypeDefBlockReturnType (x: K): HashMap <Int, Int> {
        var newHashMap: HashMap<Int, Int> = new A<Int, Int>(state = Initialized);
        A(newHashMap)._genericProc<Int, Int, Int>();
        result = newHashMap;
        action PLAIN_GENERIC_ACTION<Int>(5, 6);
        if (x is HashMap <Int, Int>) {

        }
        var obj: HashMap <Int, Int> = x as HashMap <Int, Int>;
        var newHashMapUnbounded: HashMap<?, ?> = new A<?, ?>(state = Initialized);
    }
}