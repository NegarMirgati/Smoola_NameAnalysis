package symbolTable;

import ast.Type.Type;

import java.util.ArrayList;

public class SymbolTableMethodItem extends SymbolTableItem {

    ArrayList<Type> argTypes = new ArrayList<>();
    Type returnType;

    public SymbolTableMethodItem(String name, ArrayList<Type> argTypes, Type retType) {
        this.name = name;
        this.argTypes = argTypes;
        this.returnType = retType;
    }

    public ArrayList<Type> getTypes(){
        return this.argTypes;
    }

    public Type getReturnType(){
        return this.returnType;
    }

    @Override
    public String getKey() {
        return this.name;
    }
}
