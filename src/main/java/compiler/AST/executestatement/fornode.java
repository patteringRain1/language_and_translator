package compiler.AST.executestatement;

import compiler.AST.basic.ASTnode;
import compiler.Codegenerator.Codegenerator;
import compiler.Semantic.SymbolTable;

// // for "for" loop statement
public class fornode extends ASTnode {
    // initialization of loop
    private ASTnode initialization;
    // condition of loop
    private ASTnode condition;
    // step part of loop
    private ASTnode step_part;
    // block of loop
    private ASTnode body;

    public fornode(ASTnode initialization, ASTnode condition, ASTnode step_part, ASTnode body) {
        this.initialization = initialization;
        this.condition = condition;
        this.step_part = step_part;
        this.body = body;
    }

    // print the structure of "for" loop statement
    @Override
    public void print(int indent) {
        System.out.println(getIndent(indent) + "for statement");
        if(initialization != null) {
            initialization.print(indent + 1);
        }
        if(condition != null) {
            condition.print(indent + 1);
        }
        if(step_part != null){
            step_part.print(indent + 1);
        }
        if(body != null) {
            body.print(indent + 1);
        }
    }

    @Override
    public String checkSemantics(SymbolTable table) {

        this.initialization.checkSemantics(table);
        this.condition.checkSemantics(table);
        this.step_part.checkSemantics(table);
        this.body.checkSemantics(table);

        return "void";
    }

    @Override
    public void generateCode(Codegenerator cg) {
        rangenode range = (rangenode) condition;
        String loopVar = cg.getForLoopVar(initialization);
        int slot = cg.getOrAllocateVar(loopVar, "int");

        // start with rangenode
        range.generateRangeStart(cg);
        cg.emitStore("int", slot);

        // loop
        org.objectweb.asm.Label loopStart = cg.newLabel();
        org.objectweb.asm.Label loopEnd = cg.newLabel();
        cg.markLabel(loopStart);

        cg.emitLoad("int", slot);
        range.generateRangeEnd(cg);
        cg.emitJumpIfGreaterOrEqual(loopEnd);

        body.generateCode(cg);

        step_part.generateCode(cg);
        cg.emitStore("int", slot);
        cg.emitGoto(loopStart);
        cg.markLabel(loopEnd);
    }
}
