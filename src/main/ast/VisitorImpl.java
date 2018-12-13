package ast;

import ast.node.Program;
import ast.node.declaration.ClassDeclaration;
import ast.node.declaration.MethodDeclaration;
import ast.node.declaration.VarDeclaration;
import ast.node.expression.*;
import ast.node.expression.Value.BooleanValue;
import ast.node.expression.Value.IntValue;
import ast.node.expression.Value.StringValue;
import ast.node.statement.*;
import java.util.ArrayList;
import java.lang.*;
import symbolTable.*;
import ast.Type.*;
import ast.Type.UserDefinedType.*;
import java.util.*;

public class VisitorImpl implements Visitor {

    private int numPassedRounds = 0;
    private Boolean hasErrors = false;
    private Program program;
    public int number_of_repeated_method = 0;
    public int index_variable = 0;
    public int index_class = 0;
    public int index_keyword = 0;
    HashMap <String, HashMap<String, SymbolTableItem>> classSymTables;

    public Boolean IsKeyWord(String word)
    {
      String KeyWords[] = {"return", "class","extends","new", "int", "char", "string", "boolean", "if", "then", "else","while",
       "var", "def", "writeln", "true", "false", "length"};
       for(int i=0; i<KeyWords.length;i++){
           if(word== KeyWords[i]){
            //System.out.print("This word is keyword and you can not get it for identifier");
            return false; 
           }
       }   
      return true;
    }

    public VisitorImpl(Program p){
        classSymTables = new  HashMap<String, HashMap<String, SymbolTableItem>>();
        this.program = p;
    }

    public void putGlobalVar(String name , Type type) throws ItemAlreadyExistsException{
        SymbolTable.top.put( new SymbolTableVariableItem(name,type,index_variable));
    }

    public void checkVariableName(VarDeclaration varDeclaration, int parentLine){
        String name = varDeclaration.getIdentifier().getName();
        Type type=varDeclaration.getType();
        index_variable += 1;
        try {
                putGlobalVar(name,type);

            }catch(ItemAlreadyExistsException e) {
                hasErrors = true;
                int lineToShow = Math.max(varDeclaration.getLine(), parentLine);
                System.out.println(String.format("Line:%d:Redefinition of Variable %s", lineToShow, name));
                String new_name = name + "Temporary_" + Integer.toString(index_variable);
            
            try{
                putGlobalVar(new_name, type);
            
            }catch(ItemAlreadyExistsException ee){
                System.out.println("OOOOOOOOOOOOOOOOPS!");
            }
            }
    }

    public void checkVarOfMethod(VarDeclaration varDeclaration, int parentLine){
        int idx = 0;
        String name = varDeclaration.getIdentifier().getName();
        
        Type type=varDeclaration.getType();
        idx += 1;
        try {
                putGlobalVar(name,type);

            }catch(ItemAlreadyExistsException e) {
                hasErrors = true;
                int lineToShow = Math.max(varDeclaration.getLine(), parentLine);
                System.out.println(String.format("Line:%d:Redefinition of Variable %s", lineToShow, name));
                String new_name = name + "Temporary_" + Integer.toString(idx);
            
            try{
                putGlobalVar(new_name, type);
            
            }catch(ItemAlreadyExistsException ee){
                System.out.println("OOOOOOOOOOOOOOOOPS!");
            }
            }
    }

    public void addVarOfMethod(VarDeclaration varDeclaration, int parentLine){
        String name = varDeclaration.getIdentifier().getName();
        
        Type type = varDeclaration.getType();
        index_variable += 1;
        try {
                putGlobalVar(name,type);
            }catch(ItemAlreadyExistsException e) {
                String new_name = name + "Temporary_" + Integer.toString(index_variable);
            try{
                putGlobalVar(new_name, type);
            
            }catch(ItemAlreadyExistsException ee){
                System.out.println("OOOOOOOOOOOOOOOOPS!");
            }
        }
    }

    public void put_class(String name,Type type)throws ItemAlreadyExistsException{
        SymbolTable.top.put( new SymbolTableClassItem(name,type,index_class));
    }

