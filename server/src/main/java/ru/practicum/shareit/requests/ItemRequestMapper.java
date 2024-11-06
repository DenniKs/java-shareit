package ru.practicum.shareit.requests;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;

import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
public class ItemRequestMapper {

    public ItemRequestDto toItemRequestDto(ItemRequest itemRequest, List<ItemDto> items) {
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                UserMapper.toUserDto(itemRequest.getRequestor()),
                itemRequest.getCreated(),
                items
        );
    }

    public ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User user, LocalDateTime created) {
        return new ItemRequest(
                null,
                itemRequestDto.getDescription(),
                user,
                created
        );
    }
}