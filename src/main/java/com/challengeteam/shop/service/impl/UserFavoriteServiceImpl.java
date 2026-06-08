package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.phone.response.PhoneResponseDto;
import com.challengeteam.shop.entity.favorite.Favorite;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.persistence.repository.FavoriteRepository;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.service.CatalogProductResponseAssembler;
import com.challengeteam.shop.service.UserFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserFavoriteServiceImpl implements UserFavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final PhoneRepository phoneRepository;
    private final CatalogProductResponseAssembler catalogProductResponseAssembler;

    @Transactional(readOnly = true)
    @Override
    public List<PhoneResponseDto> getUserFavorites(Long userId) {
        Objects.requireNonNull(userId, "userId");

        List<Phone> phones = favoriteRepository.findAllByUserIdWithPhoneImages(userId).stream()
                .map(Favorite::getPhone)
                .toList();

        return catalogProductResponseAssembler.toResponses(phones);
    }

    @Transactional
    @Override
    public List<PhoneResponseDto> addProductToFavorites(Long userId, Long phoneId) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(phoneId, "phoneId");

        Phone phone = findPhoneOrThrow(phoneId);

        if (!favoriteRepository.existsByUserIdAndPhoneId(userId, phoneId)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));

            favoriteRepository.save(Favorite.builder()
                    .user(user)
                    .phone(phone)
                    .build());
        }

        return getUserFavorites(userId);
    }

    @Transactional
    @Override
    public List<PhoneResponseDto> removeProductFromFavorites(Long userId, Long phoneId) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(phoneId, "phoneId");

        findPhoneOrThrow(phoneId);

        favoriteRepository.findByUserIdAndPhoneId(userId, phoneId)
                .ifPresent(favoriteRepository::delete);

        return getUserFavorites(userId);
    }

    private Phone findPhoneOrThrow(Long phoneId) {
        return phoneRepository.findById(phoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Phone with id " + phoneId + " not found"));
    }
}