    public void checkClassNames(ClassDeclaration classDeclaration){
        String name= classDeclaration.getName().getName();
        UserDefinedType class_type= new UserDefinedType();
        class_type.setClassDeclaration(classDeclaration);
        index_class+=1;

        try{
            put_class(name, class_type);
        }catch(ItemAlreadyExistsException e){

            System.out.println(String.format("Line:%d:Redefinition of class %s", classDeclaration.getLine(), name));
            hasErrors = true;
            String new_name = name + "Temporary_" + Integer.toString(index_class);
            
            try{
                put_class(name, class_type);
            }
            catch(ItemAlreadyExistsException ee){}
        }
    }

    public void put_method(String name, ArrayList<VarDeclaration> argTypes)throws ItemAlreadyExistsException{
        ArrayList<Type>types = new ArrayList<Type>();
        for(int i=0;i<argTypes.size(); i++){
            types.add(argTypes.get(i).getType());
        }
        SymbolTable.top.put(new SymbolTableMethodItem(name,types));
    }

    public void checkMethodName(MethodDeclaration methodDeclaration, int parentLine){
        String methodname = methodDeclaration.getName().getName();
        ArrayList<VarDeclaration> argTypes = new ArrayList<>(methodDeclaration.getArgs());
        argTypes= methodDeclaration.getArgs();
        try{
            put_method(methodname,argTypes);
        }catch(ItemAlreadyExistsException e){
 
            hasErrors = true;
            int lineToShow = Math.max(methodDeclaration.getLine(), parentLine);
            System.out.println(String.format("Line:%d:Redefinition of method %s", lineToShow, methodname));

            String new_name = methodname + "Temporary_" + Integer.toString(number_of_repeated_method);
            number_of_repeated_method+=1;
            try{
            put_method(new_name,argTypes);
            }
            catch(ItemAlreadyExistsException ee){}
        }  
    }

    public int getLineOfParentMethod(String methodname, String parentName, Program program){

        ClassDeclaration pc = findClass(parentName, program);
        if(pc == null)
            return -1;
        ArrayList<MethodDeclaration> methods = pc.getMethodDeclarations();
        for(int i = 0 ; i < methods.size(); i++){
            if(methods.get(i).getName().getName().equals(methodname))
                return methods.get(i).getLine();
        }
        return -1;
    }

    public int getLineOfParentVar(String varName, String parentName, Program program){

        ClassDeclaration pc = findClass(parentName, program);
        if(pc == null)
            return -1;
        ArrayList<VarDeclaration> vars = pc.getVarDeclarations();
        for(int i = 0 ; i < vars.size(); i++){
            if(vars.get(i).getIdentifier().getName().equals(varName))
                return vars.get(i).getLine();
        }
        return -1;
    }

    public void checkInsideClass(ClassDeclaration cd, Program program){

        checkVariableNamesInsideClass(cd, program);
        ArrayList<MethodDeclaration> mtds = cd.getMethodDeclarations();     
        for(int i = 0; i < mtds.size(); i++){
            checkMethodNameInsideClass(cd, mtds.get(i), program);
            checkInsideMethod(cd, mtds.get(i), program);
        }       
    }

    public void checkVariableNamesInsideClass(ClassDeclaration cd, Program program){
        
        ArrayList<VarDeclaration> variableDecs = cd.getVarDeclarations();

        for(int i = 0; i < variableDecs.size(); i++){
            String varName = variableDecs.get(i).getIdentifier().getName();
            
            int parentLine = -1;
            if(cd.getParentName() != null)
                parentLine = getLineOfParentVar(varName, cd.getParentName().getName(), program);

            checkVariableName(variableDecs.get(i), parentLine);
        }
    }

    public void checkMethodNameInsideClass(ClassDeclaration cd, MethodDeclaration md, Program program){

            String varName = md.getName().getName();
            int parentLine = -1;
            if(cd.getParentName() != null){
                parentLine = getLineOfParentMethod(varName, cd.getParentName().getName(), program);
            }
            checkMethodName(md, parentLine);
    }

    public void checkVariablesOfMethod(ArrayList<VarDeclaration> variableDecs, Program program){
        for(int i = 0; i < variableDecs.size(); i++){
            String varName = variableDecs.get(i).getIdentifier().getName();
            checkVariableName(variableDecs.get(i), -1);
        }
    }

