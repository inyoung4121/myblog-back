import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from '../config/axiosConfig';
import Header from './Header';

axios.interceptors.response.use(
    response => response,
    async (error) => {
        const originalRequest = error.config;

        // 401 에러이고 새로운 액세스 토큰이 헤더에 있는 경우
        if (error.response.status === 401 && error.response.headers['authorization']) {
            const newToken = error.response.headers['authorization'].replace('Bearer ', '');
            // 새 토큰 저장
            localStorage.setItem('eureka_jwt_token', newToken);
            // 원래 요청의 헤더 업데이트
            originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
            // 실패한 요청 재시도
            return axios(originalRequest);
        }

        return Promise.reject(error);
    }
);

const CreatePost = () => {
    const location = useLocation();
    const isEdit = location.state?.isEdit;
    const existingPost = location.state?.post;

    const [title, setTitle] = useState(existingPost?.title || '');
    const [content, setContent] = useState(existingPost?.content || '');
    const [tags, setTags] = useState(existingPost?.tags || '');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        try {
            const token = localStorage.getItem('eureka_jwt_token');
            if (!token) {
                setError('로그인이 필요합니다.');
                return;
            }

            const config = {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                }
            };

            const postData = {
                title,
                content,
                tags: tags.split(',').map(tag => tag.trim())
            };

            let response;
            if (isEdit) {
                response = await axios.put(`/api/posts/update/${existingPost.id}`, postData, config);
            } else {
                response = await axios.post('/api/posts/create', postData, config);
            }

            if (response.status === 200) {
                navigate(`/api/posts/${response.data.id}`);
            }
        } catch (err) {
            if (err.response) {
                switch (err.response.status) {
                    case 400:
                        setError('입력 값이 올바르지 않습니다.');
                        break;
                    case 401:
                        setError('인증에 실패했습니다. 다시 로그인해주세요.');
                        break;
                    case 403:
                        setError('글 작성 권한이 없습니다');
                        break;
                    default:
                        setError(`글 ${isEdit ? '수정' : '작성'} 중 오류가 발생했습니다.`);
                }
            } else {
                setError('서버와의 통신 중 오류가 발생했습니다.');
            }
        }
    };


    return (
        <div className="min-h-screen bg-gray-100">
            <Header />
            <div className="container mx-auto px-4 sm:px-6 lg:px-8 pt-20 pb-12">
                <div className="bg-white shadow-xl rounded-lg overflow-hidden">
                    <div className="p-6 sm:p-10">
                        <h1 className="text-3xl font-bold mb-6">
                            {isEdit ? '글 수정' : '새 글 작성'}
                        </h1>
                        <form onSubmit={handleSubmit}>
                            <div className="mb-4">
                                <label htmlFor="title" className="block text-gray-700 text-sm font-bold mb-2">
                                    제목
                                </label>
                                <input
                                    type="text"
                                    id="title"
                                    value={title}
                                    onChange={(e) => setTitle(e.target.value)}
                                    className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                                    required
                                />
                            </div>
                            <div className="mb-4">
                                <label htmlFor="content" className="block text-gray-700 text-sm font-bold mb-2">
                                    내용
                                </label>
                                <textarea
                                    id="content"
                                    value={content}
                                    onChange={(e) => setContent(e.target.value)}
                                    className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                                    rows="10"
                                    required
                                />
                            </div>
                            <div className="mb-6">
                                <label htmlFor="tags" className="block text-gray-700 text-sm font-bold mb-2">
                                    태그 (쉼표로 구분)
                                </label>
                                <input
                                    type="text"
                                    id="tags"
                                    value={tags}
                                    onChange={(e) => setTags(e.target.value)}
                                    className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                                />
                            </div>
                            {error && <p className="text-red-500 text-xs italic mb-4">{error}</p>}
                            <div className="flex items-center justify-between">
                                <button
                                    type="submit"
                                    className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                                >
                                    {isEdit ? "글 수정" : "글 작성"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CreatePost;