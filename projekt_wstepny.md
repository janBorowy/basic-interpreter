# Techniki Kompilacji 24L
### Jan Borowy 318364

## Projekt
Celem jest zaprojektowanie języka interpretowanego i implementacja interpretera tego języka.

## Założenia projektowe
Język powinien spełniać narzucone wymagania:
- Interpretowany
- Silne typowanie
- Zmienne domyślnie są niemutowalne.
- Przekazywanie zmiennych do funkcji odbywa się przez referencję.
- Obsługa struktur
- Obsługa rekordu wariantowego

Przyjęte wymagania:
- Język będzie typowany statycznie.
- Zmienne domyślnie są niemutowalne, ale mogą być oznaczone jako mutowalne za
pośrednictwem słowa kluczowego `var`.
- Przykrywanie zmiennych w wypadku konfliktu nazw identyfikatorów.
- Przeciążanie funkcji nie jest możliwe.

## Przykłady wykorzystania języka

Przykładowe programy napisane w języku:
### Suma, punkt wejściowy programu, wyjście standardowe, komentarze
```
/*
    This is an
    example of
    multiline comment
*/
int main() { // main is an entry point of every program
    int a = 2; // Initialize immutable integer
    int b = 2;
    int sum = a + b; // Binary(two argument) addition function
    string str = (2 + 2) as string; // immutable string initialization
    print(str); // Built-in standard output function
    
    return 0; // main return exit code
}
```

### Niemutowalność i mutowalność

```
int main () {
    int a = 2;
    var int b = 2;
    // a = 3; ERROR!
    b = 3;
    return 0;
}
```

### Twierdzenia warunkowe

```
int main () {
    int a = 2;
    int b = 3;
    if(a % 2 == 0) {
        print("a variable's value is even");
    } else {
        print("a variable's value is uneven");
    }
    return 0;
}
```

### Pętla while
```
int main() {
    int i = 0;
    while(i < 10) {
        print(i as string);
        i = i + 1;
    }
    return 0;
}
```

### Struktury

```
struct Point {
    float x;
    float y;
}

int main() {
    Point point = Point(1, 2);
    // p.x = 2; ERROR!
    print(p.x); // p.x is read-only 
    return 0;
}
```

```
struct Person {
    string name;
    string surname;
}

struct Book {
    Person author;
    string title;
}

```

### Rekord wariantowy

```
struct Person {
    string name;
    string surname;
}

struct Book {
    string title;
    string isbn;
    Person author;
}

struct Article {
    string headline;
    string shownIn;
    Person author;
}

variant Publication {
    Book,
    Article
}

void printPublication(Publication pub) {
    match(pub) {
        Book book -> print("Book with title - " + book.title)
        Article article -> print("Article with headline - " + article.headline)
        default -> print("Unknown publication")
    }
}
```

### Definiowanie funkcji

```
struct IntPoint {
    int ix;
    int iy;
}

struct FloatPoint {
    float fx;
    float fy;
}

variant Point {
    IntPoint,
    FloatPoint
}

float getCoordinatesSum(Point p) {
    match(p) {
        IntPoint ip -> {
            int sum = ip.ix + ip.iy;
            return sum as float; 
        }
        FloatPoint fp -> return fp.fx + fp.fy;
    }
}
```

### Przykrywanie zmiennych

```

int main() {
    int a = 2;
    if(true) {
        int a = 3;
        print(a as string); // 3
    }
    print(a as string); // 2
}

```

### Rekurencja
```

int getNthFibonacciNumber(int n) {
    if(n == 0 or n == 1) {
        return n;
    }
    return getNthFibonacciNumber(n - 1) + getNthFibonacciNumber(n - 2);
}
```

## Gramatyka języka

