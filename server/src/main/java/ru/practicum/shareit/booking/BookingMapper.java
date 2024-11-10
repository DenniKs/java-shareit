package ru.practicum.shareit.booking;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;


import java.util.List;

@UtilityClass
public class BookingMapper {

    public BookingDto toBookingDto(Booking booking, List<CommentDto> comments) {
        if (booking != null) {
            return new BookingDto(
                    booking.getId(),
                    booking.getStart(),
                    booking.getEnd(),
                    ItemMapper.toItemDto(booking.getItem(), comments),
                    UserMapper.toUserDto(booking.getBooker()),
                    booking.getStatus()
            );
        }

        return null;
    }

    public BookingShortDto toBookingShortDto(Booking booking) {
        if (booking != null) {
            return new BookingShortDto(
                    booking.getId(),
                    booking.getBooker().getId(),
                    booking.getStart(),
                    booking.getEnd()
            );
        }

        return null;
    }

    public Booking toBooking(BookingInputDto bookingInputDto, User booker, Item item) {
        return new Booking(
                null,
                bookingInputDto.getStart(),
                bookingInputDto.getEnd(),
                item,
                booker,
                Status.WAITING
        );
    }
}