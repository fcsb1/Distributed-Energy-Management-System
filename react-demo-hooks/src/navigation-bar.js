import React from 'react';
import { DropdownItem, DropdownMenu, DropdownToggle, Nav, Navbar, NavbarBrand, NavLink, UncontrolledDropdown, Button } from 'reactstrap';
import { withRouter } from 'react-router-dom';
import { useAuth } from './auth/useAuth';

import logo from './commons/images/icon.png';

const textStyle = {
    color: 'white',
    textDecoration: 'none'
};

function NavigationBar(props) { // Receive props
    const auth = useAuth();
    const { history } = props; // Get history from props

    const handleLogout = () => {
        auth.logout();
        history.push('/');
    };

    return (
        <div>
            <Navbar color="dark" light expand="md">
                <NavbarBrand href="/">
                    <img src={logo} width={"50"}
                        height={"35"} alt="Logo" />
                </NavbarBrand>
                <Nav className="mr-auto" navbar>

                    {/* Admin Menu */}
                    {auth.isAuthenticated && auth.user.role === 'ROLE_ADMIN' &&
                        <UncontrolledDropdown nav inNavbar>
                            <DropdownToggle style={textStyle} nav caret>
                                Admin Menu
                            </DropdownToggle>
                            <DropdownMenu right>
                                <DropdownItem>
                                    <NavLink href="/admin/users">User Management</NavLink>
                                </DropdownItem>
                                <DropdownItem>
                                    <NavLink href="/admin/devices">Device Management</NavLink>
                                </DropdownItem>
                            </DropdownMenu>
                        </UncontrolledDropdown>
                    }

                    {/* Client Menu */}
                    {auth.isAuthenticated && auth.user.role === 'ROLE_CLIENT' &&
                        <NavLink href="/client/devices" style={textStyle}>My Devices</NavLink>
                    }

                </Nav>
                <Nav className="ml-auto" navbar>
                    {auth.isAuthenticated ? (
                        <Button color="link" style={textStyle} onClick={handleLogout}>
                            Logout ({auth.user.sub})
                        </Button>
                    ) : (
                        <NavLink href="/login" style={textStyle}>Login</NavLink>
                    )}
                </Nav>
            </Navbar>
        </div>
    );
}

export default withRouter(NavigationBar); // Wrap component