package net.neoforged.neoforge.transfer.storage.wrappers;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.TransferUtils;
import net.neoforged.neoforge.transfer.storage.IResourceHandler;
import net.neoforged.neoforge.transfer.storage.IResourceHandlerModifiable;

import java.util.function.Supplier;

public class ScopedHandlerWrapper<T extends IResource> extends DelegatingHandlerWrapper<T> {
    protected int[] indices;

    public ScopedHandlerWrapper(IResourceHandler<T> delegate, int[] indices) {
        super(delegate);
        this.indices = indices;
    }

    public ScopedHandlerWrapper(Supplier<IResourceHandler<T>> delegate, int[] indices) {
        super(delegate);
        this.indices = indices;
    }

    @Override
    public int size() {
        return indices.length;
    }

    @Override
    protected int convertIndex(int index) {
        return indices[index];
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        return TransferUtils.insertIndices(getDelegate(), indices, resource, amount, action);
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        return TransferUtils.extractIndices(getDelegate(), indices, resource, amount, action);
    }

    public static class Modifiable<T extends IResource> extends ScopedHandlerWrapper<T> implements IResourceHandlerModifiable<T> {
        public Modifiable(IResourceHandlerModifiable<T> delegate, int[] indices) {
            super(delegate, indices);
        }

        public Modifiable(Supplier<IResourceHandlerModifiable<T>> delegate, int[] indices) {
            super(delegate::get, indices);
        }

        @Override
        public void set(int index, T resource, int amount) {
            getDelegate().set(convertIndex(index), resource, amount);
        }

        @Override
        public IResourceHandlerModifiable<T> getDelegate() {
            return (IResourceHandlerModifiable<T>) super.getDelegate();
        }
    }
}
