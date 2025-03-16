package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;

public class TLCExecutionStack extends XExecutionStack {
    private final VirtualFile file;
    private final PsiElement element;

    public TLCExecutionStack(VirtualFile file, PsiElement element) {
        super("foo stack");
        this.file = file;
        this.element = element;
    }

    @Override
    public @Nullable XStackFrame getTopFrame() {
        return new TLCStackFrame(file, element);
    }

    @Override
    public void computeStackFrames(int firstFrameIndex, XStackFrameContainer container) {

    }
}
