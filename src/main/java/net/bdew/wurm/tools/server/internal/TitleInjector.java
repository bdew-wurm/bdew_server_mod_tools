package net.bdew.wurm.tools.server.internal;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;
import javassist.bytecode.*;
import net.bdew.wurm.tools.server.bytecode.ByteCodeUtils;

public class TitleInjector {
    private final CtClass titleCls;
    private final ConstPool constPool;
    private final CodeIterator codeIterator;
    private int insertPos = -1;
    private int lastOrd = -1;
    private int arraySizePos = -1;

    public static TitleInjector INSTANCE;

    TitleInjector(CtClass titleCls) throws BadBytecode, NotFoundException {
        this.titleCls = titleCls;
        CtConstructor initializer = titleCls.getClassInitializer();
        CodeAttribute codeAttr = initializer.getMethodInfo().getCodeAttribute();
        constPool = codeAttr.getConstPool();
        codeIterator = codeAttr.iterator();

        // My code needs a bit more stack space than javac-generated one
        codeAttr.setMaxStack(codeAttr.getMaxStack() + 3);

        while (codeIterator.hasNext()) {
            int pos = codeIterator.next();
            int op = codeIterator.byteAt(pos);
            if (op == Bytecode.AASTORE) {
                insertPos = codeIterator.next();
            } else if (op == Bytecode.ANEWARRAY) {
                arraySizePos = pos - 2;
            } else if (op == Bytecode.NEW) {
                pos = codeIterator.next(); // dup
                pos = codeIterator.next(); // ldc of ident
                pos = codeIterator.next(); // here's the ordinal
                lastOrd = ByteCodeUtils.getInteger(constPool, codeIterator, pos);
            }
        }

        if (insertPos == -1) throw new RuntimeException("Failed to find AASTORE");
        if (lastOrd == -1) throw new RuntimeException("Failed to find ordinals");
        if (arraySizePos == -1) throw new RuntimeException("Failed to array size position");
    }

    public void addTitle(int id, String name, String femaleName, int skillId, String type) throws BadBytecode, CannotCompileException, NotFoundException {
        int ordinal = ++lastOrd;
        Bytecode code = new Bytecode(constPool);

        // When starting the values array is on stack, dup it for later use
        code.add(Bytecode.DUP);

        // Put out ordinal, will be used by AASTORE
        ByteCodeUtils.putInteger(constPool, code, ordinal);

        // Make new instance, and dupe that too
        code.addNew("com.wurmonline.server.players.Titles$Title");
        code.add(Bytecode.DUP);

        // Put constructor parameters into stack
        code.addLdc("CUSTOM_" + id);
        ByteCodeUtils.putInteger(constPool, code, ordinal);
        ByteCodeUtils.putInteger(constPool, code, id);
        code.addLdc(name);
        code.addLdc(femaleName);
        ByteCodeUtils.putInteger(constPool, code, skillId);
        code.addGetstatic("com.wurmonline.server.players.Titles$TitleType", type, "Lcom/wurmonline/server/players/Titles$TitleType;");

        // Call constructor, this will use one copy of our instance duped above
        code.addInvokespecial("com.wurmonline.server.players.Titles$Title", "<init>", "(Ljava/lang/String;IILjava/lang/String;Ljava/lang/String;ILcom/wurmonline/server/players/Titles$TitleType;)V");

        // And stick it into values array, this will use the duped array, ordinal and the other copy of our instance
        code.add(Bytecode.AASTORE);

        // End of bytecode gen, insert it into the initializer
        byte[] bytes = code.get();
        codeIterator.insertAt(insertPos, bytes);
        insertPos += bytes.length;

        // And increase array size
        codeIterator.write16bit(codeIterator.u16bitAt(arraySizePos) + 1, arraySizePos);
    }
}