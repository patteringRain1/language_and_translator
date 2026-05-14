package compiler.AST.expression;

import compiler.AST.basic.ASTnode;
import compiler.Codegenerator.Codegenerator;
import compiler.Semantic.SymbolTable;
import java.util.ArrayList;
import java.util.List;

// for function call expression
public class functioncallnode extends ASTnode {
    // function name
    private String functionname;
    // arguments list
    private List<ASTnode> arguments = new ArrayList<>();

    public functioncallnode(String functionname) {
        this.functionname = functionname;
    }

    // add argument to an function
    public void addargument(ASTnode arg){
        this.arguments.add(arg);
    }

    // print the structure of function call expression
    @Override
    public void print(int indent) {
        System.out.println(getIndent(indent) + "function call expression");
        int k = arguments.size();
        for(int i = 0; i < k; i++){
            ASTnode arg = arguments.get(i);
            if(arg != null){
                arg.print(indent + 1);
            }
        }
    }

    @Override
    public String checkSemantics(SymbolTable table) {
        switch (functionname) {
            case "read_INT":
                return "int";
            case "read_FLOAT":
                return "float";
            case "read_STRING":
                return "string";
            case "length":
                return "int";
            case "floor":
                return "int";
            case "ceil":
                return "int";
            case "str":
                return "string";
            case "not":
                return "bool";
            case "print":
            case "println":
            case "print_INT":
            case "print_FLOAT": {
                for (ASTnode arg : arguments) {
                    if (arg != null) arg.checkSemantics(table);
                }
                return "void";
            }
            default: {
                List<String> expectedTypes = table.getFunctionParamTypes(functionname);
                int k = arguments.size();

                if (expectedTypes != null) {
                    if (k != expectedTypes.size()) {
                        SymbolTable.crash("ArgumentError",
                                "function " + functionname + " expects " + expectedTypes.size()
                                        + " arguments but received " + k);
                    }
                    for (int i = 0; i < k; i++) {
                        ASTnode arg = arguments.get(i);
                        if (arg != null) {
                            String actualType = arg.checkSemantics(table);
                            String expectedType = expectedTypes.get(i);
                            if (!SymbolTable.typesAreCompatible(actualType, expectedType)) {
                                SymbolTable.crash("ArgumentError",
                                        "argument " + (i+1) + " of function " + functionname
                                                + " expects " + expectedType
                                                + " but received " + actualType);
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < k; i++) {
                        ASTnode arg = arguments.get(i);
                        if (arg != null) arg.checkSemantics(table);
                    }
                }

                String retType = table.getFunctionReturnType(functionname);
                return retType != null ? retType.toLowerCase() : functionname;
            }
        }
    }

    @Override
    public void generateCode(Codegenerator cg) {
        int k = arguments.size();
        switch (functionname) {
            // System.out.println()
            case "println": {
                cg.emitGetStatic("java/lang/System", "out", "Ljava/io/PrintStream;");
                if (k > 0) {
                    arguments.get(0).generateCode(cg);
                    String argType = cg.getExprType(arguments.get(0));
                    cg.emitInvokeVirtual("java/io/PrintStream", "println",
                            "(" + cg.typeToDescriptor(argType) + ")V");
                } else {
                    cg.emitInvokeVirtual("java/io/PrintStream", "println", "()V");
                }
                break;
            }
            // System.out.print()
            case "print": {
                cg.emitGetStatic("java/lang/System", "out", "Ljava/io/PrintStream;");
                if (k > 0) {
                    arguments.get(0).generateCode(cg);
                    String argType = cg.getExprType(arguments.get(0));
                    cg.emitInvokeVirtual("java/io/PrintStream", "print",
                            "(" + cg.typeToDescriptor(argType) + ")V");
                }
                break;
            }
            //System.out.println(int)
            case "print_INT": {
                cg.emitGetStatic("java/lang/System", "out", "Ljava/io/PrintStream;");
                arguments.get(0).generateCode(cg);
                cg.emitInvokeVirtual("java/io/PrintStream", "println", "(I)V");
                break;
            }
            //System.out.println(float)
            case "print_FLOAT": {
                cg.emitGetStatic("java/lang/System", "out", "Ljava/io/PrintStream;");
                arguments.get(0).generateCode(cg);
                cg.emitInvokeVirtual("java/io/PrintStream", "println", "(F)V");
                break;
            }
            // Scanner.nextInt()
            case "read_INT": {
                cg.emitReadInt();
                break;
            }
            // Scanner.nextFloat()
            case "read_FLOAT": {
                cg.emitReadFloat();
                break;
            }
            // Scanner.nextString()
            case "read_STRING": {
                cg.emitReadString();
                break;
            }
            case "length": {
                arguments.get(0).generateCode(cg);
                String argType = cg.getExprType(arguments.get(0));
                if ("string".equalsIgnoreCase(argType)) {
                    cg.emitInvokeVirtual("java/lang/String", "length", "()I");
                } else {
                    cg.emitInstruction(org.objectweb.asm.Opcodes.ARRAYLENGTH);
                }
                break;
            }
            case "floor": {
                arguments.get(0).generateCode(cg);
                cg.emitInvokeStatic("java/lang/Math", "floor", "(D)D");
                cg.emitInstruction(org.objectweb.asm.Opcodes.D2I);
                break;
            }
            case "ceil": {
                arguments.get(0).generateCode(cg);
                cg.emitInvokeStatic("java/lang/Math", "ceil", "(D)D");
                cg.emitInstruction(org.objectweb.asm.Opcodes.D2I);
                break;
            }
            case "not": {
                arguments.get(0).generateCode(cg);
                cg.emitInstruction(org.objectweb.asm.Opcodes.ICONST_1);
                cg.emitInstruction(org.objectweb.asm.Opcodes.IXOR);
                break;
            }
            case "str": {
                arguments.get(0).generateCode(cg);
                cg.emitInvokeStatic("java/lang/String", "valueOf", "(I)Ljava/lang/String;");
                break;
            }
            default: {
                if (cg.isCollectionConstructor(functionname)) {
                    cg.emitNew(functionname);
                    cg.emitInstruction(org.objectweb.asm.Opcodes.DUP);
                    for (int i = 0; i < k; i++) {
                        arguments.get(i).generateCode(cg);
                    }
                    String constructorDesc = cg.getCollectionConstructorDesc(functionname);
                    cg.emitInvokeSpecial(functionname, "<init>", constructorDesc);
                } else {
                    // static function
                    for (int i = 0; i < k; i++) {
                        arguments.get(i).generateCode(cg);
                    }
                    String methodDesc = cg.getFunctionDescriptor(functionname);
                    cg.emitInvokeStatic(cg.getClassName(), functionname, methodDesc);
                }
                break;
            }
        }
    }
}
