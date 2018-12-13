class FirstClass{
    def main1(): int {
        writeln(new Math().factorial(5));
        return 0;
    }
}

class SecondClass extends ThirdClass  {
    var z : string;
    def main3(q : string): int {
        var q : string;
        writeln(new Math().factorial(5));
        return 0;
    }
}


class ThirdClass extends FourthClass {
    def main2(q : string): int {
        var q : string;
        writeln(new Math().factorial(5));
        return 0;
    }
}


class FourthClass extends FifthClass {
    def main4(q : string): int {
        var q : string;
        writeln(new Math().factorial(5));
        return 0;
    }
}

class FifthClass extends SecondClass  {
    def main5(q : string): int {
        var q : string;
        writeln(new Math().factorial(5));
        return 0;
    }
}