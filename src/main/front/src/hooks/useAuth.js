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
                const response = await axios.get('/api/verify-auth');
                console.log('Auth Response:', response.data);

                setIsAuthenticated(response.data.authenticated);
                setUserRole(response.data.role);

                // 여기서 권한 체크하는 부분을 제거하거나 수정
                if (requiredRoles &&
                    Array.isArray(requiredRoles) &&
                    !requiredRoles.includes(response.data.role)) {
                    const confirmed = window.confirm(
                        "글 작성을 위해서는 관리자 승인이 필요합니다. 권한을 요청하시겠습니까?"
                    );

                    if (confirmed) {
                        axios.post('/api/secure/role-change-request')
                            .then(() => {
                                alert('권한 요청이 전송되었습니다.');
                            })
                            .catch((error) => {
                                console.error('권한 요청 실패:', error);
                                alert(error.response?.data?.message || '권한 요청 중 오류가 발생했습니다.');
                            });
                    }
                    navigate('/');
                    return;
                }
            } catch (error) {
                console.error('Auth Error:', error);
                navigate('/login');
            } finally {
                setIsLoading(false);
            }
        };

        verifyAuth();
    }, [requiredRoles, navigate]);

    return { isAuthenticated, userRole, isLoading };
};

export default useAuth;