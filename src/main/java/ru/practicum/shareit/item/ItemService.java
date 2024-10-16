package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class ItemService {
    private final ItemStorage itemStorage;

    @Autowired
    public ItemService(@Qualifier("InMemoryItemStorage") ItemStorage itemStorage) {
        this.itemStorage = itemStorage;
    }

    public ItemDto create(ItemDto itemDto, Long ownerId) {
        return ItemMapper.toItemDto(itemStorage.create(ItemMapper.toItem(itemDto, ownerId)));
    }

    public List<ItemDto> getItemsByOwner(Long ownerId) {
        return itemStorage.getItemsByOwner(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(toList());
    }

    public ItemDto getItemById(Long id) {
        return ItemMapper.toItemDto(itemStorage.getItemById(id));
    }

    public ItemDto update(ItemDto itemDto, Long ownerId, Long itemId) {
        if (itemDto.getId() == null) {
            itemDto.setId(itemId);
        }
        Item oldItem = itemStorage.getItemById(itemId);
        if (!oldItem.getOwnerId().equals(ownerId)) {
            throw new ItemNotFoundException("У пользователя нет такой вещи!");
        }
        return ItemMapper.toItemDto(itemStorage.update(ItemMapper.toItem(itemDto, ownerId)));
    }

    public ItemDto delete(Long itemId, Long ownerId) {
        Item item = itemStorage.getItemById(itemId);
        if (!item.getOwnerId().equals(ownerId)) {
            throw new ItemNotFoundException("У пользователя нет такой вещи!");
        }
        return ItemMapper.toItemDto(itemStorage.delete(itemId));
    }

    public void deleteItemsByOwner(Long ownerId) {
        itemStorage.deleteItemsByOwner(ownerId);
    }

    public List<ItemDto> getItemsBySearchQuery(String text) {
        text = text.toLowerCase();
        return itemStorage.getItemsBySearchQuery(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(toList());
    }
}