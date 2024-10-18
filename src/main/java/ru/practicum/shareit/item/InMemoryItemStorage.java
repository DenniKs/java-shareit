package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.toList;

@Component("InMemoryItemStorage")
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();
    private Long currentId = 0L;

    @Override
    public Item create(Item item) {
        if (isValidItem(item)) {
            item.setId(++currentId);
            items.put(item.getId(), item);
        }
        return item;
    }

    @Override
    public Item update(Item item) {
        if (item.getId() == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        if (!items.containsKey(item.getId())) {
            throw new ItemNotFoundException("Вещь с ID=" + item.getId() + " не найдена!");
        }

        Item existingItem = items.get(item.getId());
        if (item.getName() == null) {
            item.setName(existingItem.getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(existingItem.getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(existingItem.getAvailable());
        }

        if (isValidItem(item)) {
            items.put(item.getId(), item);
        }
        return item;
    }

    @Override
    public Item delete(Long itemId) {
        if (itemId == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        if (!items.containsKey(itemId)) {
            throw new ItemNotFoundException("Вещь с ID=" + itemId + " не найдена!");
        }
        return items.remove(itemId);
    }

    @Override
    public List<Item> getItemsByOwner(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwnerId().equals(ownerId))
                .collect(toList());
    }

    @Override
    public void deleteItemsByOwner(Long ownerId) {
        List<Long> deleteIds = items.values().stream()
                .filter(item -> item.getOwnerId().equals(ownerId))
                .map(Item::getId)
                .collect(toList());
        for (Long deleteId : deleteIds) {
            items.remove(deleteId);
        }
    }

    @Override
    public Item getItemById(Long itemId) {
        if (!items.containsKey(itemId)) {
            throw new ItemNotFoundException("Вещь с ID=" + itemId + " не найдена!");
        }
        return items.get(itemId);
    }

    @Override
    public List<Item> getItemsBySearchQuery(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }

        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .collect(toList());
    }

    private boolean isValidItem(Item item) {
        if (item.getName() == null || item.getName().isEmpty() ||
                item.getDescription() == null || item.getDescription().isEmpty() ||
                item.getAvailable() == null) {
            throw new ValidationException("У вещи некорректные данные");
        }
        return true;
    }
}
