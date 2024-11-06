package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.service.CheckConsistencyService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.util.Pagination;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final CommentRepository commentRepository;
    private final CheckConsistencyService checker;
    private final UserService userService;
    private final BookingService bookingService;
    private final ItemService itemService;

    @Autowired
    @Lazy
    public ItemServiceImpl(ItemRepository repository, CommentRepository commentRepository,
                           CheckConsistencyService checkConsistencyService, UserService userService, BookingService bookingService, ItemService itemService) {
        this.repository = repository;
        this.commentRepository = commentRepository;
        this.checker = checkConsistencyService;
        this.userService = userService;
        this.bookingService = bookingService;
        this.itemService = itemService;
    }

    @Override
    public ItemDto getItemById(Long id, Long userId) {
        Item item = repository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с ID=" + id + " не найдена!"));

        BookingShortDto lastBooking = bookingService.getLastBooking(item.getId());
        BookingShortDto nextBooking = bookingService.getNextBooking(item.getId());
        List<CommentDto> comments = itemService.getCommentsByItemId(item.getId());

        return userId.equals(item.getOwner().getId())
                ? ItemMapper.toItemExtDto(item, lastBooking, nextBooking, comments)
                : ItemMapper.toItemDto(item, comments);
    }

    @Override
    public Item findItemById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с ID=" + id + " не найдена!"));
    }

    @Override
    public ItemDto create(ItemDto itemDto, Long ownerId) {
        User owner = userService.findUserById(ownerId);
        if (userService.getUserById(ownerId) == null) {
            throw new UserNotFoundException("Пользователь с ID " + ownerId + " не найден!");
        }
        List<CommentDto> comments = itemService.getCommentsByItemId(itemDto.getId());
        return ItemMapper.toItemDto(repository.save(ItemMapper.toItem(itemDto, owner)), comments);
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId, Integer from, Integer size) {
        checker.isExistUser(ownerId);
        List<ItemDto> listItemExtDto = new ArrayList<>();
        Pageable pageable;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Page<Item> page;
        Pagination pager = new Pagination(from, size);


        if (size == null) {
            pageable =
                    PageRequest.of(pager.getIndex(), pager.getPageSize(), sort);
            do {
                page = repository.findByOwnerId(ownerId, pageable);
                listItemExtDto.addAll(page.stream().map(item -> {
                    BookingShortDto lastBooking = bookingService.getLastBooking(item.getId());
                    BookingShortDto nextBooking = bookingService.getNextBooking(item.getId());
                    List<CommentDto> comments = itemService.getCommentsByItemId(item.getId());
                    return ItemMapper.toItemExtDto(item, lastBooking, nextBooking, comments);
                }).collect(Collectors.toList()));
                pageable = pageable.next();
            } while (page.hasNext());

        } else {
            for (int i = pager.getIndex(); i < pager.getTotalPages(); i++) {
                pageable =
                        PageRequest.of(i, pager.getPageSize(), sort);
                page = repository.findByOwnerId(ownerId, pageable);
                listItemExtDto.addAll(page.stream().map(item -> {
                    BookingShortDto lastBooking = bookingService.getLastBooking(item.getId());
                    BookingShortDto nextBooking = bookingService.getNextBooking(item.getId());
                    List<CommentDto> comments = itemService.getCommentsByItemId(item.getId());
                    return ItemMapper.toItemExtDto(item, lastBooking, nextBooking, comments);
                }).collect(Collectors.toList()));
                if (!page.hasNext()) {
                    break;
                }
            }
            listItemExtDto = listItemExtDto.stream().limit(size).collect(toList());
        }
        return listItemExtDto;
    }

    @Override
    public void delete(Long itemId, Long ownerId) {
        Item item = repository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с ID=" + itemId + " не найдена!"));
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ItemNotFoundException("У пользователя нет такой вещи!");
        }
        repository.deleteById(itemId);
    }

    @Override
    public List<ItemDto> getItemsBySearchQuery(String text, Integer from, Integer size) {
        List<ItemDto> listItemDto = new ArrayList<>();
        if ((text != null) && (!text.isEmpty()) && (!text.isBlank())) {
            text = text.toLowerCase();
            Pageable pageable;
            Sort sort = Sort.by(Sort.Direction.ASC, "name");
            Page<Item> page;
            Pagination pager = new Pagination(from, size);

            if (size == null) {
                pageable =
                        PageRequest.of(pager.getIndex(), pager.getPageSize(), sort);
                do {
                    page = repository.getItemsBySearchQuery(text, pageable);
                    listItemDto.addAll(page.stream().map(item -> {
                        List<CommentDto> comments = itemService.getCommentsByItemId(item.getId());
                        return ItemMapper.toItemDto(item, comments);
                    }).collect(Collectors.toList()));
                    pageable = pageable.next();
                } while (page.hasNext());

            } else {
                for (int i = pager.getIndex(); i < pager.getTotalPages(); i++) {
                    pageable =
                            PageRequest.of(i, pager.getPageSize(), sort);
                    page = repository.getItemsBySearchQuery(text, pageable);
                    listItemDto.addAll(page.stream().map(item -> {
                        List<CommentDto> comments = itemService.getCommentsByItemId(item.getId());
                        return ItemMapper.toItemDto(item, comments);
                    }).collect(Collectors.toList()));
                    if (!page.hasNext()) {
                        break;
                    }
                }
                listItemDto = listItemDto.stream().limit(size).collect(toList());
            }
        }
        return listItemDto;
    }

    @Override
    public ItemDto update(ItemDto itemDto, Long ownerId, Long itemId) {
        checker.isExistUser(ownerId);
        Item item = repository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с ID=" + itemId + " не найдена!"));
        List<CommentDto> comments = itemService.getCommentsByItemId(itemId);
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ItemNotFoundException("У пользователя нет такой вещи!");
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(repository.save(item), comments);
    }

    @Override
    public CommentDto createComment(CommentDto commentDto, Long itemId, Long userId) {
        checker.isExistUser(userId);
        Comment comment = new Comment();
        Booking booking = bookingService.getBookingWithUserBookedItem(itemId, userId);
        if (booking == null) {
            throw new ValidationException("Данный пользователь вещь не бронировал!");
        }
        comment.setCreated(LocalDateTime.now());
        comment.setItem(booking.getItem());
        comment.setAuthor(booking.getBooker());
        comment.setText(commentDto.getText());

        return ItemMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getCommentsByItemId(Long itemId) {
        return commentRepository.findAllByItemId(itemId,
                        Sort.by(Sort.Direction.DESC, "created")).stream()
                .map(ItemMapper::toCommentDto)
                .collect(toList());
    }

    @Override
    public List<ItemDto> getItemsByRequestId(Long requestId) {
        return repository.findAllByRequestId(requestId,
                        Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(item -> {
                    List<CommentDto> comments = itemService.getCommentsByItemId(item.getId());
                    return ItemMapper.toItemDto(item, comments);
                }).collect(toList());
    }
}