package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.mayreh.intellij.plugin.tlaplus.TLAplusFileType;

public class TLCBreakpointType extends XLineBreakpointType<TLCBreakpointProperties> {
    public TLCBreakpointType() {
        super("TLCBreakpointType", "TLC Breakpoint");
    }

    @Override
    public @Nullable TLCBreakpointProperties createBreakpointProperties(@NotNull VirtualFile file, int line) {
        // We don't support any additional properties for now, so just return null
        // as described in the XLineBreakpointType javadoc
        return null;
    }

    @Override
    public boolean canPutAt(@NotNull VirtualFile file, int line, @NotNull Project project) {
        if (!TLAplusFileType.INSTANCE.getName().equals(file.getFileType().getName())) {
            return false;
        }

        // Taken from JavaLineBreakpointTypeBase.canPutAtElement
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return false;
        }
        Ref<Boolean> canPut = new Ref<>(false);

        // We only sanity check that the line is not a comment or whitespace.
        // More complete check will be done when registering breakpoint after TLC debugger starts up.
        XDebuggerUtil.getInstance().iterateLine(project, document, line, element -> {
            if (element instanceof PsiComment) {
                return true;
            }
            if (element instanceof PsiWhiteSpace) {
                return true;
            }
            canPut.set(true);
            return false;
        });

        return canPut.get();
    }
}
