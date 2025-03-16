package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TLCSuspendContext extends XSuspendContext {
    private final VirtualFile file;
    private final PsiElement element;

    @Override
    public @Nullable XExecutionStack getActiveExecutionStack() {
        return new TLCExecutionStack(file, element);
    }
}
