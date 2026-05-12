package com.challengeteam.shop.constraints.userData;

import lombok.experimental.UtilityClass;

@UtilityClass
public class InputUserValidationRules {
    public final String EMAIL_PATTERN_CONSTRAINT = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public final String NAME_PATTERN_CONSTRAINT = "^[a-zA-Zа-яА-Я]+(([',. -][a-zA-Zа-яА-Я ])?[a-zA-Zа-яА-Я]*)*$";
    public final String PHONE_NUMBER_PATTERN_CONSTRAINT = "^\\+[1-9]\\d{1,14}$";
}
