import React from 'react';
import { BrowserRouter as Router, Route, Switch, Redirect } from 'react-router-dom';
import NavigationBar from './navigation-bar';
import Home from './home/home';
import ErrorPage from './commons/errorhandling/error-page';
import styles from './commons/styles/project-style.css';

import LoginPage from './auth/LoginPage';
import PrivateRoute from './routing/PrivateRoute';
import UserContainer from './user/User-container';
import DeviceContainer from './device/DeviceContainer';
import ClientDeviceContainer from './client/ClientDeviceContainer';
import AdminChatDashboard from './client/AdminChatDashboard';

function App() {
    return (
        <div className={styles.back}>
            <Router>
                <div>
                    <NavigationBar />
                    <Switch>

                        <Route
                            exact
                            path='/'
                            render={() => <Home />}
                        />

                        <Route
                            exact
                            path='/login'
                            render={() => <LoginPage />}
                        />

                        {/* Admin Route for User Management */}
                        <PrivateRoute
                            exact
                            path='/admin/users'
                            role="ROLE_ADMIN"
                        >
                            <UserContainer />
                        </PrivateRoute>

                        {/* Admin Route for Device Management */}
                        <PrivateRoute
                            exact
                            path='/admin/devices'
                            role="ROLE_ADMIN"
                        >
                            <DeviceContainer />
                        </PrivateRoute>

                        {/* Client Route for Viewing Devices */}
                        <PrivateRoute
                            exact
                            path='/client/devices'
                            role="ROLE_CLIENT"
                        >
                            <ClientDeviceContainer />
                        </PrivateRoute>

                        {/* Admin Route for Chat Dashboard */}
                        <PrivateRoute
                            exact
                            path='/admin/chat'
                            role="ROLE_ADMIN"
                        >
                            <AdminChatDashboard />
                        </PrivateRoute>


                        {/* Client Route for Viewing Devices and Chat */}
                        <PrivateRoute
                            exact
                            path='/client/devices'
                            role="ROLE_CLIENT"
                        >
                            <ClientDeviceContainer />
                        </PrivateRoute>

                        {/* Error Pages */}
                        <Route
                            exact
                            path='/error'
                            render={() => <ErrorPage />}
                        />

                        {/* Default redirect for any other path */}
                        <Route render={() => <Redirect to="/error" />} />
                    </Switch>
                </div>
            </Router>
        </div>
    );
}

export default App;