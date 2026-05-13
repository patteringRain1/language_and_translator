package compiler.Codegenerator;

import compiler.AST.basic.ASTnode;
import compiler.AST.expression.literalnode;
import org.objectweb.asm.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;


public class Codegenerator {

    // main class name
    private String className;

    // .class file location
    private String outputDir;

    // classwriter for main class
    private ClassWriter cw;

    // methodvistor
    private MethodVisitor mv;

    // global scope
    private boolean globalScope = true;

    // local variable table for current method
    private final Deque<Map<String, VarInfo>> localVarStack = new ArrayDeque<>();

    // static fields declaration
    private final Map<String, String> staticFields = new LinkedHashMap<>();

    // collection field information
    private final Map<String, Map<String, String>> collectionFields = new LinkedHashMap<>();

    // function descriptor
    private final Map<String, String> functionDescriptors = new LinkedHashMap<>();

    // function return type
    private final Map<String, String> functionReturnTypes = new LinkedHashMap<>();

    // current return type
    private String currentReturnType = "void";

    // next local variable slot in current method
    private int nextSlot = 0;

    // label counter
    private int labelCounter = 0;

    // method vistor used for static initialization
    private MethodVisitor clinitMv;

    // has clinit opened
    private boolean clinitOpen = false;

    // scanner slot in clinit
    private static final String SCANNER_FIELD = "_scanner";


    private static class VarInfo {
        final int slot;
        final String descriptor;
        final String ourType;

        VarInfo(int slot, String descriptor, String ourType) {
            this.slot = slot;
            this.descriptor = descriptor;
            this.ourType = ourType;
        }
    }


    // construction
    public Codegenerator(String outputPath) {
        File f = new File(outputPath);
        String fileName = f.getName();
        if (fileName.endsWith(".class")) {
            this.className = fileName.substring(0, fileName.length() - 6);
        } else {
            this.className = fileName;
        }
        this.outputDir = f.getParent() != null ? f.getParent() : ".";
    }

    public String getClassName() { return className; }