    public void checkVarsOfMethod(ArrayList<VarDeclaration> variableDecs, Program program){
        SymbolTable.top.push(new SymbolTable());

        for(int i = 0; i < variableDecs.size(); i++){
            String varName = variableDecs.get(i).getIdentifier().getName();
            checkVarOfMethod(variableDecs.get(i), -1);
        }

        SymbolTable.top.pop();
    }

    public void addVarsOfMethod(ArrayList<VarDeclaration> variableDecs, Program program){
        for(int i = 0; i < variableDecs.size(); i++){
            String varName = variableDecs.get(i).getIdentifier().getName();
            addVarOfMethod(variableDecs.get(i), -1);
        }
    }
   
    public void checkInsideMethod(ClassDeclaration cd, MethodDeclaration md,  Program p){
            
            SymbolTable.top.push(SymbolTable.top);

            checkVarsOfMethod(md.getArgs(), p);
            addVarsOfMethod(md.getArgs(), p);

            checkVarsOfMethod(md.getLocalVars(), p);
            addVarsOfMethod(md.getLocalVars(), p);

            checkForInvalidIndexOfNewArray(md, p);

            SymbolTable.top.pop();
    }
    
    public void checkForInvalidIndexOfNewArray(MethodDeclaration md, Program p){
            visit(md);
    }

    public ClassDeclaration findClass(String parentName, Program p){
        ArrayList <ClassDeclaration> classDecs = getAllClassDeclarations(p);
        for(int i = 0; i  < classDecs.size(); i++){
            if(classDecs.get(i).getName().getName().equals(parentName)){
                return classDecs.get(i);
            }
        }   
        return null;
    }

    public ArrayList<ClassDeclaration> getAllClassDeclarations(Program p){
        ArrayList<ClassDeclaration> classDecs = new ArrayList<> ();
        classDecs.add(p.getMainClass());
        classDecs.addAll(p.getClasses());
        return classDecs;
    }
    /* phase 3 */

    public boolean isUserDefinedType(Type t){
        String typeName = t.toString();
        if(typeName.equals("int") || typeName.equals("string") || typeName.equals("int[]"))
            return true;
        return false;
    }

    public boolean isSubType(Type t1, Type t2){
        if (t1.toString().equals(t2.toString())){
            return true;
        }
        else if(isUserDefinedType(t1) && isUserDefinedType(t2)){
            String t1ClassName = t1.toString();
            String t2ClassName = t2.toString();

            ClassDeclaration c1 = findClass(t1ClassName, this.program);
            Identifier parentName = c1.getParentName();
            while(parentName != null){
                if(parentName.getName().equals(t2ClassName))
                    return true;
                else{
                    ClassDeclaration parentClass = findClass(parentName.getName(), this.program);
                    parentName = parentClass.getName();
                }
            }
        }
        return false;
    }
    /* End of Phase 3*/

    @Override
    public void visit(Program program) {
        if(numPassedRounds == 0){ // pass 1
            SymbolTable.top.push(new SymbolTable());
            hasErrors = false;
            numPassedRounds += 1;
        }
        if(numPassedRounds == 1){  // pass 2
            ArrayList<ClassDeclaration> classDecs = getAllClassDeclarations(program);
            for(int i = 0; i < classDecs.size(); i++){

                checkClassNames(classDecs.get(i));
                Identifier parentName = classDecs.get(i).getParentName();
                SymbolTable.top.push(new SymbolTable(SymbolTable.top)); // for this class
                if(parentName != null)
                   addDecsendantsSymTable(classDecs.get(i).getName().getName(), findClass(parentName.getName(), this.program));

                checkInsideClass(classDecs.get(i), program);

                String className = classDecs.get(i).getName().getName();
                this.classSymTables.put(className, SymbolTable.top.getItems());
                SymbolTable.top.pop(); // class checking finished, pop it
            }
            numPassedRounds += 1;
            SymbolTable.top.pop();

        }
        if(numPassedRounds == 2 && hasErrors == false){
            ArrayList<ClassDeclaration> classDecs = getAllClassDeclarations(program);
            for(int i = 0; i < classDecs.size(); i++){
                String className = classDecs.get(i).getName().getName();

                if(classDecs.get(i).getParentName() != null)
                    checkForParentClassErrors(classDecs.get(i));
                    checkForCyclicClassErrors(classDecs.get(i));
                    checkInsideClassPhase3(classDecs.get(i));
            }
            
        }
        /*if(numPassedRounds == 3){ // final round : print ast if no errors found
            if(hasErrors == false)
                System.out.println(program.toString());
            ArrayList<ClassDeclaration> allClasses =  getAllClassDeclarations(program);
            for(int i = 0; i < allClasses.size(); i++){
                allClasses.get(i).accept(this);
            }
        }*/
    }

