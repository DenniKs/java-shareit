package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.service.CheckConsistencyService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository repository;
    private final CheckConsistencyService checker;
    private final UserServiceImpl userService;
    private final ItemServiceImpl itemService;

    @Autowired
    @Lazy
    public BookingServiceImpl(BookingRepository bookingRepository,
                              CheckConsistencyService checkConsistencyService, UserServiceImpl userService, ItemServiceImpl itemService) {
        this.repository = bookingRepository;
        this.checker = checkConsistencyService;
        this.userService = userService;
        this.itemService = itemService;
    }

    @Override
    public BookingDto create(BookingInputDto bookingInputDto, Long bookerId) {
        checker.isExistUser(bookerId);

        if (!checker.isAvailableItem(bookingInputDto.getItemId())) {
            throw new ValidationException("Вещь с ID=" + bookingInputDto.getItemId() +
                    " недоступна для бронирования!");
        }
        User user = userService.findUserById(bookerId);
        Item item = itemService.findItemById(bookingInputDto.getItemId());
        List<CommentDto> comments = itemService.getCommentsByItemId(item.getId());

        if (bookerId.equals(item.getOwner().getId())) {
            throw new BookingNotFoundException("Вещь с ID=" + bookingInputDto.getItemId() +
                    " недоступна для бронирования самим владельцем!");
        }
        Booking booking = BookingMapper.toBooking(bookingInputDto, user, item);
        return BookingMapper.toBookingDto(repository.save(booking), comments);
    }

    @Override
    public BookingDto update(Long bookingId, Long userId, Boolean approved) {
        checker.isExistUser(userId);
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование с ID=" + bookingId + " не найдено!"));
        if (booking.getEnd().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Время бронирования уже истекло!");
        }

        if (booking.getBooker().getId().equals(userId)) {
            if (approved) {
                throw new UserNotFoundException("Подтвердить бронирование может только владелец вещи!");
            }
            booking.setStatus(Status.CANCELED);
            log.info("Пользователь с ID={} отменил бронирование с ID={}", userId, bookingId);
        } else if ((checker.isItemOwner(booking.getItem().getId(), userId)) &&
                (!booking.getStatus().equals(Status.CANCELED))) {
            if (!booking.getStatus().equals(Status.WAITING)) {
                throw new ValidationException("Решение по бронированию уже принято!");
            }
            if (approved) {
                booking.setStatus(Status.APPROVED);
                log.info("Пользователь с ID={} подтвердил бронирование с ID={}", userId, bookingId);
            } else {
                booking.setStatus(Status.REJECTED);
                log.info("Пользователь с ID={} отклонил бронирование с ID={}", userId, bookingId);
            }
        } else {
            throw new ValidationException(
                booking.getStatus().equals(Status.CANCELED) ? "Бронирование было отменено!" : "Подтвердить бронирование может только владелец вещи!"
            );
        }
        List<CommentDto> comments = itemService.getCommentsByItemId(booking.getItem().getId());
        return BookingMapper.toBookingDto(repository.save(booking), comments);
    }

    @Override
    public BookingDto getBookingById(Long bookingId, Long userId) {
        checker.isExistUser(userId);
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование с ID=" + bookingId + " не найдено!"));
        if (booking.getBooker().getId().equals(userId) || checker.isItemOwner(booking.getItem().getId(), userId)) {
            List<CommentDto> comments = itemService.getCommentsByItemId(booking.getItem().getId());
            return BookingMapper.toBookingDto(booking, comments);
        }
        throw new UserNotFoundException("Посмотреть данные бронирования может только владелец вещи" + " или бронирующий ее!");
    }

    @Override
    public List<BookingDto> getBookings(String state, Long userId) {
        checker.isExistUser(userId);
        List<Booking> bookings;
        Sort sortByStartDesc = Sort.by(Sort.Direction.DESC, "start");
        switch (state) {
            case "ALL":
                bookings = repository.findByBookerId(userId, sortByStartDesc);
                break;
            case "CURRENT":
                bookings = repository.findByBookerIdAndStartIsBeforeAndEndIsAfter(userId, LocalDateTime.now(),
                        LocalDateTime.now(), sortByStartDesc);
                break;
            case "PAST":
                bookings = repository.findByBookerIdAndEndIsBefore(userId, LocalDateTime.now(), sortByStartDesc);
                break;
            case "FUTURE":
                bookings = repository.findByBookerIdAndStartIsAfter(userId, LocalDateTime.now(), sortByStartDesc);
                break;
            case "WAITING":
                bookings = repository.findByBookerIdAndStatus(userId, Status.WAITING, sortByStartDesc);
                break;
            case "REJECTED":
                bookings = repository.findByBookerIdAndStatus(userId, Status.REJECTED, sortByStartDesc);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }
        return bookings.stream()
                .map((Booking booking) -> BookingMapper.toBookingDto(booking, itemService.getCommentsByItemId(booking.getItem().getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getBookingsOwner(String state, Long userId) {
        checker.isExistUser(userId);
        List<Booking> bookings;
        Sort sortByStartDesc = Sort.by(Sort.Direction.DESC, "start");
        switch (state) {
            case "ALL":
                bookings = repository.findByItem_Owner_Id(userId, sortByStartDesc);
                break;
            case "CURRENT":
                bookings = repository.findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(userId, LocalDateTime.now(),
                        LocalDateTime.now(), sortByStartDesc);
                break;
            case "PAST":
                bookings = repository.findByItem_Owner_IdAndEndIsBefore(userId, LocalDateTime.now(), sortByStartDesc);
                break;
            case "FUTURE":
                bookings = repository.findByItem_Owner_IdAndStartIsAfter(userId, LocalDateTime.now(),
                        sortByStartDesc);
                break;
            case "WAITING":
                bookings = repository.findByItem_Owner_IdAndStatus(userId, Status.WAITING, sortByStartDesc);
                break;
            case "REJECTED":
                bookings = repository.findByItem_Owner_IdAndStatus(userId, Status.REJECTED, sortByStartDesc);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }
        return bookings.stream()
                .map((Booking booking) -> BookingMapper.toBookingDto(booking, itemService.getCommentsByItemId(booking.getItem().getId())))
                .collect(Collectors.toList());
    }

    @Override
    public BookingShortDto getLastBooking(Long itemId) {
        return BookingMapper.toBookingShortDto(repository.findFirstByItem_IdAndEndBeforeOrderByEndDesc(itemId,
                LocalDateTime.now()));
    }

    @Override
    public BookingShortDto getNextBooking(Long itemId) {
        return BookingMapper.toBookingShortDto(repository.findFirstByItem_IdAndStartAfterOrderByStartAsc(itemId,
                LocalDateTime.now()));
    }

    @Override
    public Booking getBookingWithUserBookedItem(Long itemId, Long userId) {
        return repository.findFirstByItem_IdAndBooker_IdAndEndIsBeforeAndStatus(itemId,
                userId, LocalDateTime.now(), Status.APPROVED);
    }
}