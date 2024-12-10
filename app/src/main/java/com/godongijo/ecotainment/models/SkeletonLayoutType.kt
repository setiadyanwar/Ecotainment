package com.godongijo.ecotainment.models

import com.godongijo.ecotainment.R

enum class SkeletonLayoutType(val layoutResId: Int) {
    PRODUCT(R.layout.skeleton_view_product),
    WISHLIST(R.layout.skeleton_view_wishlist),
    HISTORY(R.layout.skeleton_view_history),
    CART(R.layout.skeleton_view_cart),
    ORDER_STATUS(R.layout.skeleton_view_order_status),
    ADDRESS(R.layout.skeleton_view_address),
    MANAGE_PRODUCT(R.layout.skeleton_view_manage_product),
    MANAGE_TRANSACTION(R.layout.skeleton_view_manage_transaction),
    MANAGE_BANK(R.layout.skeleton_view_manage_bank)
}