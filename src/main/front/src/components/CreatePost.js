import React, { useState, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from '../config/axiosConfig';
import Header from './Header';

const CreatePost = () => {
    const location = useLocation();
    const isEdit = location.state?.isEdit;
    const existingPost = location.state?.post;

    const [title, setTitle] = useState(existingPost?.title || '');
    const [content, setContent] = useState(existingPost?.content || '');
    const [tags, setTags] = useState(existingPost?.tags || '');
    const [error, setError] = useState('');
    const [cursorPosition, setCursorPosition] = useState(0);
    const contentRef = useRef(null);
    const navigate = useNavigate();

    const handleImageUpload = async (e) => {
        const files = Array.from(e.target.files);

        try {
            for (const file of files) {
                const formData = new FormData();
                formData.append('image', file);

                const token = localStorage.getItem('eureka_jwt_token');
                const response = await axios.post('/api/posts/upload-image', formData, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'multipart/form-data'
                    }
                });

                const imageUrl = response.data.url;
                insertImageAtCursor(imageUrl);
            }
        } catch (err) {
            setError('이미지 업로드 중 오류가 발생했습니다.');
        }
    };

    const insertImageAtCursor = (imageUrl) => {
        const textarea = contentRef.current;
        const imageTag = `\n<img src="${imageUrl}" alt="uploaded image">\n`;

        const start = textarea.selectionStart;
        const end = textarea.selectionEnd;

        const newContent = content.substring(0, start) +
            imageTag +
            content.substring(end);

        setContent(newContent);

        // 커서를 이미지 태그 뒤로 이동
        const newCursorPosition = start + imageTag.length;
        setTimeout(() => {
            textarea.focus();
            textarea.setSelectionRange(newCursorPosition, newCursorPosition);
        }, 0);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        try {
            const token = localStorage.getItem('eureka_jwt_token');
            if (!token) {
                setError('로그인이 필요합니다.');
                return;
            }

            const postData = {
                title,
                content,
                tags: tags.split(',').map(tag => tag.trim())
            };

            const config = {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            };

            let response;
            if (isEdit) {
                response = await axios.put(`/api/posts/update/${existingPost.id}`, postData, config);
            } else {
                response = await axios.post('/api/posts/create', postData, config);
            }

            if (response.status === 200) {
                navigate(`/posts/${response.data.id}`);
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

    const handleContentClick = (e) => {
        setCursorPosition(e.target.selectionStart);
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
                                <label className="block text-gray-700 text-sm font-bold mb-2">
                                    이미지 추가
                                </label>
                                <input
                                    type="file"
                                    accept="image/*"
                                    onChange={handleImageUpload}
                                    className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                                    multiple
                                />
                                <p className="text-sm text-gray-500 mt-1">
                                    ※ 이미지를 추가하고 싶은 위치에 커서를 놓고 파일을 선택하세요
                                </p>
                            </div>
                            <div className="mb-4">
                                <label htmlFor="content" className="block text-gray-700 text-sm font-bold mb-2">
                                    내용
                                </label>
                                <textarea
                                    ref={contentRef}
                                    id="content"
                                    value={content}
                                    onChange={(e) => setContent(e.target.value)}
                                    onClick={handleContentClick}
                                    onKeyUp={handleContentClick}
                                    className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                                    rows="15"
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