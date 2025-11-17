import React from 'react';
import { Route, Redirect } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';

// This PrivateRoute checks for login and optionally for a specific role
function PrivateRoute({ children, role, ...rest }) {
    let auth = useAuth();

    return (
        <Route
            {...rest}
            render={({ location }) => {
                // 1Check if user is authenticated
                if (!auth.isAuthenticated) {
                    return (
                        <Redirect
                            to={{
                                pathname: "/login",
                                state: { from: location }
                            }}
                        />
                    );
                }

                // 2Check if a specific role is required
                if (role && auth.user.role !== role) {
                    // Useris logged in but does not have the required role
                    return (
                        <Redirect
                            to={{
                                pathname: "/error",
                                state: { from: location }
                            }}
                        />
                    );
                }

                return children;
            }}
        />
    );
}

export default PrivateRoute;