import React, { createContext, useState, useEffect, useContext } from 'react';
import api from '../services/api';
import { AuthContext } from './AuthContext';

export const CartContext = createContext();

export const CartProvider = ({ children }) => {
    const { user } = useContext(AuthContext);
    const [cart, setCart] = useState(null);
    const [cartLoading, setCartLoading] = useState(false);

    const fetchCart = async () => {
        if (user && user.role === 'CUSTOMER') {
            try {
                setCartLoading(true);
                const res = await api.get('/cart');
                setCart(res.data);
            } catch (err) {
                console.error("Failed to load cart", err);
                setCart(null);
            } finally {
                setCartLoading(false);
            }
        } else {
            setCart(null);
        }
    };

    useEffect(() => {
        fetchCart();
        // eslint-disable-next-line
    }, [user]);

    const addToCart = async (productId, quantity) => {
        const res = await api.post('/cart/items', { productId, quantity });
        setCart(res.data);
        return res.data;
    };

    const updateQuantity = async (itemId, quantity) => {
        const res = await api.put(`/cart/items/${itemId}`, { quantity });
        setCart(res.data);
        return res.data;
    };

    const removeItem = async (itemId) => {
        const res = await api.delete(`/cart/items/${itemId}`);
        setCart(res.data);
        return res.data;
    };

    const clearCart = async () => {
        await api.delete('/cart/clear');
        fetchCart();
    };

    return (
        <CartContext.Provider value={{ cart, cartLoading, fetchCart, addToCart, updateQuantity, removeItem, clearCart }}>
            {children}
        </CartContext.Provider>
    );
};
