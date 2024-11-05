package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(ItemRequestDto itemRequestDto, Long requestorId, LocalDateTime created);
    ItemRequestDto getItemRequestById(Long itemRequestId, Long userId);
    List<ItemRequestDto> getOwnItemRequests(Long requestorId);
    List<ItemRequestDto> getAllItemRequests(Long userId, Integer from, Integer size);
}