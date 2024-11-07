package ru.practicum.shareit.requests;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllByRequestorId(Long requestorId, Sort sort);

    Page<ItemRequest> findAllByRequestorIdNot(Long userId, Pageable pageable);

    List<ItemRequest> findAllByRequestorIdNotOrderByCreatedDesc(Long userId);
}