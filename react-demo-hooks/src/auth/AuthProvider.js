import React, { createContext, useState, useEffect } from 'react';
import { jwtDecode } from 'jwt-decode';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [auth, setAuth] = useState({
        token: null,
        isAuthenticated: false,
        user: null, // user: { sub: "username", role: "ROLE_ADMIN", userId: "uuid" }
        isLoading: true // start in loading state
    });

    useEffect(() => {
        // check for token in localStorage on initial load
        const token = localStorage.getItem('token');
        if (token) {
            try {
                const decodedUser = jwtDecode(token);

                setAuth({
                    token: token,
                    isAuthenticated: true,
                    user: decodedUser,
                    isLoading: false
                });
            } catch (e) {
                // Invalid token
                localStorage.removeItem('token');
                setAuth({ token: null, isAuthenticated: false, user: null, isLoading: false });
            }
        } else {
            setAuth({ token: null, isAuthenticated: false, user: null, isLoading: false });
        }
    }, []);

    const login = (token) => {
        try {
            const decodedUser = jwtDecode(token);
            localStorage.setItem('token', token);
            setAuth({
                token: token,
                isAuthenticated: true,
                user: decodedUser,
                isLoading: false
            });
        } catch (e) {
            console.error("Failed to decode token", e);
            logout();
        }
    };

    const logout = () => {
        localStorage.removeItem('token');
        setAuth({ token: null, isAuthenticated: false, user: null, isLoading: false });
    };

    return (
        <AuthContext.Provider value={{ ...auth, login, logout }}>
            {!auth.isLoading && children}
        </AuthContext.Provider>
    );
};