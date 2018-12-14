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
import ast.Type.PrimitiveType.*;
import ast.Type.NoType.*;
import ast.Type.ArrayType.*;
import java.util.*;

public class VisitorImpl implements Visitor {

    private int numPassedRounds = 0;
    private Boolean hasErrors = false;
    private Program program;
    public int number_of_repeated_method = 0;
    public int index_variable = 0;
    public int index_class = 0;
    public int index_keyword = 0;
    private ClassDeclaration currentScope ; /* current class for phase 3 -> for setting this type */
    private boolean isMainClass = false; /* for checking main method name */

    HashMap <String, HashMap<String, SymbolTableItem>> classSymTables;

    public Boolean IsKeyWord(String word)
    {
      String KeyWords[] = {"return", "class","extends","new", "int", "char", "string", "boolean", "if", "then", "else","while",
       "var", "def", "writeln", "true", "false", "this"};
       for(int i=0; i<KeyWords.length;i++){
           if(word== KeyWords[i]){
            return true; 
           }
       }   
      return false;
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
        Type type = varDeclaration.getType();

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

    public void put_method(String name, ArrayList<VarDeclaration> argTypes, Type retType)throws ItemAlreadyExistsException{
        ArrayList<Type>types = new ArrayList<Type>();
        for(int i=0;i<argTypes.size(); i++){
            types.add(argTypes.get(i).getType());
        }
        SymbolTable.top.put(new SymbolTableMethodItem(name, types, retType));
    }

    public void checkMethodName(MethodDeclaration methodDeclaration, int parentLine){
        String methodname = methodDeclaration.getName().getName();
        ArrayList<VarDeclaration> argTypes = new ArrayList<>(methodDeclaration.getArgs());
        argTypes= methodDeclaration.getArgs();
        Type retType = methodDeclaration.getReturnType();
        try{
            put_method(methodname, argTypes, retType);
        }catch(ItemAlreadyExistsException e){
 
            hasErrors = true;
            int lineToShow = Math.max(methodDeclaration.getLine(), parentLine);
            System.out.println(String.format("Line:%d:Redefinition of method %s", lineToShow, methodname));

            String new_name = methodname + "Temporary_" + Integer.toString(number_of_repeated_method);
            number_of_repeated_method+=1;
            try{
            put_method(new_name, argTypes, retType);
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

    public int getBinaryOperatorType(BinaryOperator bo){
        switch(bo){
            case  add: return 1; 
            case sub: return 1;
            case mult: return 1;
            case div: return 1;
            case and: return 2;
            case or:  return 2;
            case eq:  return 3; 
            case neq: return 3;
            case lt: return 1;
            case gt: return 1;
            case assign: return 4;
            default : return -1;
        }
    }

    public int getUnaryOperatorType(UnaryOperator uo){
        switch(uo){
            case minus : return 1;
            case not :  return 2;
            default : return -1;
        }
    }

    public boolean isUserDefinedType(Type t){
        String typeName = t.toString();
        if(typeName.equals("int") || typeName.equals("string") || typeName.equals("int[]") || typeName.equals("bool"))
            return false;
        else
            return true;
    }

    public boolean isInt(Type t){
        if(t.toString().equals("int") || t.toString().equals("noType"))
            return true;
        return false;
    }

    public boolean isBool(Type t){
        if(t.toString().equals("bool") || t.toString().equals("noType"))
            return true;
        return false;

    }

    public boolean isSubType(Type t1, Type t2){
        if(t1.toString().equals("noType"))
            return true;
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
                this.currentScope = classDecs.get(i);
                if(i == 0)
                    this.isMainClass = true;
                else
                    this.isMainClass = false;

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
            Type temp_type = varDeclaration.getType();
            String className = temp_type.toString();

            if(((isUserDefinedType(temp_type)))){
                if(!this.classSymTables.containsKey(temp_type.toString())){ // class which does no exists

                    Type type = new NoType();
                    varDeclaration.setType(type);
                }
            }

            Type type = varDeclaration.getType();

            if(isUserDefinedType(varDeclaration.getType())){
                String typeName = varDeclaration.getType().toString();
                if(!this.classSymTables.containsKey(typeName)){
                    hasErrors = true;
                    int line = varDeclaration.getLine(); 
                    System.out.println(String.format("Line:%d:class %s is not declared", line, className));
                    type = new NoType();
                }
            }

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
            Type retType = md.getReturnType();
            try{
                put_method(name, args, retType);
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
                Type retType = methodDecs.get(i).getReturnType();

                for(int j = 0; j < args.size(); j++){
                    types.add(args.get(j).getType());
                }

                try{
                    SymbolTable.top.put(new SymbolTableMethodItem(methodName, types, retType));

                }catch(ItemAlreadyExistsException e){
                    String new_name = methodName + "Temporary_" + Integer.toString(number_of_repeated_method);
                    number_of_repeated_method+=1;
                    try{

                        SymbolTable.top.put(new SymbolTableMethodItem(new_name, types, retType));
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

        if(hasErrors== false && numPassedRounds == 3){
            classDeclaration.getName().accept(this);
            if(classDeclaration.getParentName() != null)
                classDeclaration.getParentName().accept(this);
        }
    
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

        if(numPassedRounds == 2 && this.isMainClass){
            if(!methodDeclaration.getName().getName().equals("main")){
                int line = methodDeclaration.getLine();
                System.out.println(String.format("Line:%d: Invalid name for main class function", line));
                hasErrors = true;
            }
        }
        if(hasErrors == false && numPassedRounds == 3)
            methodDeclaration.getName().accept(this); 

        ArrayList<VarDeclaration> args = methodDeclaration.getArgs();
        for(int i = 0 ; i < args.size(); i++){
            if(numPassedRounds == 2){
                String name = args.get(i).getIdentifier().getName();
                Type type = args.get(i).getType();
                try{
                    putGlobalVar(name, type);
                }
                catch(ItemAlreadyExistsException e){

                    String new_name = name + "Temporary_" + Integer.toString(index_variable);
                    try{

                        putGlobalVar(new_name, type);
                    }catch(ItemAlreadyExistsException ee){
                        System.out.println("oooooopppppsssss");
                    }
                }
            }
            args.get(i).accept(this);
        }
        // accept local variables
        ArrayList <VarDeclaration> localVars = new ArrayList<>(methodDeclaration.getLocalVars());
        for(int i = 0; i < localVars.size(); i++){
            if(numPassedRounds == 2){
                String name = localVars.get(i).getIdentifier().getName();
                Type type = localVars.get(i).getType();
                try{
                    putGlobalVar(name, type);
                }
                catch(ItemAlreadyExistsException e){

                    String new_name = name + "Temporary_" + Integer.toString(index_variable);
                    try{

                        putGlobalVar(new_name, type);
                    }catch(ItemAlreadyExistsException ee){
                        System.out.println("oooooopppppsssss");
                    }
                }
            }
            localVars.get(i).accept(this);
        }
        // then accept body statements 
        ArrayList<Statement> bodyStms = new ArrayList<>(methodDeclaration.getBody());
        for (int i = 0; i < bodyStms.size(); i++){
            bodyStms.get(i).accept(this);
        }
        // finally accept return statement
        methodDeclaration.getReturnValue().accept(this);

        if(numPassedRounds == 2){
            Type retValType =  methodDeclaration.getReturnValue().getType();
            Expression retval = methodDeclaration.getReturnValue();
            Type retType = methodDeclaration.getReturnType();
            Expression ret_value = methodDeclaration.getReturnValue();
            if(!isSubType(retValType, retType)){
                hasErrors = true;
                int line = retval.getLine();
                System.out.println(String.format("Line:%d:return type must be %s", line, retType.toString()));
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

        if(numPassedRounds == 2){
            arrayCall.setType(new IntType());
        }
        arrayCall.getInstance().accept(this);
        arrayCall.getIndex().accept(this);
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {

        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(binaryExpression.toString());
  
        binaryExpression.getLeft().accept(this);
        binaryExpression.getRight().accept(this);

        if(numPassedRounds == 2){
            BinaryOperator bo = binaryExpression.getBinaryOperator();
            if(getBinaryOperatorType(bo) == 1){
            if(!(isInt(binaryExpression.getLeft().getType())  && 
              isInt(binaryExpression.getRight().getType()))){
                hasErrors = true;
                int line = binaryExpression.getLeft().getLine();
                System.out.println(String.format("Line:%d:unsupported operand type for %s",line,bo.name()));
                binaryExpression.setType(new NoType());
               }
               else{
                   binaryExpression.setType(new IntType());
               }
            } 
            else if (getBinaryOperatorType(bo) == 2){
                if(!(isBool(binaryExpression.getLeft().getType())  && 
                    isBool(binaryExpression.getRight().getType()))){
                   hasErrors = true;
                   int line = binaryExpression.getLeft().getLine();
                   System.out.println(String.format("Line:%d:unsupported operand type for %s",line,bo.name()));
                    binaryExpression.setType(new NoType());
               }
               else{
                binaryExpression.setType(new BooleanType());
                }
            }

            else if(getBinaryOperatorType(bo) == 3){
                Type t1 = binaryExpression.getLeft().getType();
                Type t2 = binaryExpression.getRight().getType();
                if(!(isSubType(t1, t2) || isSubType(t2, t1))){
                    hasErrors = true;
                    int line = binaryExpression.getLine();
                    System.out.println(String.format("Line:%d:unsupported operand type for %s",line,bo.name()));
                    binaryExpression.setType(new NoType());
                }
                else{
                    binaryExpression.setType(new BooleanType());
                }
            }
            else if(getBinaryOperatorType(bo) == 4){
                System.out.println("here");
                if(!(binaryExpression.getLeft() instanceof Identifier)){
                    hasErrors = true;
                    int line = binaryExpression.getLine();
                    System.out.println(String.format("Line:%d:left side of assignment must be a valid lvaue",line));
                    binaryExpression.setType(new NoType());
                }
                binaryExpression.setType(new NoType()); // not sure ..
            }      
        }
    } 

    @Override
    public void visit(Identifier identifier) {
        int var_line = identifier.getLine();
        if(IsKeyWord(identifier.getName())){
            System.out.println(String.format("Line:%d:%s is not valid a valid name", var_line, identifier.getName()));
            hasErrors = true;
        }
        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(identifier.toString());
        if(numPassedRounds == 2){
            try{
                SymbolTableVariableItemBase item = (SymbolTableVariableItemBase)SymbolTable.top.get(identifier.getName());
                identifier.setType(item.getType());
            }
            catch(ItemNotFoundException e){
                int line = identifier.getLine();
                hasErrors = true;
                System.out.println(String.format("Line:%d:variable %s is not declared", line, identifier.getName()));
                identifier.setType(new NoType());
            }
        }
    }

    @Override
    public void visit(Length length) {
        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(length.toString());

        length.getExpression().accept(this);

        if(numPassedRounds == 2){
        if(!length.getExpression().getType().toString().equals("int[]")){
            int line = length.getLine();
            System.out.println(String.format("Line:%d:Unsupported type for length", line));
            hasErrors = true;
            length.setType(new NoType());
        }
        else{
            length.setType(new IntType());
        }
    }

    }

    @Override
    public void visit(MethodCall methodCall) {
        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(methodCall.toString());

        methodCall.getInstance().accept(this);
        if(numPassedRounds == 2){
            Type t = methodCall.getInstance().getType();
            String typeName = t.toString();
            if(!this.classSymTables.containsKey(typeName)){
                int line = methodCall.getLine();
                System.out.println(String.format("Line:%d:class %s is not declared", line, typeName));
                hasErrors = true;
            }
        }
        if(numPassedRounds == 3)
            methodCall.getMethodName().accept(this);

        if(numPassedRounds == 2){

            String methodname = methodCall.getMethodName().getName();
            String className = methodCall.getInstance().getType().toString();
            HashMap<String, SymbolTableItem> classSymTable;
            classSymTable = this.classSymTables.get(className);
            if(!classSymTable.containsKey(methodname)){
                int line = methodCall.getLine();
                System.out.println(String.format("Line:%d:there is no method named %s in class %s", line, methodname, className));
                hasErrors = true;
                methodCall.setType(new NoType());
            }
            else{
                SymbolTableMethodItem md = (SymbolTableMethodItem)(this.classSymTables.get(className).get(methodname));
                ArrayList<Type> methodTypes = md.getTypes();
                ArrayList<Expression> args = new ArrayList<> (methodCall.getArgs());
                methodCall.setType(md.getReturnType());
  

                if(methodTypes.size() != args.size()){
                    int line = methodCall.getLine();
                    System.out.println(String.format("Line:%d:incorrect number of arguments for method %s", line, methodname));
                    hasErrors = true;
                }
                for(int i = 0; i < args.size(); i++){
                    if(i >= args.size() || i >= methodTypes.size())
                        break;

                    args.get(i).accept(this);
                    if(!isSubType(args.get(i).getType(), methodTypes.get(i))){
                        hasErrors = true;
                        int line = methodCall.getLine();
                        System.out.println(String.format("Line:%d:incompatible arguement types for aguemnet number %d", line, i));
                    }
                }
            }
        }

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
        if(numPassedRounds == 2){
            newArray.setType(new ArrayType());
        }
    }

    @Override
    public void visit(NewClass newClass) {
        if(hasErrors== false && numPassedRounds == 3){
            System.out.println(newClass.toString());
            System.out.println(newClass.getClassName());
        }
        if(numPassedRounds == 2){
            Identifier nameid = newClass.getClassName();
            String name = nameid.getName();
            if(!this.classSymTables.containsKey(name)){
                hasErrors = true;
                int line = newClass.getLine();
                System.out.println(String.format("Line:%d:class %s is not declared", line, name));
                newClass.setType(new NoType());
            }
            else{
                UserDefinedType u = new UserDefinedType();
                ClassDeclaration cd = findClass(name, this.program);
                u.setClassDeclaration(cd);
                u.setName(nameid);
                newClass.setType(u);
            }
        }
    }

    @Override
    public void visit(This instance) {
        if(numPassedRounds == 2){
            UserDefinedType u = new UserDefinedType();
            u.setClassDeclaration(this.currentScope);
            Identifier name = this.currentScope.getName();
            u.setName(name);
            instance.setType(u);
        }
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(unaryExpression.toString());

        unaryExpression.getValue().accept(this);
        
        if(numPassedRounds == 2){
            UnaryOperator uo = unaryExpression.getUnaryOperator();
            if(getUnaryOperatorType(uo) == 1){
                if(!unaryExpression.getType().toString().equals("int")){
                    hasErrors = true;
                    int line = unaryExpression.getLine();
                    System.out.println(String.format("Line:%d: Usupported operand type for %s", line, uo.toString()));
                    unaryExpression.setType(new NoType());
                }
                else{
                    unaryExpression.setType(new IntType());
                }
            }
            else if(getUnaryOperatorType(uo) == 2){
                if(!unaryExpression.getType().toString().equals("boolean")){
                    hasErrors = true;
                    int line = unaryExpression.getLine();
                    System.out.println(String.format("Line:%d: Usupported operand type for %s", line, uo.toString()));
                    unaryExpression.setType(new NoType());
                }
                else{
                    unaryExpression.setType(new BooleanType());
                }
            }

        }
        
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
            if(numPassedRounds == 2){
            if(assign.getrValue() != null){
                if(!(assign.getlValue() instanceof Identifier)){
                    hasErrors = true;
                    int line = assign.getlValue().getLine();
                    System.out.println(String.format("Line:%d:left side of assignment must be a valid lvaue",line));
                }
                Type t1 = assign.getrValue().getType();
                Type t2 = assign.getlValue().getType();
                if(!isSubType(t1, t2)){
                    hasErrors = true;
                    int line = assign.getlValue().getLine();
                    System.out.println(String.format("Line:%d:incompatible types for =", line));
                }

            }
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
        if(hasErrors== false && numPassedRounds == 3)
            System.out.println(conditional.toString());

        conditional.getExpression().accept(this);
          
        if(numPassedRounds == 2){
            Expression cond = conditional.getExpression();
            if(!isBool(cond.getType())){
                hasErrors = true;
                int line = conditional.getLine();
                System.out.println(String.format("Line:%d:condition type must be boolean", line));
            }
        }

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
             if(hasErrors== false && numPassedRounds == 3)
                System.out.println(loop.toString());

              loop.getCondition().accept(this);
              if(numPassedRounds == 2){
                Expression cond = loop.getCondition();
                if(!isBool(cond.getType())){
                    hasErrors = true;
                    int line = cond.getLine();
                    System.out.println(String.format("Line:%d:condition type must be boolean", line));
                }
            }
              loop.getBody().accept(this);
    }

    @Override
    public void visit(Write write) {
          if(hasErrors== false && numPassedRounds == 3)
            System.out.println(write.toString());

        write.getArg().accept(this); 

        if(numPassedRounds == 2){
            Type write_type = write.getArg().getType();
            String typeName = write_type.toString();
            if(!(typeName.equals("int") || typeName.equals("int[]") || typeName.equals("string")
                || typeName.equals("noType"))){
                hasErrors = true;
                int line = write.getLine();
                System.out.println(String.format("Line %d:Unsupported type for writeln", line));
            }
        }
    }
}
