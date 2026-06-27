package com.challengeteam.shop.service.user.personalInfo;

import com.challengeteam.shop.dto.user.response.UserPersonalInfoResponseDto;
import com.challengeteam.shop.entity.favorite.Favorite;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.user.InvalidUserCredentialsException;
import com.challengeteam.shop.exceptionHandling.exception.user.UsernameMissingException;
import com.challengeteam.shop.mapper.user.UserPersonalInfoMapper;
import com.challengeteam.shop.persistence.repository.FavoriteRepository;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPersonalInfoServiceImpl implements UserPersonalInfoService {

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final OrderRepository orderRepository;
    private final UserPersonalInfoMapper userPersonalInfoMapper;

    @Transactional(readOnly = true)
    @Override
    public UserPersonalInfoResponseDto getUserPersonalInfo(String username) {
        if (username == null || username.isBlank()) {
            log.error("User email is blank: {}", username);
            throw new UsernameMissingException("User email cannot be blank");
        }
        Optional<User> userOptional = userRepository.findByEmail(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            log.debug("User was found by username: {}", username);
            long id = user.getId();
            List<Favorite> allByUserIdWithPhoneImages = favoriteRepository.findAllByUserIdWithPhoneImages(id);
            List<Order> userOrders = orderRepository.findAllByUserId(id);
            return userPersonalInfoMapper.toDto(user, allByUserIdWithPhoneImages, userOrders);
        } else {
            log.error("User not found by username: {}", username);
            throw new InvalidUserCredentialsException("User %s was not found".formatted(username));
        }
    }
}