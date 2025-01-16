package com.bikkadIt.ElectronicStore.repository;

import com.bikkadIt.ElectronicStore.entity.Cart;
import com.bikkadIt.ElectronicStore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository  extends JpaRepository <Cart,String> {

    Optional<Cart> findByUser(User user);
}
