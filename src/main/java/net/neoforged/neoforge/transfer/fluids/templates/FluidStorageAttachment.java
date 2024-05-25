package net.neoforged.neoforge.transfer.fluids.templates;

import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.storage.ISingleResourceHandler;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class FluidStorageAttachment implements ISingleResourceHandler<FluidResource> {
    private final AttachmentHolder holder;
    private final Supplier<AttachmentType<SimpleFluidContent>> attachmentType;
    private final int limit;
    private Predicate<FluidResource> validator = r -> true;

    public FluidStorageAttachment(AttachmentHolder holder, Supplier<AttachmentType<SimpleFluidContent>> attachmentType, int limit) {
        this.holder = holder;
        this.attachmentType = attachmentType;
        this.limit = limit;
    }

    public FluidStorageAttachment setValidator(Predicate<FluidResource> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public FluidResource getResource() {
        return holder.getData(attachmentType).getResource();
    }

    @Override
    public int getAmount() {
        return holder.getData(attachmentType).getAmount();
    }

    @Override
    public int getLimit() {
        return limit;
    }

    @Override
    public boolean isResourceValid(FluidResource resource) {
        return validator.test(resource);
    }

    public boolean isEmpty() {
        return holder.getData(attachmentType).isEmpty();
    }

    @Override
    public boolean canInsert() {
        return true;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public int insert(FluidResource resource, int amount, TransferAction action) {
        if (resource.isBlank() || amount <= 0 || !isResourceValid(resource) || (!isEmpty() && !getResource().equals(resource))) return 0;
        int inserted = Math.min(amount, getLimit() - getAmount());
        if (inserted > 0 && action.isExecuting()) {
            holder.setData(attachmentType, SimpleFluidContent.of(resource, getAmount() + inserted));
        }
        return inserted;
    }

    @Override
    public int extract(FluidResource resource, int amount, TransferAction action) {
        if (resource.isBlank() || amount <= 0 || !isResourceValid(resource) || (isEmpty() || !getResource().equals(resource))) return 0;
        int extracted = Math.min(amount, getAmount());
        if (extracted > 0 && action.isExecuting()) {
            int newAmount = getAmount() - extracted;
            holder.setData(attachmentType, newAmount <= 0 ? SimpleFluidContent.EMPTY : SimpleFluidContent.of(resource, newAmount));
        }
        return extracted;
    }
}
