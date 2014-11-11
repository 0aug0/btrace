/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.btrace.runtime;

import com.sun.btrace.org.objectweb.asm.MethodVisitor;
import static com.sun.btrace.org.objectweb.asm.Opcodes.*;
import static com.sun.btrace.runtime.Constants.*;

import com.sun.btrace.org.objectweb.asm.Type;

/**
 *
 * @author jbachorik
 */
final public class Assembler {
    private final MethodVisitor mv;
    public Assembler(MethodVisitor mv) {
        this.mv = mv;
    }

    public Assembler push(int value) {
        if (value >= -1 && value <= 5) {
            mv.visitInsn(ICONST_0 + value);
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            mv.visitIntInsn(BIPUSH, value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            mv.visitIntInsn(SIPUSH, value);
        } else {
            mv.visitLdcInsn(value);
        }
        return this;
    }

    public Assembler arrayLoad(Type type) {
        mv.visitInsn(type.getOpcode(IALOAD));
        return this;
    }

    public Assembler arrayStore(Type type) {
        mv.visitInsn(type.getOpcode(IASTORE));
        return this;
    }

    public Assembler ldc(Object o) {
        mv.visitLdcInsn(o);
        return this;
    }

    public Assembler sub(Type t) {
        int opcode = -1;
        switch (t.getSort()) {
            case Type.SHORT:
            case Type.BYTE:
            case Type.INT: {
                opcode = ISUB;
                break;
            }
            case Type.LONG: {
                opcode = LSUB;
                break;
            }
            case Type.FLOAT: {
                opcode = FSUB;
                break;
            }
            case Type.DOUBLE: {
                opcode = DSUB;
                break;
            }
        }
        if (opcode != -1) {
            mv.visitInsn(opcode);
        }
        return this;
    }

    public Assembler loadNull() {
        mv.visitInsn(ACONST_NULL);
        return this;
    }

    public Assembler loadLocal(Type type, int index) {
        mv.visitVarInsn(type.getOpcode(ILOAD), index);
        return this;
    }

    public Assembler storeLocal(Type type, int index) {
        mv.visitVarInsn(type.getOpcode(ISTORE), index);
        return this;
    }

    public Assembler pop() {
        mv.visitInsn(POP);
        return this;
    }

    public Assembler dup() {
        mv.visitInsn(DUP);
        return this;
    }

    public Assembler dup2() {
        mv.visitInsn(DUP2);
        return this;
    }

    public Assembler swap() {
        mv.visitInsn(SWAP);
        return this;
    }

    public Assembler newInstance(Type t) {
        mv.visitTypeInsn(NEW, t.getInternalName());
        return this;
    }

    public Assembler newArray(Type t) {
        mv.visitTypeInsn(ANEWARRAY, t.getInternalName());
        return this;
    }

    public Assembler dupArrayValue(int arrayOpcode) {
        switch (arrayOpcode) {
            case IALOAD: case FALOAD:
            case AALOAD: case BALOAD:
            case CALOAD: case SALOAD:
            case IASTORE: case FASTORE:
            case AASTORE: case BASTORE:
            case CASTORE: case SASTORE:
                dup();
            break;

            case LALOAD: case DALOAD:
            case LASTORE: case DASTORE:
                dup2();
            break;
        }
        return this;
    }

    public Assembler dupReturnValue(int returnOpcode) {
        switch (returnOpcode) {
            case IRETURN:
            case FRETURN:
            case ARETURN:
                mv.visitInsn(DUP);
                break;
            case LRETURN:
            case DRETURN:
                mv.visitInsn(DUP2);
                break;
            case RETURN:
                break;
            default:
                throw new IllegalArgumentException("not return");
        }
        return this;
    }

    public Assembler dupValue(Type type) {
        switch (type.getSize()) {
            case 1:
                dup();
            break;
            case 2:
                dup2();
            break;
        }
        return this;
    }

    public Assembler dupValue(String desc) {
        int typeCode = desc.charAt(0);
        switch (typeCode) {
            case '[':
            case 'L':
            case 'Z':
            case 'C':
            case 'B':
            case 'S':
            case 'I':
                mv.visitInsn(DUP);
                break;
            case 'J':
            case 'D':
                mv.visitInsn(DUP2);
                break;
            default:
                throw new RuntimeException("invalid signature");
        }
        return this;
    }

    public Assembler box(Type type) {
        return box(type.getDescriptor());
    }

    public Assembler box(String desc) {
        int typeCode = desc.charAt(0);
        switch (typeCode) {
            case '[':
            case 'L':
                break;
            case 'Z':
                invokeStatic(JAVA_LANG_BOOLEAN,
                                BOX_VALUEOF,
                                BOX_BOOLEAN_DESC);
                break;
            case 'C':
                invokeStatic(JAVA_LANG_CHARACTER,
                                BOX_VALUEOF,
                                BOX_CHARACTER_DESC);
                break;
            case 'B':
                invokeStatic(JAVA_LANG_BYTE,
                                BOX_VALUEOF,
                                BOX_BYTE_DESC);
                break;
            case 'S':
                invokeStatic(JAVA_LANG_SHORT,
                                BOX_VALUEOF,
                                BOX_SHORT_DESC);
                break;
            case 'I':
                invokeStatic(JAVA_LANG_INTEGER,
                                BOX_VALUEOF,
                                BOX_INTEGER_DESC);
                break;
            case 'J':
                invokeStatic(JAVA_LANG_LONG,
                                BOX_VALUEOF,
                                BOX_LONG_DESC);
                break;
            case 'F':
                invokeStatic(JAVA_LANG_FLOAT,
                                BOX_VALUEOF,
                                BOX_FLOAT_DESC);
                break;
            case 'D':
                invokeStatic(JAVA_LANG_DOUBLE,
                                BOX_VALUEOF,
                                BOX_DOUBLE_DESC);
                break;
        }
        return this;
    }

    public Assembler unbox(Type type) {
        return unbox(type.getDescriptor());
    }

    public Assembler unbox(String desc) {
        int typeCode = desc.charAt(0);
        switch (typeCode) {
            case '[':
            case 'L':
                mv.visitTypeInsn(CHECKCAST, Type.getType(desc).getInternalName());
                break;
            case 'Z':
                mv.visitTypeInsn(CHECKCAST, JAVA_LANG_BOOLEAN);
                invokeVirtual(JAVA_LANG_BOOLEAN,
                                BOOLEAN_VALUE,
                                BOOLEAN_VALUE_DESC);
                break;
            case 'C':
                mv.visitTypeInsn(CHECKCAST, JAVA_LANG_CHARACTER);
                invokeVirtual(JAVA_LANG_CHARACTER,
                                CHAR_VALUE,
                                CHAR_VALUE_DESC);
                break;
            case 'B':
                mv.visitTypeInsn(CHECKCAST, JAVA_LANG_NUMBER);
                invokeVirtual(JAVA_LANG_NUMBER,
                                BYTE_VALUE,
                                BYTE_VALUE_DESC);
                break;
            case 'S':
                mv.visitTypeInsn(CHECKCAST, JAVA_LANG_NUMBER);
                invokeVirtual(JAVA_LANG_NUMBER,
                                SHORT_VALUE,
                                SHORT_VALUE_DESC);
                break;
            case 'I':
                mv.visitTypeInsn(CHECKCAST, JAVA_LANG_NUMBER);
                invokeVirtual(JAVA_LANG_NUMBER,
                                INT_VALUE,
                                INT_VALUE_DESC);
                break;
            case 'J':
                mv.visitTypeInsn(CHECKCAST, JAVA_LANG_NUMBER);
                invokeVirtual(JAVA_LANG_NUMBER,
                                LONG_VALUE,
                                LONG_VALUE_DESC);
                break;
            case 'F':
                mv.visitTypeInsn(CHECKCAST, JAVA_LANG_NUMBER);
                invokeVirtual(JAVA_LANG_NUMBER,
                                FLOAT_VALUE,
                                FLOAT_VALUE_DESC);
                break;
            case 'D':
                mv.visitTypeInsn(CHECKCAST, JAVA_LANG_NUMBER);
                invokeVirtual(JAVA_LANG_NUMBER,
                                DOUBLE_VALUE,
                                DOUBLE_VALUE_DESC);
                break;
        }
        return this;
    }

    public Assembler defaultValue(String desc) {
        int typeCode = desc.charAt(0);
        switch (typeCode) {
            case '[':
            case 'L':
                mv.visitInsn(ACONST_NULL);
                break;
            case 'Z':
            case 'C':
            case 'B':
            case 'S':
            case 'I':
                mv.visitInsn(ICONST_0);
                break;
            case 'J':
                mv.visitInsn(LCONST_0);
                break;
            case 'F':
                mv.visitInsn(FCONST_0);
                break;
            case 'D':
                mv.visitInsn(DCONST_0);
                break;
        }
        return this;
    }

    public Assembler println(String msg) {
        mv.visitFieldInsn(GETSTATIC,
                    "java/lang/System",
                    "out",
                    "Ljava/io/PrintStream;");
        mv.visitLdcInsn(msg);
        invokeVirtual("java/io/PrintStream",
                    "println",
                    "(Ljava/lang/String;)V");
        return this;
    }

    // print the object on the top of the stack
    public Assembler printObject() {
        mv.visitFieldInsn(GETSTATIC,
                    "java/lang/System",
                    "out",
                    "Ljava/io/PrintStream;");
        mv.visitInsn(SWAP);
        mv.visitMethodInsn(INVOKEVIRTUAL,
                    "java/io/PrintStream",
                    "println",
                    "(Ljava/lang/Object;)V");
        return this;
    }

    public Assembler invokeVirtual(String owner, String method, String desc) {
        mv.visitMethodInsn(INVOKEVIRTUAL, owner, method, desc, false);
        return this;
    }

    public Assembler invokeSpecial(String owner, String method, String desc) {
        mv.visitMethodInsn(INVOKESPECIAL, owner, method, desc, false);
        return this;
    }

    public Assembler invokeStatic(String owner, String method, String desc) {
        mv.visitMethodInsn(INVOKESTATIC, owner, method, desc, false);
        return this;
    }

    public Assembler invokeInterface(String owner, String method, String desc) {
        mv.visitMethodInsn(INVOKEVIRTUAL, owner, method, desc, true);
        return this;
    }

    public Assembler getStatic(String owner, String name, String desc) {
        mv.visitFieldInsn(GETSTATIC, owner, name, desc);
        return this;
    }

    public Assembler putStatic(String owner, String name, String desc) {
        mv.visitFieldInsn(PUTSTATIC, owner, name, desc);
        return this;
    }
}