    public void beginClass() {
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V11,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER,
                className, null, "java/lang/Object", null);
        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
                SCANNER_FIELD, "Ljava/util/Scanner;", null, null).visitEnd();

        openClinit();

        // Scanner scanner = new Scanner(System.in);
        clinitMv.visitTypeInsn(Opcodes.NEW, "java/util/Scanner");
        clinitMv.visitInsn(Opcodes.DUP);
        clinitMv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        clinitMv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/Scanner", "<init>",
                "(Ljava/io/InputStream;)V", false);
        clinitMv.visitFieldInsn(Opcodes.PUTSTATIC, className, SCANNER_FIELD, "Ljava/util/Scanner;");
    }

    // called by ProgramNode after all declarations
    public void endClass() {
        if (clinitOpen) {
            clinitMv.visitInsn(Opcodes.RETURN);
            clinitMv.visitMaxs(0, 0);
            clinitMv.visitEnd();
            clinitOpen = false;
        }

        cw.visitEnd();
        byte[] bytes = cw.toByteArray();
        writeClassFile(className, bytes);
    }

    private void openClinit() {
        if (!clinitOpen) {
            clinitMv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            clinitMv.visitCode();
            clinitOpen = true;
        }
    }


    // collection class generation
    public void beginCollectionClass(String collName) {
        collectionFields.put(collName, new LinkedHashMap<>());
    }

    public void addCollectionField(String fieldName, String fieldType) {
        String desc = typeToDescriptor(fieldType);
        collectionFields.get(currentCollectionName()).put(fieldName, desc);
    }

    private String currentCollectionName() {
        String last = null;
        for (String k : collectionFields.keySet()) last = k;
        return last;
    }


    public void endCollectionClass(String collName, List<ASTnode> fields) {
        ClassWriter ccw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ccw.visit(Opcodes.V11,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER,
                collName, null, "java/lang/Object", null);

        Map<String, String> fieldMap = collectionFields.getOrDefault(collName, new LinkedHashMap<>());

        // declare fields
        for (Map.Entry<String, String> e : fieldMap.entrySet()) {
            ccw.visitField(Opcodes.ACC_PUBLIC, e.getKey(), e.getValue(), null, null).visitEnd();
        }

        StringBuilder ctorDesc = new StringBuilder("(");
        for (String desc : fieldMap.values()) ctorDesc.append(desc);
        ctorDesc.append(")V");

        MethodVisitor ctorMv = ccw.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
                ctorDesc.toString(), null, null);
        ctorMv.visitCode();

        ctorMv.visitVarInsn(Opcodes.ALOAD, 0);
        ctorMv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object",
                "<init>", "()V", false);

        int slot = 1;
        for (Map.Entry<String, String> e : fieldMap.entrySet()) {
            String desc = e.getValue();
            ctorMv.visitVarInsn(Opcodes.ALOAD, 0);
            ctorMv.visitVarInsn(loadOpcodeForDesc(desc), slot);
            ctorMv.visitFieldInsn(Opcodes.PUTFIELD, collName, e.getKey(), desc);
            slot += slotsForDesc(desc);
        }

        ctorMv.visitInsn(Opcodes.RETURN);
        ctorMv.visitMaxs(0, 0);
        ctorMv.visitEnd();

        ccw.visitEnd();
        writeClassFile(collName, ccw.toByteArray());
        functionDescriptors.put(collName + ".<init>", ctorDesc.toString());
    }


    // static field
    public boolean isGlobalScope() { return globalScope; }

    public void declareStaticField(String name, String type) {
        String desc = typeToDescriptor(type);
        staticFields.put(name, desc);
        cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, name, desc, null, null).visitEnd();
    }

    public void emitPutStatic(String name, String type) {
        String desc = typeToDescriptor(type);
        clinitMv.visitFieldInsn(Opcodes.PUTSTATIC, className, name, desc);
    }

    public void pushGlobalInitContext() {
        globalScope = true;
    }

    public void popGlobalInitContext() {
        globalScope = false;
    }

    // public static void main(String[] args)
    public void beginMainMethod() {
        globalScope = false;
        nextSlot = 1;
        localVarStack.push(new LinkedHashMap<>());
        currentReturnType = "void";
        mv = cw.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "main",
                "([Ljava/lang/String;)V",
                null, null);
        mv.visitCode();
    }

    public void endMainMethod() {
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        mv = null;
        globalScope = true;
        localVarStack.clear();
    }


    public void beginMethod(String name, String returnType,
                            List<String> paramTypes, List<String> paramNames) {
        globalScope = false;
        currentReturnType = returnType;
        nextSlot = 0;
        localVarStack.clear();
        localVarStack.push(new LinkedHashMap<>());

        StringBuilder desc = new StringBuilder("(");
        for (String pt : paramTypes) desc.append(typeToDescriptor(pt));
        desc.append(")");
        desc.append(typeToDescriptor(returnType));

        String descriptor = desc.toString();
        functionDescriptors.put(name, descriptor);
        functionReturnTypes.put(name, returnType);

        mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                name, descriptor, null, null);
        mv.visitCode();

        for (int i = 0; i < paramNames.size(); i++) {
            String pType = paramTypes.get(i);
            String pDesc = typeToDescriptor(pType);
            int slot = nextSlot;
            localVarStack.peek().put(paramNames.get(i), new VarInfo(slot, pDesc, pType));
            nextSlot += slotsForDesc(pDesc);
        }
    }

    public void endMethod(String returnType) {
        if ("void".equalsIgnoreCase(returnType)) {
            mv.visitInsn(Opcodes.RETURN);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        mv = null;
        globalScope = true;
        localVarStack.clear();
    }


    // local variable
    public int allocateLocalVar(String name, String type) {
        String desc = typeToDescriptor(type);
        int slot = nextSlot;
        if (localVarStack.isEmpty()) localVarStack.push(new LinkedHashMap<>());
        localVarStack.peek().put(name, new VarInfo(slot, desc, type));
        nextSlot += slotsForDesc(desc);
        return slot;
    }

    public int getOrAllocateVar(String name, String type) {
        VarInfo vi = findLocalVar(name);
        if (vi != null) return vi.slot;
        return allocateLocalVar(name, type);
    }

    public boolean isLocalVar(String name) { return findLocalVar(name) != null; }

    public int getLocalVarSlot(String name) {
        VarInfo vi = findLocalVar(name);
        return vi != null ? vi.slot : -1;
    }

    public String getLocalVarType(String name) {
        VarInfo vi = findLocalVar(name);
        return vi != null ? vi.ourType : "int";
    }

    private VarInfo findLocalVar(String name) {
        for (Map<String, VarInfo> scope : localVarStack) {
            if (scope.containsKey(name)) return scope.get(name);
        }
        return null;
    }

    public String getStaticFieldType(String name) {
        // Reverse-map descriptor to our type
        String desc = staticFields.get(name);
        if (desc == null) return "int";
        return descriptorToType(desc);
    }

    // scope stack
    public void enterScope() { localVarStack.push(new LinkedHashMap<>()); }

    public void exitScope() {
        if (!localVarStack.isEmpty()) localVarStack.pop();
    }

    private MethodVisitor activeMv() {
        return globalScope ? clinitMv : mv;
    }

    // push constants
    public void emitPushInt(int val) {
        MethodVisitor m = activeMv();
        if (val >= -1 && val <= 5) {
            m.visitInsn(Opcodes.ICONST_0 + val);
        } else if (val >= Byte.MIN_VALUE && val <= Byte.MAX_VALUE) {
            m.visitIntInsn(Opcodes.BIPUSH, val);
        } else if (val >= Short.MIN_VALUE && val <= Short.MAX_VALUE) {
            m.visitIntInsn(Opcodes.SIPUSH, val);
        } else {
            m.visitLdcInsn(val);
        }
    }

    public void emitPushFloat(float val) {
        activeMv().visitLdcInsn(val);
    }

    public void emitPushString(String val) {
        activeMv().visitLdcInsn(val);
    }

    // load or store
    public void emitLoad(String type, int slot) {
        MethodVisitor m = activeMv();
        String desc = typeToDescriptor(type);
        m.visitVarInsn(loadOpcodeForDesc(desc), slot);
    }

    public void emitStore(String type, int slot) {
        MethodVisitor m = activeMv();
        String desc = typeToDescriptor(type);
        m.visitVarInsn(storeOpcodeForDesc(desc), slot);
    }

    public void emitLoadVar(String name) {
        if (isLocalVar(name)) {
            VarInfo vi = findLocalVar(name);
            emitLoad(vi.ourType, vi.slot);
        } else {
            String desc = staticFields.getOrDefault(name, "I");
            activeMv().visitFieldInsn(Opcodes.GETSTATIC, className, name, desc);
        }
    }

    // static field access
    public void emitGetStatic(String owner, String name, String desc) {
        activeMv().visitFieldInsn(Opcodes.GETSTATIC, owner, name, desc);
    }

    public void emitPutStaticRaw(String owner, String name, String desc) {
        activeMv().visitFieldInsn(Opcodes.PUTSTATIC, owner, name, desc);
    }

    // instance field access for collections
    public void emitGetField(String owner, String name, String desc) {
        activeMv().visitFieldInsn(Opcodes.GETFIELD, owner, name, desc);
    }

    public void emitPutField(String owner, String name, String desc) {
        activeMv().visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
    }

    // assignment
    public void emitAssign(ASTnode target) {
        if (target instanceof literalnode) {
            literalnode lit = (literalnode) target;
            String lt = lit.getLiteralType().toLowerCase();

            if (lt.equals("identifier")) {
                String name = (String) lit.getValue();
                if (isLocalVar(name)) {
                    VarInfo vi = findLocalVar(name);
                    activeMv().visitVarInsn(storeOpcodeForDesc(vi.descriptor), vi.slot);
                } else {
                    String desc = staticFields.getOrDefault(name, "I");
                    activeMv().visitFieldInsn(Opcodes.PUTSTATIC, className, name, desc);
                }
            } else if (lt.equals("fieldaccess")) {
                String fullName = (String) lit.getValue();
                emitFieldStore(fullName);
            }
        }
    }

    public void emitFieldAccess(String fullName) {
        int lastDot = fullName.lastIndexOf('.');
        if (lastDot < 0) {
            emitLoadVar(fullName);
            return;
        }
        String objPath = fullName.substring(0, lastDot);
        String fieldName = fullName.substring(lastDot + 1);

        emitLoadPath(objPath);

        String collType = inferCollectionTypeFromPath(objPath);
        if (collType != null) {
            Map<String, String> fields = collectionFields.getOrDefault(collType, new LinkedHashMap<>());
            String fieldDesc = fields.getOrDefault(fieldName, "I");
            activeMv().visitFieldInsn(Opcodes.GETFIELD, collType, fieldName, fieldDesc);
        }
    }

    private void emitFieldStore(String fullName) {
        int lastDot = fullName.lastIndexOf('.');
        if (lastDot < 0) {
            emitAssignSimple(fullName);
            return;
        }
        String objPath = fullName.substring(0, lastDot);
        String fieldName = fullName.substring(lastDot + 1);
        int tempSlot = nextSlot++;
        activeMv().visitVarInsn(Opcodes.ASTORE, tempSlot);

        emitLoadPath(objPath);

        String collType = inferCollectionTypeFromPath(objPath);
        if (collType != null) {
            Map<String, String> fields = collectionFields.getOrDefault(collType, new LinkedHashMap<>());
            String fieldDesc = fields.getOrDefault(fieldName, "I");
            activeMv().visitVarInsn(loadOpcodeForDesc(fieldDesc), tempSlot);
            activeMv().visitFieldInsn(Opcodes.PUTFIELD, collType, fieldName, fieldDesc);
        }
        nextSlot--;
    }

    private void emitAssignSimple(String name) {
        if (isLocalVar(name)) {
            VarInfo vi = findLocalVar(name);
            activeMv().visitVarInsn(storeOpcodeForDesc(vi.descriptor), vi.slot);
        } else {
            String desc = staticFields.getOrDefault(name, "I");
            activeMv().visitFieldInsn(Opcodes.PUTSTATIC, className, name, desc);
        }
    }

    private void emitLoadPath(String path) {
        if (path.endsWith("]")) {
            int bracketOpen = path.lastIndexOf('[');
            String arrName = path.substring(0, bracketOpen);
            String idxStr = path.substring(bracketOpen + 1, path.length() - 1);
            emitLoadVar(arrName);
            try {
                int idx = Integer.parseInt(idxStr);
                emitPushInt(idx);
            } catch (NumberFormatException e) {
                emitLoadVar(idxStr);
            }
            activeMv().visitInsn(Opcodes.AALOAD);
        } else {
            emitLoadVar(path);
        }
    }

    private String inferCollectionTypeFromPath(String path) {
        String base = path.contains("[") ? path.substring(0, path.indexOf('[')) : path;
        VarInfo vi = findLocalVar(base);
        if (vi != null) {
            return descriptorToCollectionName(vi.descriptor);
        }
        String desc = staticFields.get(base);
        if (desc != null) return descriptorToCollectionName(desc);
        return null;
    }

    private String descriptorToCollectionName(String desc) {
        if (desc.startsWith("L") && desc.endsWith(";")) {
            return desc.substring(1, desc.length() - 1);
        }
        if (desc.startsWith("[L") && desc.endsWith(";")) {
            return desc.substring(2, desc.length() - 1);
        }
        return null;
    }

    // array operations
    public void emitNewArray(String elementType) {
        MethodVisitor m = activeMv();
        switch (elementType.toLowerCase()) {
            case "int":
                m.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
                break;
            case "float":
                m.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_FLOAT);
                break;
            case "bool":
                m.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
                break;
            case "string":
                m.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/String");
                break;
            default:
                m.visitTypeInsn(Opcodes.ANEWARRAY, elementType);
        }
    }

    public void emitArrayLoad(String elementType) {
        MethodVisitor m = activeMv();
        switch ((elementType != null ? elementType : "int").toLowerCase()) {
            case "int":  m.visitInsn(Opcodes.IALOAD); break;
            case "float":m.visitInsn(Opcodes.FALOAD); break;
            case "bool": m.visitInsn(Opcodes.BALOAD); break;
            default:     m.visitInsn(Opcodes.AALOAD); break;
        }
    }

    public void emitArrayStore(String elementType) {
        MethodVisitor m = activeMv();
        switch ((elementType != null ? elementType : "int").toLowerCase()) {
            case "int":  m.visitInsn(Opcodes.IASTORE); break;
            case "float":m.visitInsn(Opcodes.FASTORE); break;
            case "bool": m.visitInsn(Opcodes.BASTORE); break;
            default:     m.visitInsn(Opcodes.AASTORE); break;
        }
    }

    public String getArrayElementType(String arrayName) {
        String desc;
        VarInfo vi = findLocalVar(arrayName);
        if (vi != null) desc = vi.descriptor;
        else desc = staticFields.getOrDefault(arrayName, "[I");

        if (desc.startsWith("[")) {
            return descriptorToType(desc.substring(1));
        }
        return "int";
    }

    // method invocation
    public void emitInvokeVirtual(String owner, String name, String desc) {
        activeMv().visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, name, desc, false);
    }

    public void emitInvokeStatic(String owner, String name, String desc) {
        activeMv().visitMethodInsn(Opcodes.INVOKESTATIC, owner, name, desc, false);
    }

    public void emitInvokeSpecial(String owner, String name, String desc) {
        activeMv().visitMethodInsn(Opcodes.INVOKESPECIAL, owner, name, desc, false);
    }

    // object creation
    public void emitNew(String typeName) {
        activeMv().visitTypeInsn(Opcodes.NEW, typeName);
    }

    // i/o build
    public void emitReadInt() {
        MethodVisitor m = activeMv();
        m.visitFieldInsn(Opcodes.GETSTATIC, className, SCANNER_FIELD, "Ljava/util/Scanner;");
        m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextInt", "()I", false);
    }

    public void emitReadFloat() {
        MethodVisitor m = activeMv();
        m.visitFieldInsn(Opcodes.GETSTATIC, className, SCANNER_FIELD, "Ljava/util/Scanner;");
        m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextFloat", "()F", false);
    }

    public void emitReadString() {
        MethodVisitor m = activeMv();
        m.visitFieldInsn(Opcodes.GETSTATIC, className, SCANNER_FIELD, "Ljava/util/Scanner;");
        m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextLine",
                "()Ljava/lang/String;", false);
    }

    // string concatenation
    public void emitStringConcat() {
        MethodVisitor m = activeMv();

        int tempSlot = nextSlot++;
        m.visitVarInsn(Opcodes.ASTORE, tempSlot);
        m.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
        m.visitInsn(Opcodes.DUP_X1);
        m.visitInsn(Opcodes.SWAP);
        m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder",
                "<init>", "(Ljava/lang/String;)V", false);

        m.visitVarInsn(Opcodes.ALOAD, tempSlot);
        m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder",
                "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);

        m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder",
                "toString", "()Ljava/lang/String;", false);
        nextSlot--;
    }

    // control flow
    public Label newLabel() { return new Label(); }

    public void markLabel(Label label) { activeMv().visitLabel(label); }

    public void emitGoto(Label label) { activeMv().visitJumpInsn(Opcodes.GOTO, label); }

    public void emitJumpIfFalse(Label label) {
        activeMv().visitJumpInsn(Opcodes.IFEQ, label);
    }

    public void emitJumpIfGreaterOrEqual(Label label) {
        activeMv().visitJumpInsn(Opcodes.IF_ICMPGE, label);
    }

    // comparison operators
    public void emitComparison(String op, String type) {
        MethodVisitor m = activeMv();
        Label trueLabel = new Label();
        Label endLabel = new Label();

        if ("float".equalsIgnoreCase(type)) {
            m.visitInsn(Opcodes.FCMPL);
            switch (op) {
                case "==": m.visitJumpInsn(Opcodes.IFEQ,  trueLabel); break;
                case "!=": m.visitJumpInsn(Opcodes.IFNE,  trueLabel); break;
                case "<":  m.visitJumpInsn(Opcodes.IFLT,  trueLabel); break;
                case "<=": m.visitJumpInsn(Opcodes.IFLE,  trueLabel); break;
                case ">":  m.visitJumpInsn(Opcodes.IFGT,  trueLabel); break;
                case ">=": m.visitJumpInsn(Opcodes.IFGE,  trueLabel); break;
                default:   m.visitJumpInsn(Opcodes.IFEQ,  trueLabel);
            }
        } else if ("string".equalsIgnoreCase(type)) {
            m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String",
                    "equals", "(Ljava/lang/Object;)Z", false);
            if ("!=".equals(op)) {
                m.visitJumpInsn(Opcodes.IFEQ, trueLabel);
            } else {
                m.visitJumpInsn(Opcodes.IFNE, trueLabel);
            }
        } else {
            switch (op) {
                case "==": m.visitJumpInsn(Opcodes.IF_ICMPEQ, trueLabel); break;
                case "!=": m.visitJumpInsn(Opcodes.IF_ICMPNE, trueLabel); break;
                case "<":  m.visitJumpInsn(Opcodes.IF_ICMPLT, trueLabel); break;
                case "<=": m.visitJumpInsn(Opcodes.IF_ICMPLE, trueLabel); break;
                case ">":  m.visitJumpInsn(Opcodes.IF_ICMPGT, trueLabel); break;
                case ">=": m.visitJumpInsn(Opcodes.IF_ICMPGE, trueLabel); break;
                default:   m.visitJumpInsn(Opcodes.IF_ICMPEQ, trueLabel);
            }
        }
        m.visitInsn(Opcodes.ICONST_0);
        m.visitJumpInsn(Opcodes.GOTO, endLabel);
        m.visitLabel(trueLabel);
        m.visitInsn(Opcodes.ICONST_1);
        m.visitLabel(endLabel);
    }

    // return
    public String getCurrentReturnType() { return currentReturnType; }

    public void emitReturn(String type) {
        MethodVisitor m = activeMv();
        switch (type.toLowerCase()) {
            case "void": m.visitInsn(Opcodes.RETURN);   break;
            case "int":
            case "bool": m.visitInsn(Opcodes.IRETURN);  break;
            case "float":m.visitInsn(Opcodes.FRETURN);  break;
            default:     m.visitInsn(Opcodes.ARETURN);  break;
        }
    }

    // raw instruction emit
    public void emitInstruction(int opcode) {
        activeMv().visitInsn(opcode);
    }

    // "for" loop
    public String getForLoopVar(ASTnode init) {
        if (init instanceof compiler.AST.declaration.vardeclarationnode) {
            return ((compiler.AST.declaration.vardeclarationnode) init).getName();
        }
        return "";
    }

    // type system
    public String typeToDescriptor(String type) {
        if (type == null) return "V";
        switch (type.toLowerCase()) {
            case "int":    return "I";
            case "float":  return "F";
            case "bool":   return "I";
            case "string": return "Ljava/lang/String;";
            case "void":   return "V";
            case "int[]":  return "[I";
            case "float[]":return "[F";
            case "bool[]": return "[I";
            case "string[]":return "[Ljava/lang/String;";
            default:
                if (type.endsWith("[]")) {
                    String base = type.substring(0, type.length() - 2);
                    return "[L" + base + ";";
                }
                return "L" + type + ";";
        }
    }

    private String descriptorToType(String desc) {
        if (desc == null) return "void";
        switch (desc) {
            case "I": return "int";
            case "F": return "float";
            case "V": return "void";
            case "Ljava/lang/String;": return "string";
            case "[I": return "int[]";
            case "[F": return "float[]";
            default:
                if (desc.startsWith("L") && desc.endsWith(";")) return desc.substring(1, desc.length()-1);
                if (desc.startsWith("[L") && desc.endsWith(";")) return desc.substring(2, desc.length()-1) + "[]";
                return "int";
        }
    }

    private int loadOpcodeForDesc(String desc) {
        if (desc == null) return Opcodes.ILOAD;
        switch (desc) {
            case "I": case "Z": case "B": case "C": case "S": return Opcodes.ILOAD;
            case "F": return Opcodes.FLOAD;
            case "J": return Opcodes.LLOAD;
            case "D": return Opcodes.DLOAD;
            default:  return Opcodes.ALOAD;
        }
    }

    private int storeOpcodeForDesc(String desc) {
        if (desc == null) return Opcodes.ISTORE;
        switch (desc) {
            case "I": case "Z": case "B": case "C": case "S": return Opcodes.ISTORE;
            case "F": return Opcodes.FSTORE;
            case "J": return Opcodes.LSTORE;
            case "D": return Opcodes.DSTORE;
            default:  return Opcodes.ASTORE;
        }
    }

    private int slotsForDesc(String desc) {
        return ("J".equals(desc) || "D".equals(desc)) ? 2 : 1;
    }

    // function or collection queries
    public boolean isCollectionConstructor(String name) {
        return collectionFields.containsKey(name);
    }

    public String getCollectionConstructorDesc(String collName) {
        return functionDescriptors.getOrDefault(collName + ".<init>", "()V");
    }

    public String getFunctionDescriptor(String name) {
        return functionDescriptors.getOrDefault(name, "()V");
    }

    public String getFunctionReturnType(String name) {
        return functionReturnTypes.getOrDefault(name, "void");
    }

    public String getExprType(ASTnode node) {
        if (node instanceof literalnode) {
            literalnode lit = (literalnode) node;
            String lt = lit.getLiteralType().toLowerCase();
            if (lt.equals("integer") || lt.equals("int")) return "int";
            if (lt.equals("float")) return "float";
            if (lt.equals("string")) return "string";
            if (lt.equals("boolean") || lt.equals("bool")) return "bool";
            if (lt.equals("identifier")) {
                String name = (String) lit.getValue();
                if (isLocalVar(name)) return getLocalVarType(name);
                return getStaticFieldType(name);
            }
            if (lt.equals("fieldaccess")) {
                String fullName = (String) lit.getValue();
                int dot = fullName.lastIndexOf('.');
                if (dot >= 0) {
                    String field = fullName.substring(dot + 1);
                    String obj = fullName.substring(0, dot).replaceAll("\\[.*\\]", "");
                    String collType;
                    if (isLocalVar(obj)) {
                        collType = descriptorToCollectionName(
                                typeToDescriptor(getLocalVarType(obj)));
                    } else {
                        collType = descriptorToCollectionName(
                                staticFields.getOrDefault(obj, "I"));
                    }
                    if (collType != null) {
                        String desc = collectionFields
                                .getOrDefault(collType, new LinkedHashMap<>())
                                .getOrDefault(field, "I");
                        return descriptorToType(desc);
                    }
                }
                return "int";
            }
        }
        return "int";
    }

    // file i/o
    private void writeClassFile(String name, byte[] bytes) {
        File dir = new File(outputDir);
        if (!dir.exists()) dir.mkdirs();
        File out = new File(dir, name + ".class");
        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write class file: " + out.getAbsolutePath(), e);
        }
    }
}