import React, { useContext } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { AuthContext } from '../../contexts/AuthContext';

const ProtectedRoute = ({ allowedRoles }) => {
    const { user, loading } = useContext(AuthContext);

    // Wait until the AuthContext finishes checking localStorage
    if (loading) {
        return <div className="text-center mt-5 spinner-border text-success" role="status"></div>;
    }

    // If no user is logged in, kick them to login
    if (!user) {
        return <Navigate to="/login" replace />;
    }

    // If they are logged in but don't have the right role, kick to unauthorized
    if (allowedRoles && !allowedRoles.includes(user.role)) {
        return <Navigate to="/unauthorized" replace />;
    }

    // If they pass all checks, render the child routes!
    return <Outlet />;
};

export default ProtectedRoute;
