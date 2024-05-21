package net.neoforged.neoforge.transfer.storage;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;

public interface IStorage<T extends IResource> {
    int getSlotCount();

    T getResource(int slot);

    int getAmount(int slot);

    int getSlotLimit(int slot);

    boolean isResourceValid(int slot, T resource);

    boolean canInsert();

    boolean canExtract();

    int insert(int slot, T resource, int amount, TransferAction action);

    int insert(T resource, int amount, TransferAction action);

    int extract(int slot, T resource, int amount, TransferAction action);

    int extract(T resource, int amount, TransferAction action);

    static <T extends IResource> Class<IStorage<T>> asClass() {
        return (Class<IStorage<T>>) (Object) IStorage.class;
    }
}