package net.neoforged.neoforge.transfer.storage.templates;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.storage.IStorage;

import java.util.stream.Stream;

public class AggregateStorage<T extends IResource> implements IStorage<T> {
    IStorage<T>[] storages;

    public AggregateStorage(IStorage<T>... storages) {
        this.storages = storages;
    }

    @Override
    public int getSlotCount() {
        return Stream.of(storages).mapToInt(IStorage::getSlotCount).sum();
    }

    @Override
    public T getResource(int slot) {
        for (IStorage<T> storage : storages) {
            if (slot < storage.getSlotCount()) {
                return storage.getResource(slot);
            }
            slot -= storage.getSlotCount();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int getAmount(int slot) {
        for (IStorage<T> storage : storages) {
            if (slot < storage.getSlotCount()) {
                return storage.getAmount(slot);
            }
            slot -= storage.getSlotCount();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int getSlotLimit(int slot) {
        for (IStorage<T> storage : storages) {
            if (slot < storage.getSlotCount()) {
                return storage.getSlotLimit(slot);
            }
            slot -= storage.getSlotCount();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean isResourceValid(int slot, T resource) {
        for (IStorage<T> storage : storages) {
            if (slot < storage.getSlotCount()) {
                return storage.isResourceValid(slot, resource);
            }
            slot -= storage.getSlotCount();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean isEmpty(int slot) {
        for (IStorage<T> storage : storages) {
            if (slot < storage.getSlotCount()) {
                return storage.isEmpty(slot);
            }
            slot -= storage.getSlotCount();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean canInsert() {
        for (IStorage<T> storage : storages) {
            if (storage.canInsert()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canExtract() {
        for (IStorage<T> storage : storages) {
            if (storage.canExtract()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int insert(int slot, T resource, int amount, TransferAction action) {
        for (IStorage<T> storage : storages) {
            if (slot < storage.getSlotCount()) {
                if (storage.canInsert()) {
                    return storage.insert(slot, resource, amount, action);
                } else {
                    return 0;
                }
            }
            slot -= storage.getSlotCount();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        int inserted = 0;
        for (IStorage<T> storage : storages) {
            if (storage.canInsert()) {
                inserted += storage.insert(resource, amount - inserted, action);
            }
            if (inserted >= amount) {
                return inserted;
            }
        }
        return inserted;
    }

    @Override
    public int extract(int slot, T resource, int amount, TransferAction action) {
        for (IStorage<T> storage : storages) {
            if (slot < storage.getSlotCount()) {
                if (storage.canExtract()) {
                    return storage.extract(slot, resource, amount, action);
                } else {
                    return 0;
                }
            }
            slot -= storage.getSlotCount();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        int extracted = 0;
        for (IStorage<T> storage : storages) {
            if (storage.canExtract()) {
                extracted += storage.extract(resource, amount - extracted, action);
            }
            if (extracted >= amount) {
                return extracted;
            }
        }
        return extracted;
    }
}