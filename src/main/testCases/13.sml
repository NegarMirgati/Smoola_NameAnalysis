class FirstClass{
    def main(): int {
        writeln(new Math().factorial(5));
        return 0;
    }
}

class SecondClass extends ClassWhichDoesNotExists  {
    var q : string;
    var q : boolean;
    def main2(q : string): int {
        var q : string;
        writeln(new Math().factorial(5));
        return 0;
    }
}