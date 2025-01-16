package com.bikkadIt.ElectronicStore.service.impl;

import com.bikkadIt.ElectronicStore.dtos.AddItemToCartRequest;
import com.bikkadIt.ElectronicStore.dtos.CartDto;
import com.bikkadIt.ElectronicStore.entity.Cart;
import com.bikkadIt.ElectronicStore.entity.CartItem;
import com.bikkadIt.ElectronicStore.entity.Product;
import com.bikkadIt.ElectronicStore.entity.User;
import com.bikkadIt.ElectronicStore.exception.BadApiRequestException;
import com.bikkadIt.ElectronicStore.exception.ResourceNotFoundException;
import com.bikkadIt.ElectronicStore.repository.CartItemRepository;
import com.bikkadIt.ElectronicStore.repository.CartRepository;
import com.bikkadIt.ElectronicStore.repository.ProductRepository;
import com.bikkadIt.ElectronicStore.repository.UserRepository;
import com.bikkadIt.ElectronicStore.service.CartService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    public CartDto addItemToCart(String userId, AddItemToCartRequest request) {
        int quantity = request.getQuantity();
        String productId = request.getProductId();
          if (quantity <=0){
              throw new BadApiRequestException("Request quantity is not valid !!");
          }
        System.out.println("this is the");
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("this add for product item"));

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found in database"));
               Cart cart = null;
               try{
                   cart = cartRepository.findByUser(user).get();
               }catch (NoSuchElementException e){
                   cart = new Cart();
                   cart.setCartId(UUID.randomUUID().toString());
                   cart.setCreatedAt(new Date());
               }
       // System.out.println("final cart complete");
        //perform cart operation
        //if cart item already present ; then update

        AtomicReference<Boolean> updated = new AtomicReference(false);
        List<CartItem> items = cart.getItems();

        List<CartItem> updatedItems = items.stream().map(item -> {
            if (item.getProduct().getProductId().equals(productId)) {
                item.setQuantity(quantity);
                item.setTotalPrice(quantity * product.getPrice());
                updated.set(true);

            }
            return item;
        }).collect(Collectors.toList());

        //create items
        if (!updated.get()) {
            CartItem cartItem = CartItem.builder()
                    .quantity(quantity)
                    .totalPrice(quantity * product.getDiscountedPrice())
                    .cart(cart)
                    .product(product)
                    .build();
            cart.getItems().add(cartItem);
        }
            cart.setItems(updatedItems);
            cart.setUser(user);
            Cart updateCart = cartRepository.save(cart);
            return mapper.map(updateCart, CartDto.class);
        }

        @Override
        public void removeItemFromCart(String userId,int cartItem){
           // conditions
            CartItem cartItem1 = cartItemRepository.findById(cartItem).orElseThrow(() -> new ResourceNotFoundException("cart item not found in "));
            cartItemRepository.delete(cartItem1);
        }

        @Override
        public void clearCart (String userId){
        //fetch the user from db
            User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found in database"));
            Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new ResourceNotFoundException("cart of given not found"));
            cart.getItems().clear();
            cartRepository.save(cart);
        }

    @Override
    public CartDto getCartByUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found in database"));
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new ResourceNotFoundException("cart of given not found"));

        return mapper.map(cart, CartDto.class);
    }
}

