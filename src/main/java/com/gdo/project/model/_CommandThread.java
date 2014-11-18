package com.gdo.project.model;

import com.gdo.project.model.ThreadStcl.ICommandThread;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.cmd.CommandContext;
import com.gdo.stencils.plug.PStcl;

/**
 * 
 * Abstract class to make a command running as a thread.
 * 
 * @author gdo
 * 
 */
public abstract class _CommandThread implements ICommandThread {
    private ThreadStcl _thread;

    public _CommandThread(CommandContext<StclContext, PStcl> cmdContext, PStcl reference) {
        ThreadStcl.createThread(cmdContext, this, reference);
    }

    @Override
    public void setThread(ThreadStcl thread) {
        _thread = thread;
    }

    protected StclContext getStencilContext() {
        return _thread.getStencilContext();
    }

    protected CommandContext<StclContext, PStcl> getCommandContext() {
        return _thread.getCommandContext();
    }

    protected PStcl getThread() {
        return _thread.self();
    }

    protected PStcl getReference() {
        return _thread.getReference();
    }

}