    public void checkInsideClassPhase3(ClassDeclaration cd){

        Identifier parentName = cd.getParentName();
        SymbolTable.top.push(new SymbolTable(SymbolTable.top));
        if(parentName != null)
           addDecsendantsSymTable(cd.getName().getName(), findClass(parentName.getName(), this.program));
        addAllItemsPhase3(cd);
        visit(cd);
        SymbolTable.top.pop();
    }

    public void addAllItemsPhase3(ClassDeclaration cd){

        ArrayList<VarDeclaration> vards = cd.getVarDeclarations();
        for(int i = 0 ; i < vards.size(); i++){
            VarDeclaration varDeclaration = vards.get(i);
            String name = varDeclaration.getIdentifier().getName();
            Type type=varDeclaration.getType();
            try{
                putGlobalVar(vards.get(i).getIdentifier().getName(), type);
            }catch(ItemAlreadyExistsException ee){
                System.out.println("OOOOOOOOOOPPPPSSSS");
            }
        }

        ArrayList<MethodDeclaration> mds = cd.getMethodDeclarations();
        for(int i = 0 ; i < mds.size(); i++){
            MethodDeclaration md = mds.get(i);
            String name = md.getName().getName();
            ArrayList <VarDeclaration> args = md.getArgs();
            try{
                put_method(name, args);
            }catch(ItemAlreadyExistsException ee){
                System.out.println("OOOOOOOOOOPPPPSSSS");
            }
        }
    }


    public void checkForParentClassErrors(ClassDeclaration cd){

        String parentName = cd.getParentName().getName();
            if(findParentSymbolTable(parentName) == null){
                int line = cd.getLine();
                System.out.println(String.format("Line:%d:Parent class does not exists", line));
                hasErrors = true;
            }
    }

    public HashMap<String, SymbolTableItem> findParentSymbolTable(String parentName){
        HashMap<String, SymbolTableItem> s = this.classSymTables.get(parentName);
        return s;
    }

    public void checkForCyclicClassErrors(ClassDeclaration cd){
        ArrayList <String> visitedClasses = new ArrayList<String>();
        visitedClasses.add(cd.getName().getName());
        ClassDeclaration cdInCycle = cd;
        while(true){
            if(cdInCycle.getParentName()== null)
                return;
            String parentName = cdInCycle.getParentName().getName();
            if(visitedClasses.contains(parentName)){
                hasErrors = true;
                int line = cd.getLine();
                System.out.println(String.format("Line:%d:Cyclic declaration of class %s", line, cd.getName().getName()));
                break;
            }
                else{
                    cdInCycle = findClass(parentName, this.program);
                    if(cdInCycle == null)
                        return;
                }
        }
    }

