libsl "1.1.0";

library std
    version "11"
    language "unknown"
    url "-";

typealias Int = int32;
typealias IntHashMap = HashMap<Int, Int>;
typealias ArrayOfMaps = HashMap<HashMap<Int, map<Int, Int>>, Int>;

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
        K: out any,
        V: out any
{
    fun remove (key: K, value: V): void;
}

enum foo.vldf.Type <T, H> {
}

define action <T> PLAIN_GENERIC_ACTION(x: Int, s: T): T where T: any;
define action <T, R> COMPLICATED_GENERIC_ACTION(s: T): R where T: array<HashMap<in Int, out string>>, R: HashMap<HashMap<in string, out Int>, HashMap<string, Int>>;
define action <T> COMPLICATED_RETURN_TYPE_OF_GENERIC_ACTION(x: Int, s: T): HashMap<HashMap<T, R>, HashMap<T, R>> where T: any, R: string;

automaton A
(
    var storage: array<K>
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
        A<Int, Int>(newHashMap)._genericProc<Int, Int, Int>();
        result = newHashMap;
        action PLAIN_GENERIC_ACTION<Int>(5, 6);
        if (x is HashMap <Int, Int>) {

        }
        var obj: HashMap <Int, Int> = x as HashMap <Int, Int>;

        var newHashMapUnbounded: HashMap<?, ?> = new A<Int, Int>(state = Initialized);
        A<Int, Int>(newHashMapUnbounded)._genericProc<?, ?, ?>();
        action PLAIN_GENERIC_ACTION<?>(5, 6);
        if (x is HashMap<?, ?>) {

        }
        var obj2: HashMap <?, ?> = x as HashMap <?, ?>;
        var newHashMapBounded: HashMap<in Int, out String> = new A<Int, String>(state = Initialized);
        A<Int, Int>(newHashMapBounded)._genericProc<in Int, Int, out Int>();
        action PLAIN_GENERIC_ACTION<in Int>(5, 6);
        if (x is HashMap<in Int, out string>) {

        }
        var obj3: HashMap<in Int, out string> = x as HashMap<in Int, out string>;
        
        var newHashMapWithLiterals: HashMap<in Int, out String> = new A<5, "anyString">(state = Initialized);
        A<Int, Int>(newHashMapWithLiterals)._genericProc<5, 'J', "anyString">();
        if (x is HashMap<5.79, "anyString">) {

        }
        var obj4: HashMap<in Int, out string> = x as HashMap<5.79, "anyString">;
        A<Int, Int>(newHashMapBounded)._genericProc<in Int, Int, out Int>();
        var copyStorage: array<K> = A<Int, Int>(newHashMapBounded).storage;
    }

    fun *.ComplicatedWhere <T, R> (from: R, to: T): void where T: HashMap<HashMap<Int, map<Int, Int>>, Int>, R: out Int {
    }

    fun *.genericReturnType <T, R> (): HashMap<HashMap<T, R>, HashMap<T, R>> where T: any, R: any {
    }
    
    fun *.genericWhereLiteralTypes <T, R> (): void where T: 5, R: "anyString" {
    }
}