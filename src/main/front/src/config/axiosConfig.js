import axios from 'axios';

// axios 기본 설정
const axiosInstance = axios.create({
    baseURL: process.env.REACT_APP_API_BASE_URL || '', // API 기본 URL (필요한 경우)
    timeout: 10000, // 타임아웃 설정
    headers: {
        'Content-Type': 'application/json',
    }
});

// 요청 인터셉터
axiosInstance.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('eureka_jwt_token');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 응답 인터셉터
axiosInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        // 토큰 만료로 인한 401 에러 처리
        if (error.response?.status === 401 && error.response.headers['authorization']) {
            try {
                const newToken = error.response.headers['authorization'].replace('Bearer ', '');

                // 새 토큰 저장
                localStorage.setItem('eureka_jwt_token', newToken);

                // 원래 요청의 헤더 업데이트
                originalRequest.headers['Authorization'] = `Bearer ${newToken}`;

                // 실패한 요청 재시도
                return await axiosInstance(originalRequest);
            } catch (refreshError) {
                // 리프레시 토큰도 만료된 경우 로그아웃 처리
                localStorage.removeItem('eureka_jwt_token');
                window.location.href = '/login'; // 로그인 페이지로 리다이렉트
                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);

export default axiosInstance;