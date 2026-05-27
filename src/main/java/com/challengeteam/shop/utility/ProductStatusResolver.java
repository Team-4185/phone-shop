package com.challengeteam.shop.utility;

import com.challengeteam.shop.entity.phone.ProductStatus;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProductStatusResolver {
  public static ProductStatus resolve(int stock) {
    if (stock == 0) {
      return ProductStatus.OUT_OF_STOCK;
    }
    if (stock <= 9) {
      return ProductStatus.LOW_STOCK;
    }
    return ProductStatus.IN_STOCK;
  }
}
