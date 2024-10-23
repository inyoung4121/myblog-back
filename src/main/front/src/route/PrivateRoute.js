import { Navigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';

const PrivateRoute = ({ children, requiredRoles }) => {
    const { isAuthenticated, isLoading } = useAuth(requiredRoles);

    if (isLoading) return <div>Loading...</div>;
    return isAuthenticated ? children : <Navigate to="/login" />;
};

export default PrivateRoute;