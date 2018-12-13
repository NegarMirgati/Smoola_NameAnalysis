package ast.Type;

public abstract class Type {
    protected int lineNumber;
    public abstract String toString();
    public int getLine() {return this.lineNumber;}
    public void setLine(int line_) {this.lineNumber = line_ ;}
}