     public void addDecsendantsSymTable(String initialClassName, ClassDeclaration cd){
        ArrayList <String> visitedClasses = new ArrayList<String> ();
        visitedClasses.add(initialClassName);
        
        while(true){
            if(cd == null)
                break;
            String className = cd.getName().getName();
            if(visitedClasses.contains(className)){
                break;
            }
            else
                visitedClasses.add(className);

            ArrayList <VarDeclaration> varDecs = cd.getVarDeclarations();
            for(int i = 0; i < varDecs.size(); i++){
                
                String name = varDecs.get(i).getIdentifier().getName();
                Type type = varDecs.get(i).getType();
                index_variable += 1;
                try{
                    SymbolTable.top.put( new SymbolTableVariableItem(name, type, index_variable));
                    
                } catch(ItemAlreadyExistsException e){
                    String new_name = name + "Temporary_" + Integer.toString(index_variable);
                    try{
                        SymbolTable.top.put( new SymbolTableVariableItem(new_name, type, index_variable));
            
                        }catch(ItemAlreadyExistsException ee){
                            System.out.println("OOOOOOOOOPPPPPPSSSS");
                        }
                }
            }
            ArrayList <MethodDeclaration> methodDecs = cd.getMethodDeclarations();
            for(int i = 0; i < methodDecs.size(); i++){

                String methodName = methodDecs.get(i).getName().getName();
                ArrayList<Type> types = new ArrayList<Type>();
                ArrayList<VarDeclaration> args = methodDecs.get(i).getArgs();

                for(int j = 0; j < args.size(); j++){
                    types.add(args.get(j).getType());
                }

                try{
                    SymbolTable.top.put(new SymbolTableMethodItem(methodName,types));

                }catch(ItemAlreadyExistsException e){
                    String new_name = methodName + "Temporary_" + Integer.toString(number_of_repeated_method);
                    number_of_repeated_method+=1;
                    try{

                        SymbolTable.top.put(new SymbolTableMethodItem(new_name, types));
                    }
                    catch(ItemAlreadyExistsException ee){
                        System.out.println("OOOOOOOOOPSSSSS!");
                    }
                }  
            } 
            if(cd.getParentName() == null)
                break;
            else
                cd = findClass(cd.getParentName().getName(), this.program);
         }
     }

    @Override
    public void visit(ClassDeclaration classDeclaration) {
        
        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(classDeclaration.toString());

        classDeclaration.getName().accept(this);
        if(classDeclaration.getParentName() != null)
            classDeclaration.getParentName().accept(this);
            
        ArrayList<VarDeclaration> vards = new ArrayList<>(classDeclaration.getVarDeclarations());
        for (int i = 0; i < vards.size(); i++){
            vards.get(i).accept(this);
        }
        ArrayList<MethodDeclaration> mthds = new ArrayList<>(classDeclaration.getMethodDeclarations());
        for (int i = 0; i < mthds.size(); i++){
            mthds.get(i).accept(this);
        }
    }

    @Override
    public void visit(MethodDeclaration methodDeclaration) {

        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(methodDeclaration.toString());
   
        methodDeclaration.getName().accept(this); 

        ArrayList<VarDeclaration> args = methodDeclaration.getArgs();
        for(int i = 0 ; i < args.size(); i++)
            args.get(i).accept(this);

        // accept local variables
        ArrayList <VarDeclaration> localVars = new ArrayList<>(methodDeclaration.getLocalVars());
        for(int i = 0; i < localVars.size(); i++){
            localVars.get(i).accept(this);
        }
        // then accept body statements 
        ArrayList<Statement> bodyStms = new ArrayList<>(methodDeclaration.getBody());
        for (int i = 0; i < bodyStms.size(); i++){
            bodyStms.get(i).accept(this);
        }

        // finally accept return statement
        methodDeclaration.getReturnValue().accept(this);

             
            if(numPassedRounds == 2 && hasErrors == false){
                Type retValType =  methodDeclaration.getReturnValue().getType();
                Type retType = methodDeclaration.getReturnType();
                if(!isSubType(retValType, retType)){
                    hasErrors = true;
                    int line = retType.getLine();
                    System.out.println(String.format("Line:%d:return type must be %s",line, retType.toString()));
                }
            }
        }

    @Override
    public void visit(VarDeclaration varDeclaration) {

        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(varDeclaration.toString());

        varDeclaration.getIdentifier().accept(this);
    }

