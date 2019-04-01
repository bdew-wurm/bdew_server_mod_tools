package net.bdew.wurm.tools.server.bytecode;

import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;

public class ByteCodeUtils {
    public static void putInteger(ConstPool cp, Bytecode code, int val) {
        switch (val) {
            case -1:
                code.add(Bytecode.ICONST_M1);
                break;
            case 0:
                code.add(Bytecode.ICONST_0);
                break;
            case 1:
                code.add(Bytecode.ICONST_1);
                break;
            case 2:
                code.add(Bytecode.ICONST_2);
                break;
            case 3:
                code.add(Bytecode.ICONST_3);
                break;
            case 4:
                code.add(Bytecode.ICONST_4);
                break;
            case 5:
                code.add(Bytecode.ICONST_5);
                break;
            default:
                if (val >= Byte.MIN_VALUE && val <= Byte.MAX_VALUE) {
                    code.add(Bytecode.BIPUSH);
                    code.add(val);
                } else if (val >= Short.MIN_VALUE && val <= Short.MAX_VALUE) {
                    code.add(Bytecode.SIPUSH);
                    code.add(val >> 8 & 0xFF, val & 0xFF);
                } else {
                    code.addLdc(cp.addIntegerInfo(val));
                }
        }
    }

    public static int getInteger(ConstPool cp, CodeIterator iterator, int pos) {
        int op = iterator.byteAt(pos);
        switch (op) {
            case Bytecode.ICONST_M1:
                return -1;
            case Bytecode.ICONST_0:
                return 0;
            case Bytecode.ICONST_1:
                return 1;
            case Bytecode.ICONST_2:
                return 2;
            case Bytecode.ICONST_3:
                return 3;
            case Bytecode.ICONST_4:
                return 4;
            case Bytecode.ICONST_5:
                return 5;
            case Bytecode.BIPUSH:
                return iterator.byteAt(pos + 1);
            case Bytecode.SIPUSH:
                return iterator.s16bitAt(pos + 1);
            case Bytecode.LDC:
                return cp.getIntegerInfo(iterator.byteAt(pos + 1));
            case Bytecode.LDC_W:
                return cp.getIntegerInfo(iterator.u16bitAt(pos + 1));
            default:
                throw new RuntimeException(String.format("Failed to decode integer. Pos = %d, Bytecode = %d", pos, op));
        }
    }
}
