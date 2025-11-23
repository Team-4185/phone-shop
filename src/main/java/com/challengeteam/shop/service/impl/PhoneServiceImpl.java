package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.phone.PhoneCreateRequestDto;
import com.challengeteam.shop.dto.phone.PhoneUpdateRequestDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.service.PhoneService;
import com.challengeteam.shop.service.impl.merger.PhoneMerger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneServiceImpl implements PhoneService {

    private final PhoneRepository phoneRepository;

    private final PhoneMerger phoneMerger;

    @Transactional(readOnly = true)
    @Override
    public Optional<Phone> getById(Long id) {
        Objects.requireNonNull(id, "id");

        log.debug("Get phone by id: {}", id);
        return phoneRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Phone> getPhones(int page, int size) {
        log.debug("Get phones page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return phoneRepository.findAll(pageable);
    }

    @Transactional
    @Override
    public Long create(PhoneCreateRequestDto phoneCreateRequestDto) {
        Objects.requireNonNull(phoneCreateRequestDto, "phoneCreateRequestDto");

        var phone = Phone.builder()
                .name(phoneCreateRequestDto.name())
                .description(phoneCreateRequestDto.description())
                .price(phoneCreateRequestDto.price())
                .brand(phoneCreateRequestDto.brand())
                .releaseYear(phoneCreateRequestDto.releaseYear())
                .build();

        phone = phoneRepository.save(phone);
        log.debug("Created new phone: {}", phone);
        return phone.getId();
    }

    @Transactional
    @Override
    public void update(Long id, PhoneUpdateRequestDto phoneUpdateRequestDto) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(phoneUpdateRequestDto, "phoneUpdateRequestDto");

        Phone phone = phoneRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found phone with id: " + id));
        phoneMerger.mergePhone(phone, phoneUpdateRequestDto);
        phoneRepository.save(phone);
        log.debug("Updated phone: {}", phone);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Objects.requireNonNull(id, "id");

        if(!phoneRepository.existsById(id)) {
            throw new ResourceNotFoundException("Not found phone with id: " + id);
        }

        phoneRepository.deleteById(id);
        log.debug("Deleted phone with id: {}", id);
    }

}