    @Override
    public void visit(ArrayCall arrayCall) {
        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(arrayCall.toString());

        arrayCall.getInstance().accept(this);
        arrayCall.getIndex().accept(this);
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {


        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(binaryExpression.toString());
        binaryExpression.getLeft().accept(this);
        binaryExpression.getRight().accept(this);
    }

    @Override
    public void visit(Identifier identifier) {
        int var_line = identifier.getLine();
        if(IsKeyWord(identifier.getName())){
            System.out.println(String.format("Line:%d: %s  is not valid,It is keyword", var_line, identifier.getName()));
            hasErrors = true;
        }
        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(identifier.toString());
    }

    @Override
    public void visit(Length length) {
        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(length.toString());
        length.getExpression().accept(this);
    }

    @Override
    public void visit(MethodCall methodCall) {
        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(methodCall.toString());

        methodCall.getInstance().accept(this);
        methodCall.getMethodName().accept(this);
        ArrayList<Expression> args = new ArrayList<> (methodCall.getArgs());
        for(int i = 0; i < args.size(); i++){
            args.get(i).accept(this);
        }
    }

    @Override
    public void visit(NewArray newArray) {

        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(newArray.toString());

        if(newArray.Size() <= 0 && numPassedRounds == 1){          
            newArray.setSize(0);
            int line = newArray.getLine();
            System.out.println(String.format("Line:%d:Array length should not be zero or negative", line));
            hasErrors = true;
        }
        newArray.getExpression().accept(this);
    }

    @Override
    public void visit(NewClass newClass) {
        if(hasErrors== false && numPassedRounds == 3){
            System.out.println(newClass.toString());
            System.out.println(newClass.getClassName());
        }
    }

    @Override
    public void visit(This instance) {
        //TODO: implement appropriate visit functionality
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(unaryExpression.toString());
        unaryExpression.getValue().accept(this);
        
    }

    @Override
    public void visit(BooleanValue value) {
        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(value.toString());
    }

    @Override
    public void visit(IntValue value) {
        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(value.toString());
    }

    @Override
    public void visit(StringValue value) {
        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(value.toString());
    }

    @Override
    public void visit(Assign assign) {

        if(assign.getrValue() == null){
            if(hasErrors== false && numPassedRounds == 3)
                System.out.println(assign.toString());  // not sure
            assign.getlValue().accept(this);
        }

        if(assign.getrValue() != null){
                if(hasErrors== false && numPassedRounds == 3)
                    System.out.println(assign.toString());
                assign.getlValue().accept(this);
                assign.getrValue().accept(this); 
            }
    }

    @Override
    public void visit(Block block) {
        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(block.toString());

        ArrayList<Statement> bb = new ArrayList<> (block.getBody());
        for(int i = 0; i < bb.size(); i++){
            bb.get(i).accept(this);
        }
    }

    @Override
    public void visit(Conditional conditional) {
        /* 
            if(numPassedRounds == 2){
                Expression cond = conditional.getExpression();
                if(cond.getType().toString() != 'boolean'){
                    hasErrors = true;
                    int line = conditional.getLine();
                    System.out.println(format("Line:%d:condition type must be boolean"));
                }
            }
        */
        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(conditional.toString());

        conditional.getExpression().accept(this);
        conditional.getConsequenceBody().accept(this);

        if(conditional.getAlternativeBody() != null )
            conditional.getAlternativeBody().accept(this);
    }

    @Override
    public void visit(MethodCallInMain methodCallInMain) {
        //TODO: implement appropriate visit functionality
    }

    @Override
    public void visit(While loop) {
             /*
                if(numPassedRounds == 2){
                    Expression cond = loop.getCondition();
                    if(!cond.getType().toString().equals('boolean')){
                        hasErrors = true;
                        int line = cond.getLine();
                        System.out.println(format("Line:%d:condition type must be boolean"));
                    }
                }
              */
             if(hasErrors== false && numPassedRounds == 3)
                System.out.println(loop.toString());

              loop.getCondition().accept(this);
              loop.getBody().accept(this);
    }

    @Override
    public void visit(Write write) {
        /* if(numPassedRound == 2){
            Type write_type = write.getArg().getType();
            if(!write_type.toString().equals('int') !write_type.toString().equals('int[]') !write_type.toString().equals('string')){
                hasErrors = true;
                int line = write.getLine();
                System.out.println(format("Line %d:Unsupported type for writeln", line));
            }
        }


        */
        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(write.toString());

        write.getArg().accept(this); 
    }
}
