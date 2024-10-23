import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const useAuth = (requiredRoles) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [userRole, setUserRole] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const verifyAuth = async () => {
            try {
                const response = await axios.get('/api/verify-auth');        console.log('Auth Response:', response.data); // 응답 확인을 위한 로그

                setIsAuthenticated(response.data.isAuthenticated);
                setUserRole(response.data.role);

                if (requiredRoles &&
                    Array.isArray(requiredRoles) &&
                    !requiredRoles.includes(response.data.role)) {
                    alert("권한이 없습니다")
                    navigate('/');
                }
            } catch (error) {
                navigate('/login');
            } finally {
                setIsLoading(false);
            }
        };

        verifyAuth();
    }, [requiredRoles]);

    return { isAuthenticated, isLoading, userRole };
};

export default useAuth;