```
program                  ::= { definition };
definition               ::= functionDefinition
                           | structureDefinition
                           | variantDefinition;
functionDefinition       ::= functionSignature "(" parameters ")" block;
structureDefinition      ::= "struct " identifier "{" parameters "}";
variantDefinition        ::= "variant " identifier "{" identifier { "," identifier } "}";
instruction              ::= block
                           | singleStatement
                           | compoundStatement;
block                    ::= "{" { singleOrCompoundStatement } "}";
singleOrCompoundStatement::= singleStatement
                           | compoundStatement;
singleStatement          ::= (identifierStatement
                           | "var" initialization // var initialization
                           | return) ";";
identifierStatement      ::= identifier ("(" [ expression {"," expression } ] ")" // function call
                           | "=" expression // assignment
                           | identifier "=" expression) // user type initialization
compoundStatement        ::= if
                           | while
                           | match;
initialization           ::= primitiveType identifier "=" expression; // primitive initialization
                           | identifier identifier "=" expression; // user type initialization
return                   ::= "return ", [expression];
while                    ::= "while", "(" expression ")", instruction;
functionCall             ::= identifier, arguments;
match                    ::= "match", "(", dotAccess, ")", "{", matchBranch, {matchBranch}, "}";
matchBranch              ::= identifier, identifier, "->" instruction;
cast                     ::= sum, ["as", primitiveType]
                           | stringLiteral, ["as", primitiveType];
sum                      ::= multiplication, {additionOperator, multiplication};
multiplication           ::= negation, {multiplicationOperator, negation};
additionOperator         ::= "+"
                           | "-";
multiplicationOperator   ::= "*"
                           | "/"
                           | "%";
negation                 ::= ["!"] factor;
factor                   ::= dotAccess
                           | number // integer or float literal
                           | booleanLiteral
                           | "(", expression, ")";
dotAccess                ::= identifierOrFunctionCall {"." identifier}
identifierOrFunctionCall ::= identifier ["("[ expression {"," expression } ]")"]
if                       ::= "if" "(" expression ")" instruction [ "else" instruction ];
expression               ::= alternative, {"and", alternative};
alternative              ::= relation, {"or", relation};
relation                 ::= cast, [relationalOperator, cast];
relationalOperator       ::= "=="
                           | "!="
                           | "<"
                           | ">"
                           | "<="
                           | ">=";
arguments                ::= "(", [ expression {"," expression } ], ")";
functionSignature        ::= "void", identifier,
                           | primitiveType, identifier
                           | identifier, identifier; // user return type
parameters               ::= [ parameterType, identifier { "," parameterType, identifier } ];
parameterType            ::= primitiveType
                           | identifier;
initializationSignature  ::= ["var "] primitiveType, identifier;
identifierList   ::= identifier { "," identifier };
primitiveType            ::= "int"
                           | "float"
                           | "string"
                           | "bool";
identifier               ::= identifierFirstCharacter, { digit | letter | "_" };
identifierFirstCharacter ::= "_" | letter;
booleanLiteral           ::= "true"
                           | "false";
stringLiteral            ::= '"'string'"';
string                   ::= { letter
                           | digit
                           | stringLegalWhitespace
                           | otherStringLegalCharacters };
number                   ::= ["-"], nonZeroDigit, {digit}
                          | "0"
                          | ["-"], digit, ".", digit, {digit};
digit                    ::= "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9";
nonZeroDigit             ::= "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9";
stringLegalWhitespace    ::= " ";
letter                   ::= "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j" | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" | "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z" | capitalLetter
capitalLetter            ::= "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" | "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z";
otherStringLegalCharacters ::= "~" | "!" | "@" | "#" | "$" | "%" | "^" | "&" | "*" | "(" | ")" | "-" | "_" | "=" | "+" | "[" | "{" | "]" | "}" | "|" | ";" | ":" | "'" | "," | "<" | "." | ">" | "/"
```

## Podstawowe typy danych
W języku dostępne są cztery podstawowe typy danych:
- Zmienne całkowitoliczbowe `int`
- Zmienne zmiennoprzecinkowe `float`
- Łańcuchy znaków `string`
- Zmienna boolowska `bool`

### Operacje
Dostępne są operatory:
- Operatory dodawania i odejmowania `+ -`
- Operatory mnożenia i dzielenia `* /`
- Operator modulo `%`
- Operatory koniunkcji i alternatywy `and or`
- Operatory równości i nierówności `== != < > <= >=`

Operacje na zmiennych możliwe są tylko wtedy, jeśli po obu stronach operatora zmienne są
tego samego typu. Przy czym:
- Dla zmiennych typu `string` dostępny jest tylko operator `+`, który oznacza konkatonację.
- Dla zmiennych typu `boolean` dostępne są wyłącznie operatory `and or`
- Dla zmiennych typu `int` lub `float` dostępne są operatory `+ - / % > < <= >=`

| Typ zmiennych | Dostępne operatory                |
|---------------|-----------------------------------|
| int, float    | `+` `-` `/` `%` `>` `<` `<=` `>=` |
| boolean       | `and` `or`                        |
| string        | `+`                               |

W przypadku struktur operatory są niedozwolone.

## Funkcje
Funkcje definiuje się w następujący sposób:
``` 
int add(int a, int b) {
    return a + b;
}

void printHello() {
    print("Hello");
    return; // Not required
}
```

`void` to specjalny typ zwracany przez funkcję, który nie zwraca żadnej wartości.
Funkcje niezwracające wartości są nieprawidłowym wyrazem operacji.
np. twierdzenie `add(1, 2) + printHelloWord()` jest błędne.

## Struktury
Struktury to typy danych definiowane przez użytkownika. Zawierają one pola, czyli stałe
wartości dowolnego typu.

### Deklaracja
Wewnątrz definicji struktury mogą znajdować się tylko deklaracje jej pól. Nazwa struktury musi zaczynać się
z wielkiej litery.
```
struct Point {
    float x;
    float y;
}
```

### Inicjalizacja
Inicjalizacja struktury odbywa się poprzez konstruktor `NazwaStruktury(wartościPól...)`.

