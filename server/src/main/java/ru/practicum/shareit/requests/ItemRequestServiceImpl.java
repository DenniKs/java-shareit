package ru.practicum.shareit.requests;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.service.CheckConsistencyService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.util.Pagination;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository repository;
    private final CheckConsistencyService checker;
    private final UserService userService;
    private final ItemService itemService;

    @Autowired
    public ItemRequestServiceImpl(ItemRequestRepository repository,
                                  CheckConsistencyService checkConsistencyService, UserService userService, ItemService itemService) {
        this.repository = repository;
        this.checker = checkConsistencyService;
        this.userService = userService;
        this.itemService = itemService;
    }

    @Override
    public ItemRequestDto create(ItemRequestDto itemRequestDto, Long requestorId, LocalDateTime created) {
        User user = userService.findUserById(requestorId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, user, created);
        return ItemRequestMapper.toItemRequestDto(repository.save(itemRequest), itemService.getItemsByRequestId(itemRequest.getId()));
    }

    @Override
    public ItemRequestDto getItemRequestById(Long itemRequestId, Long userId) {
        checker.isExistUser(userId);
        ItemRequest itemRequest = repository.findById(itemRequestId)
                .orElseThrow(() -> new ItemRequestNotFoundException("Запрос с ID=" + itemRequestId + " не найден!"));
        return ItemRequestMapper.toItemRequestDto(itemRequest, itemService.getItemsByRequestId(itemRequest.getId()));
    }

    @Override
    public List<ItemRequestDto> getOwnItemRequests(Long requestorId) {
        checker.isExistUser(requestorId);
        return repository.findAllByRequestorId(requestorId,
                        Sort.by(Sort.Direction.DESC, "created")).stream()
                .map((ItemRequest itemRequest) -> ItemRequestMapper.toItemRequestDto(itemRequest, itemService.getItemsByRequestId(itemRequest.getId())))
                .collect(toList());
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId, Integer from, Integer size) {
        checker.isExistUser(userId);
        List<ItemRequestDto> listItemRequestDto = new ArrayList<>();
        Pageable pageable;
        Page<ItemRequest> page;
        Pagination pager = new Pagination(from, size);
        Sort sort = Sort.by(Sort.Direction.DESC, "created");

        if (size == null) {
            List<ItemRequest> listItemRequest = repository.findAllByRequestorIdNotOrderByCreatedDesc(userId);
            listItemRequestDto
                    .addAll(listItemRequest.stream().skip(from).map((ItemRequest itemRequest) -> ItemRequestMapper.toItemRequestDto(itemRequest, itemService.getItemsByRequestId(itemRequest.getId()))).toList());
        } else {
            for (int i = pager.getIndex(); i < pager.getTotalPages(); i++) {
                pageable =
                        PageRequest.of(i, pager.getPageSize(), sort);
                page = repository.findAllByRequestorIdNot(userId, pageable);
                listItemRequestDto.addAll(page.stream().map((ItemRequest itemRequest) -> ItemRequestMapper.toItemRequestDto(itemRequest, itemService.getItemsByRequestId(itemRequest.getId()))).toList());
                if (!page.hasNext()) {
                    break;
                }
            }
            listItemRequestDto = listItemRequestDto.stream().limit(size).collect(toList());
        }
        return listItemRequestDto;
    }
}