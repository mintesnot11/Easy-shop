package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@CrossOrigin
@PreAuthorize("hasRole('ROLE_USER')")
public class ShoppingCartController
{
    private final ShoppingCartDao shoppingCartDao;
    private final UserDao userDao;
    private final ProductDao productDao;

    @Autowired
    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    @GetMapping
    public ResponseEntity<ShoppingCart> getCart(Principal principal)
    {
        try
        {
            // get the currently logged in username
            String userName = principal.getName();
            // find database user by userId
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            // use the shoppingcartDao to get all items in the cart and return the cart
            ShoppingCart cart = shoppingCartDao.getByUserId(userId);
            return ResponseEntity.ok(cart);
        }
        catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @PostMapping("/products/{productId}")
    public ResponseEntity<ShoppingCart> addToCart(Principal principal, @PathVariable int productId, @RequestParam(defaultValue = "1") int quantity)
    {
        try
        {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            // Validate product exists
            if (productDao.getById(productId) == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
            }

            // Validate quantity
            if (quantity <= 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quantity required");
            }

            shoppingCartDao.addItem(userId, productId, quantity);
            
            // Return updated cart data
            ShoppingCart updatedCart = shoppingCartDao.getByUserId(userId);
            
            return ResponseEntity.ok(updatedCart);
        }
        catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<ShoppingCart> updateCartItem(Principal principal, @PathVariable int productId, @RequestBody ShoppingCartItem item)
    {
        try
        {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            // Validate product exists
            if (productDao.getById(productId) == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
            }

            // Validate quantity
            if (item.getQuantity() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity cannot be negative");
            }

            shoppingCartDao.updateItemQuantity(userId, productId, item.getQuantity());
            
            // Return updated cart data
            ShoppingCart updatedCart = shoppingCartDao.getByUserId(userId);
            return ResponseEntity.ok(updatedCart);
        }
        catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<ShoppingCart> removeFromCart(Principal principal, @PathVariable int productId)
    {
        try
        {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            shoppingCartDao.removeItem(userId, productId);
            
            // Return updated cart data
            ShoppingCart updatedCart = shoppingCartDao.getByUserId(userId);
            return ResponseEntity.ok(updatedCart);
        }
        catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @DeleteMapping
    public ResponseEntity<ShoppingCart> clearCart(Principal principal)
    {
        try
        {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            shoppingCartDao.clearCart(userId);
            
            // Return empty cart
            ShoppingCart emptyCart = new ShoppingCart();
            return ResponseEntity.ok(emptyCart);
        }
        catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
}