### Niemutowalność zmiennych
Zmienne wewnątrz struktury są niemutowalne. Wewnątrz deklaracji struktury słowo kluczowe
`var` jest zakazane.

Sama zmienna typu struktury jest mutowalna, może przyjąć inną wartość:
```
struct Point {
    float x;
    float y;
}

int main() {
    var Point p = Point(1.5, 1.25);
    p = Point(1.2, 1.25);
    // p.x = 3.5 ERROR!
}
```

### Struktury zagnieżdżone
Istnieje możliwość dowolnego zagnieżdżania struktur. W przypadku użycia zmiennych przy
inicjalizacji struktur, zmienne są klonowane - wszystkie ich pola są kopiowane i tworzona
jest nowa struktura o identycznych polach.
```
struct Point {
    float x;
    float y;
}

struct Vector {
    Point beginning;
    Point end;
}


int main() {
    Point beginning = Point(1.5, 1.25);
    Point end = Point(3.5, 0.5);
    Vector force = Point(beginning, end);
}
```

## Konwersja

Konwersja typów odbywa się przez słowo kluczowe `as`.

| Typ 1  | Typ 2  | Konwersja 1 -> 2 | Wynik |
|--------|--------|------------------|-------|
| int    | float  | `1 as float`     | 1.0   |
| int    | string | `123 as string`  | "123" |
| float  | int    | `1.5 as int`     | 1     |
| float  | string | `1.5 as string`  | "1.5" |
| string | int    | `123 as int`     | 123   |
| string | float  | `"1.5" as float` | 1.5   |

#### Uwagi:

- W wypadku błędnej konwersji, zostanie zgłoszony błąd interpretacji i koniec programu, np. konwersja `"abc" as int`.
- Konwersja liczby zmiennoprzecinkowej na całkowitoprzecinkową powoduje zaokroąglenie liczby w dół do liczby jedności.
- Konwersja nie jest dozwolona między strukturami i typami boolowskimi.

## Rekord wariantowy
Typ zmiennej, który przechowuje wartość jednego z wariantów - struktur.

### Deklaracja
Wariant deklaruje się poprzez słowo kluczowe `variant` i wypisane kolejno dostępne warianty:
```
struct IntPoint {
    int ix;
    int iy;
}

struct FloatPoint {
    float fx;
    float fy;
}

variant Point {
    IntPoint,
    FloatPoint
}
```
### Słowo kluczowe match
Rekord wariantowy nie posiada żadnych pól. Należy uprzednio dopasować typ struktury przy
pomocy słowa kluczowego `match`:

```
void printPoint(Point p) {
    match(p) {
        IntPoint intPoint -> print("x: " + ix as string + " y: " + iy as string);
        FloatPoint floatPoint -> print("x: " + fx as string + " y: " + fy as string);
        default -> print("Hard to tell");
    }
}
```
Słowo kluczowe `default` dopasowuje każdy wariant i nie inizcjalizuje zmiennej powiązanej
z wariantem. Należy obsłużyć wszystkie możliwe warianty, inaczej interpretacja kończy się błędem.

## Obsługa błędów
W przypadku błędu interpretacji, program powinien natychmiastowo zakończyć działanie i wskazać adekwadtny błąd.
Struktura wiadomości błędu zawiera komunikat czytelny dla człowieka oraz dokładne miejsce w strumieniu
danych, w którym błąd wystąpił. Miejsce określane jest przez linię i kolumnę, gdzie następne linie wyznaczane są przez
znak nowej linii (najczęściej jest to `"\n"`), a kolumna to pozycja znaku w aktualnej linii.
Jeśli błąd jest obsługiwalny, czyli kod może być dalej interpretowany bez konieczności zakończenia interpretacji
z błędem, zastosuje on domyślne działanie pozwalające na dalsze wykonanie kodu.

### Obsługiwalne przypadki i działanie
- Brak `;` na końcu wyrażenia - dodanie `;` przez interpreter i kontynuacja interpretacji

### Przykładowe komunikaty błędu:
- `Invalid conversion at line 5, col 10: "a.bc" is not convertable.`
- `Invalid type name at line 1, col 1: double is not a valid type name.`
- `Invalid operation at line 5, col 10: 1 + "abc": operand types do not match`
- `Undefined structure at line 150, col 25: structure "Matrix" is undefined`
- `Undefined function at line 120, col 1: function "apply" was not defined`

## Struktura projektu
```mermaid
flowchart TD
    sourceCode((Kod źródłowy))
    lexicalAnalyzer(Analizator leksykalny)
    syntaxAnalyzer(Analizator składniowy)
    semanticAnalyzer(Analizator semantyczny)
    executer(Wykonawca)
    errorHandling(Obsługa błędów)
    
    sourceCode --> lexicalAnalyzer --> syntaxAnalyzer --> semanticAnalyzer --> executer
    errorHandling --> sourceCode & lexicalAnalyzer & syntaxAnalyzer & semanticAnalyzer & executer
    
```
