grammar Smoola;
@header { 
    import ast.*;
    import ast.node.*;
    import ast.node.expression.*;
    import ast.node.expression.Value.*;
    import ast.node.statement.*;
    import ast.node.declaration.*;
    import ast.Type.*;
    import ast.Type.ArrayType.*;
    import ast.Type.UserDefinedType.*;
    import ast.Type.PrimitiveType.*;
    import java.util.ArrayList;
    import symbolTable.*;
}
@members{
    boolean noClasses = true;
    ArrayList<UserDefinedType> incompleteTypes = new ArrayList <> ();

    void print(String str){
        System.out.println(str);
    }
    void setIncompleteTypes(Program prog){

        for(int i = 0; i < incompleteTypes.size(); i++){
            UserDefinedType t = incompleteTypes.get(i);
            Identifier className = t.getName();
            ArrayList<ClassDeclaration> classes = new ArrayList<>(prog.getClasses());
            ClassDeclaration cd = findCorrespondingClassDec(prog,className);
            incompleteTypes.get(i).setClassDeclaration(cd);
        }
    }

    ClassDeclaration findCorrespondingClassDec(Program prog, Identifier id){
        ArrayList<ClassDeclaration> allClasses = new ArrayList<>(prog.getClasses());
        allClasses.add(prog.getMainClass());
        for(int i = 0; i < allClasses.size(); i++){
            if(allClasses.get(i).getName().getName().equals(id.getName())){
                return (allClasses.get(i));
            }
        }
        ClassDeclaration cd = new ClassDeclaration(id, null);
        return cd;
    }
    
}

    program returns [Program prog]:
        {Program prog = new Program();} mainClass[prog](classDeclaration[prog])* EOF 
        {
            if(noClasses == true)
                print("Line:0:No class exists in the program"); 
            else{
                setIncompleteTypes(prog);
	            VisitorImpl v = new VisitorImpl(prog);
                prog.accept(v);
            }

        }    
    ;

    mainClass [Program prog] returns [ClassDeclaration main]:
        cl = 'class'{noClasses = false;}{int line = $cl.getLine();} className = ID {
            Identifier id = new Identifier($className.text);
            $main = new ClassDeclaration(id, null);
            $main.setLine(line);
        }
        '{' ml = 'def' {int mline = $ml.getLine();} methodname = ID '(' ')' ':' 'int' 
        {
            Identifier mid = new Identifier($methodname.text);
            MethodDeclaration mainMethodDec = new MethodDeclaration(mid);
            mainMethodDec.setLine(mline);
            IntType t = new IntType(); mainMethodDec.setReturnType(t);

            }
        '{'  stms = statements{
            for (int i = 0; i < ($stms.multipleStatements).size(); i++) {
                mainMethodDec.addStatement($stms.multipleStatements.get(i));
		    }
         }
         tkn = 'return' retexp = expression{ 
            $retexp.expr.setLine($tkn.getLine()); 
            mainMethodDec.setReturnValue($retexp.expr);
            $main.addMethodDeclaration(mainMethodDec);
            $prog.setMainClass($main);
            }
            ';' '}' '}' 
         ;

    classDeclaration [Program prog] returns [ClassDeclaration classDec]:
        {noClasses = false;}cl = 'class' {int line = $cl.getLine();} classname = ID ('extends' parentname = ID)?
        {
            Identifier classid = new Identifier($classname.text);
            Identifier parentclassid;
            if($parentname.text != null)
                parentclassid = new Identifier($parentname.text);
            else    
                parentclassid = null;

            $classDec= new ClassDeclaration(classid, parentclassid);
            $classDec.setLine(line);
        }
        '{' (vardec = varDeclaration {$classDec.addVarDeclaration($vardec.varDec);})* 
            (methoddec = methodDeclaration {$classDec.addMethodDeclaration($methoddec.methodDec);})* '}'
            {   
            $prog.addClass($classDec);
            } 
    ;
    varDeclaration returns [VarDeclaration varDec]:
        vl = 'var' {int line = $vl.getLine(); } name = ID ':' t = type ';' 
        {
            Identifier id = new Identifier($name.text);
            $varDec = new VarDeclaration(id, $t.t);
            $varDec.setLine(line);
        }
    ;
    methodDeclaration returns[MethodDeclaration methodDec]:
        ml = 'def' {int line = $ml.getLine();} methodname = ID{
            Identifier id = new Identifier($methodname.text);
            $methodDec = new MethodDeclaration(id);
            $methodDec.setLine(line);
        }
        ('(' ')' | ( vl = '(' {int vline = $vl.getLine();} id = ID ':' tp = type{   
            Identifier vardecid = new Identifier($id.text);
            VarDeclaration arg = new VarDeclaration(vardecid, $tp.t);
            arg.setLine(vline);
            $methodDec.addArg(arg);}
        (vl2 = ',' { int vline2 = $vl2.getLine(); } id = ID ':' tp = type
        {
            Identifier vardecid2 = new Identifier($id.text);
            VarDeclaration arg2 = new VarDeclaration(vardecid2, $tp.t);
            arg2.setLine(vline2);
            $methodDec.addArg(arg2);
        })* ')')) co=':' 
        rettype = type '{' {
         int typeline = $co.getLine(); 
         $rettype.t.setLine(typeline);
         $methodDec.setReturnType($rettype.t); }
        (vardec = varDeclaration { $methodDec.addLocalVar($vardec.varDec); })*
        stms = statements {
            for(int i = 0; i < $stms.multipleStatements.size(); i++){
                $methodDec.addStatement($stms.multipleStatements.get(i));
            }
        }
        ret='return' retvalexpr = expression {
        int retline=$ret.getLine();
        $retvalexpr.expr.setLine(retline);  
        $methodDec.setReturnValue($retvalexpr.expr);}';' '}'
 
    ;
    statements returns [ArrayList<Statement> multipleStatements]:
        {$multipleStatements = new ArrayList<>();}
        (stm = statement{$multipleStatements.add($stm.stm);})*
    ;
    statement returns [Statement stm]:
        st = statementBlock {$stm = $st.block;} |
        st1 = statementCondition {$stm = $st1.conditional;} |
        st2 = statementLoop {$stm = $st2.wh;} |
        st3 = statementWrite {$stm = $st3.stm_write;} |
        st4 = statementAssignment {$stm = ($st4.assign);}
      
    ;
    statementBlock returns [Block block]:
        '{' {$block = new Block();}
        stms = statements {
            for(int i = 0 ; i < $stms.multipleStatements.size(); i ++ ){
                $block.addStatement($stms.multipleStatements.get(i));
            }
        } 
        '}' 
    ;
    statementCondition returns [Conditional conditional]:
        'if' tkn = '('exp = expression')' 'then' 
        cst = statement {
                            Conditional cond = new Conditional($exp.expr, $cst.stm);
                            cond.setLine($tkn.getLine());

                        }
        ('else' ast = statement {cond.setAlternativeBody($ast.stm);})?
        {$conditional = cond;}
      
    ;
    statementLoop returns [While wh]:
        tkn = 'while' '(' exp = expression ')' st = statement {
            $wh = new While($exp.expr, $st.stm);
            int line = $tkn.getLine();
            $wh.setLine(line);
        }
    ;
    statementWrite returns [Write stm_write]:
        tkn = 'writeln(' expr = expression ')' ';' {$stm_write = new Write($expr.expr); 
                                                    int line = $tkn.getLine();
                                                    $stm_write.setLine(line);} 
    ;
    statementAssignment returns [Assign assign]:
        expr = expression tkn = ';'
        {
            if($expr.lvalue != null && $expr.rvalue != null){

                $assign = new Assign($expr.lvalue, $expr.rvalue);
                $assign.setLine($tkn.getLine());
            }
            else if($expr.expr != null)
                $assign = new Assign($expr.expr, null);
                $assign.setLine($tkn.getLine());
        }
    ;

    expression returns [Expression lvalue, Expression rvalue, Expression expr]:
		retval = expressionAssignment {
            $rvalue = $retval.rvalue;
            $lvalue = $retval.lvalue;
            $expr = $retval.expr;
        }
	;

    expressionAssignment returns [Expression lvalue, Expression rvalue, Expression expr]:
		 expr_lvalue = expressionOr eqaulline = '=' expr_rvalue = expressionAssignment {
             int equal_line = $eqaulline.getLine();
             $lvalue = $expr_lvalue.expr; 
             $rvalue = $expr_rvalue.expr; 
             $lvalue.setLine(equal_line);
             $rvalue.setLine(equal_line);
             BinaryOperator bo = BinaryOperator.assign;
             BinaryExpression be = new BinaryExpression($expr_lvalue.expr, $expr_rvalue.expr, bo);
             $expr = be;
         }
         | exp = expressionOr {$expr = $exp.expr; $rvalue = null; $lvalue = null;}
	;

    expressionOr returns [Expression expr, BinaryOperator be]:
		lvalue = expressionAnd rvalue = expressionOrTemp{
            if($rvalue.expr != null){
                $expr = new BinaryExpression($lvalue.expr, $rvalue.expr, $rvalue.bo);
            }
            else{
                $expr = $lvalue.expr;
            }
        }
	;

    expressionOrTemp returns [Expression expr, BinaryOperator bo]:
		tkn = '||'{$bo = BinaryOperator.or;} lvalue = expressionAnd rvalue = expressionOrTemp
        {   
            if($rvalue.expr != null){
                $expr = new BinaryExpression($lvalue.expr, $rvalue.expr, $rvalue.bo);
                $expr.setLine($tkn.getLine());
            }
            else{
                $expr = $lvalue.expr;
                //$expr.setLine($tkn.getLine());
            }
        }
	    |
	;

    expressionAnd returns [Expression expr]:
		lvalue = expressionEq rvalue = expressionAndTemp
        {
            if($rvalue.expr != null){
                $expr = new BinaryExpression($lvalue.expr, $rvalue.expr, $rvalue.bo);
        }
            else{
                $expr = $lvalue.expr;
            }
        }
	;

    expressionAndTemp returns [Expression expr, BinaryOperator bo]:
		tkn = '&&'{$bo = BinaryOperator.and;} expr1 = expressionEq expr2 = expressionAndTemp
        {   
            if($expr2.expr != null){
                $expr = new BinaryExpression($expr1.expr, $expr2.expr, $expr2.bo);
                $expr.setLine($tkn.getLine());
            }
            else{
                $expr = $expr1.expr;
                //$expr.setLine($tkn.getLine());
            }
        }
	    |
	;

    expressionEq returns [Expression expr]:
		lvalue = expressionCmp rvalue = expressionEqTemp {
            if($rvalue.expr != null){
                $expr = new BinaryExpression($lvalue.expr, $rvalue.expr, $rvalue.bo);
        }
            else{
                $expr = $lvalue.expr;
            }
        }
	;

    expressionEqTemp returns [Expression expr, BinaryOperator bo]:
		(tkn = '=='{$bo = BinaryOperator.eq;} | tkn = '<>' {$bo = BinaryOperator.neq;}) 
        expr1 = expressionCmp expr2 = expressionEqTemp 
        {   
            if($expr2.expr != null){
                $expr = new BinaryExpression($expr1.expr, $expr2.expr, $expr2.bo);
                $expr.setLine($tkn.getLine());
                }
            else{
                $expr = $expr1.expr;
                //$expr.setLine($tkn.getLine());
            }
        }
	    |
	;

    expressionCmp returns [Expression expr]:
		lvalue = expressionAdd rvalue = expressionCmpTemp{
            if($rvalue.expr != null){
                $expr = new BinaryExpression($lvalue.expr, $rvalue.expr, $rvalue.bo);
        }
            else{
                $expr = $lvalue.expr;
            }
        }
	;

    expressionCmpTemp returns [Expression expr, BinaryOperator bo]:
		(tkn = '<' {$bo = BinaryOperator.lt;} | tkn = '>' {$bo = BinaryOperator.gt;}) 
        expr1 = expressionAdd expr2 = expressionCmpTemp 
        {
            if($expr2.expr != null){
                $expr = new BinaryExpression($expr1.expr, $expr2.expr, $expr2.bo);
                $expr.setLine($tkn.getLine());
            }
            else{
                $expr = $expr1.expr;
                //$expr.setLine($tkn.getLine());
            }
        }
	    |
	;

    expressionAdd returns [Expression expr]:
		lvalue = expressionMult rvalue = expressionAddTemp{
            if($rvalue.expr != null){
                $expr = new BinaryExpression($lvalue.expr, $rvalue.expr, $rvalue.bo);
        }
            else{
                $expr = $lvalue.expr;
            }
        }
	;

    expressionAddTemp returns [Expression expr, BinaryOperator bo]:
		(tkn = '+' { $bo = BinaryOperator.add; } | tkn = '-' { $bo = BinaryOperator.sub; } ) 
        expr1 = expressionMult expr2 =  expressionAddTemp
        {   
            if($expr2.expr != null){
                $expr = new BinaryExpression($expr1.expr, $expr2.expr, $expr2.bo);
                $expr.setLine($tkn.getLine());
            }
            else{
                $expr = $expr1.expr;
                //$expr.setLine($tkn.getLine());
            }

            }
	    |
	;

        expressionMult returns [Expression expr]:
		lvalue = expressionUnary rvalue = expressionMultTemp{
            if($rvalue.expr != null){
                $expr = new BinaryExpression($lvalue.expr, $rvalue.expr, $rvalue.bo);
            }
            else{ $expr = $lvalue.expr;}
        }
	;

    expressionMultTemp returns [Expression expr, BinaryOperator bo]:
		(tkn = '*' {$bo = BinaryOperator.mult;} | tkn2 = '/' {$bo = BinaryOperator.div;} ) 
        expr1 = expressionUnary expr2 = expressionMultTemp
        {   
            //int mult_line=$tkn.getLine();
            //$expr1.expr.setLine(mult_line);
            //$expr2.expr.setLine(mult_line);
            if($expr2.expr != null){
                $expr = new BinaryExpression($expr1.expr, $expr2.expr, $expr2.bo);
                $expr2.expr.setLine($tkn.getLine());
            }
            else
                $expr = $expr1.expr;
            }
	    |
	;

    expressionUnary returns [Expression expr]:
		 (tkn = '!'  exp = expressionUnary) {UnaryOperator uo = UnaryOperator.not; 
                                            $expr = new UnaryExpression(uo, $exp.expr);
                                            $expr.setLine($tkn.getLine());}
         | (tkn2 = '-'  exp1 = expressionUnary) {UnaryOperator uo1 = UnaryOperator.minus; 
                                                $expr = new UnaryExpression(uo1, $exp1.expr);
                                                $expr.setLine($tkn2.getLine());} 
         |	exp2 = expressionMem {$expr = $exp2.expr;}
	;

    expressionMem returns [Expression expr]:
		instance = expressionMethods index = expressionMemTemp
        {if($index.expr != null){
             $expr = new ArrayCall($instance.expr, $index.expr);
            }
        else{
            $expr = $instance.expr;
        }
        }
	;

    expressionMemTemp returns [Expression expr]:
		ind ='[' index = expression ']' {
        $expr = $index.expr;
        $index.expr.setLine(($ind.getLine()));
        }
	    |
	;
	expressionMethods returns [Expression expr]: // not sure
	    instance = expressionOther methodcall = expressionMethodsTemp[$instance.expr] 
        {   
            if($methodcall.methodcall == null){
                $expr = $instance.expr;
            }
            else if($instance.expr == null || $methodcall.methodcall != null){
                $expr = $methodcall.methodcall;
            }
            
        }
    
	;
	expressionMethodsTemp [Expression instance] returns [Expression methodcall]:
     point='.' methodname = ID '(' ')'  {   
                
                Identifier id = new Identifier($methodname.text);
                MethodCall new_inst = new MethodCall($instance, id);
                int p_line=$point.getLine();
                new_inst.setLine(p_line);
            }
        temp = expressionMethodsTemp[new_inst] {$methodcall = $temp.methodcall; $methodcall.setLine(p_line);}

     | point='.' methodname = ID  '(' {
                Identifier id = new Identifier($methodname.text);
                MethodCall tempm = new MethodCall($instance, id);
                int p_line2=$point.getLine();
                tempm.setLine(p_line2);
            }
            (arg = expression {tempm.addArg($arg.expr);} 
            (co=',' arg = expression {tempm.addArg($arg.expr);
            int p_line3=$co.getLine();
            $arg.expr.setLine(p_line3);})*) ')' 
            temp = expressionMethodsTemp [tempm] {$methodcall = $temp.methodcall; $methodcall.setLine(p_line2);}

     | point='.' 'length' {Length new_inst = new Length($instance); } temp = expressionMethodsTemp[new_inst] {$methodcall = $temp.methodcall;
        int p_line=$point.getLine();
        new_inst.setLine(p_line);}
     | {$methodcall = $instance;}
       
        
	;

    expressionOther returns [Expression expr, Expression lvalue, Expression rvalue]:

		num = CONST_NUM{   
                IntType t = new IntType();
                Expression temp_expr = new IntValue($num.int, t);
                $expr = temp_expr;
            }
        |	str = CONST_STR {
            StringType st = new StringType();
            $expr = new StringValue($str.text, st);
        }
            
        | ln = 'new ' 'int' '[' num = CONST_NUM ']'
            {   
                IntType t = new IntType();
                IntValue val = new IntValue($num.int, t);
                NewArray newarr = new NewArray();
                newarr.setExpression(val);
                newarr.setSize($num.int);
                newarr.setLine($ln.getLine());
                $expr = newarr;
            }
        |   'new ' name = ID '(' ')' {
            Identifier id = new Identifier($name.text);
            $expr = new NewClass(id);
            }
        |   'this' { $expr = new This();}
        |   constval = 'true' {
                                BooleanType bt = new BooleanType(); 
                                $expr = new BooleanValue(true, bt); 
                             }
        |   constval = 'false'{ BooleanType bt = new BooleanType();
                                $expr = new BooleanValue(false, bt);
                                }
        |	id = ID {$expr = new Identifier($id.text); $expr.setLine($id.getLine());}
        |   id = ID '[' exp = expression ']' 
            {     
                Identifier identifier = new Identifier($id.text);
                $expr = new ArrayCall(identifier, $exp.expr);
            }
        |	'(' thisexpr = expression ')' {
                $lvalue = $thisexpr.lvalue;
                $rvalue = $thisexpr.rvalue;
                $expr = $thisexpr.expr;
            }
	;

	type returns [Type t]:
	    'int' {$t = new IntType();}|
	    'boolean' {$t = new BooleanType();}|
	    'string' {$t = new StringType();}|
	    'int' '[' ']' {$t = new ArrayType();}|
	    id = ID {
                 Identifier cid = new Identifier($id.text); 
                 UserDefinedType ct = new UserDefinedType();
                 ct.setName(cid);
                 $t = ct;
                 incompleteTypes.add(ct);
                 }
	;

    CONST_NUM:
		[0-9]+ 
	;

    CONST_STR:
		('"') (~('\r' | '\n' | '"')*) ('"') 
	;
    NL:
		'\r'? '\n' -> skip
	;

    ID:
		[a-zA-Z_][a-zA-Z0-9_]*
	;

    COMMENT:
		'#'(~[\r\n])* -> skip
	;

    WS:
    	[ \t] -> skip
    ;