class Test2 {
	def main() : int {
		i = 3+5 * 5 || i + (34/5 && 2);
		arr = new int[666];
		writeln(arr.length); # the output is 666
		return 0;
	}
}

class Test3 extends Bro{
	var jj : int[];
	var kk : boolean;
	def main3() : int {
		i = 3+5 * 5 || i + (34/5 && 2)+0;
		arr = new int[666];
		writeln(arr.length); # the output is 666
		return 0;
	}
}

class Test21 extends Test2{
	var jj : int[];
	var kk : boolean;
	def main2() : int {
		i = 3+5 * 5 || i + (34/5 && 2)+0;
		arr = new int[666];
		writeln(arr.length); # the output is 666
		return 0;
	}
	def main3() : int {
		i = 3+5 * 5 || i + (34/5 && 2);
		arr = new int[666];
		writeln(arr.length); # the output is 666
		return 0;
	}
}

class Bro{
	var j : int[];
	var k : boolean;
	def main2() : int {
		i = 3+5 * 5 || i + (34/5 && 2)+0;
		arr = new int[666];
		writeln(arr.length); # the output is 666
		return 0;
	}
}