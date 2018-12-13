class FirstClass{
    def main1(): int {
        writeln(new Math().factorial(5));
        return 0;
    }
}

class SecondClass extends ThirdClass  {
    var q : string;
    var q : boolean;
    def main2(q : string): int {
        var q : string;
        writeln(new Math().factorial(5));
        return 0;
    }
}


class ThirdClass extends SecondClass {
    var q : string;
    var q : boolean;
    def main2(q : string): int {
        var q : string;
        writeln(new Math().factorial(5));
        return 0;
    }
}