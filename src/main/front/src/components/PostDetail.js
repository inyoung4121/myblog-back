import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism';
import Header from './Header';
import { v4 as uuidv4 } from 'uuid';


const formatDate = (dateString) => {
    const date = new Date(dateString);
    const year = date.getFullYear().toString().slice(-2);
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${year}.${month}.${day} ${hours}:${minutes}`;
};

const Comment = ({ comment, currentUserId, onDelete }) => {
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [deletePassword, setDeletePassword] = useState('');
    const [error, setError] = useState('');

    const handleDeleteClick = () => {
        setShowDeleteConfirm(true);
    };

    const handleDeleteConfirm = async () => {
        try {
            let url = `/api/comments/${comment.id}`;
            let params = {};
            if (comment.anonymous) {
                params.deletePassword = deletePassword;
            } else {
                params.userId = currentUserId;
            }
            await axios.delete(url, { params });
            onDelete(comment.id);
        } catch (err) {
            if (err.response) {
                switch (err.response.status) {
                    case 403:
                        setError('댓글 삭제 권한이 없습니다.');
                        break;
                    case 404:
                        setError('해당 댓글을 찾을 수 없습니다.');
                        break;
                    default:
                        setError('댓글 삭제 중 오류가 발생했습니다.');
                }
            } else {
                setError('서버와의 통신 중 오류가 발생했습니다.');
            }
        }
        setShowDeleteConfirm(false);
        setDeletePassword('');
    };

    const showDeleteButton = comment.anonymous || comment.authorId === currentUserId;

    return (
        <div className="bg-gray-50 p-4 rounded-lg mb-4">
            <div className="flex justify-between items-center mb-2">
                <div>
                    <span className="text-lg font-semibold text-gray-800 mr-4">
                        {comment.anonymous ? comment.anonymousName : comment.authorName}
                    </span>
                    <span className="text-sm text-gray-500">{formatDate(comment.createdAt)}</span>
                </div>
                {showDeleteButton && !showDeleteConfirm && (
                    <button
                        onClick={handleDeleteClick}
                        className="text-red-500 hover:text-red-700"
                    >
                        삭제
                    </button>
                )}
            </div>
            <p className="mb-2">{comment.content}</p>
            {showDeleteConfirm && (
                <div className="mt-2 flex items-center">
                    {comment.anonymous && (
                        <input
                            type="password"
                            value={deletePassword}
                            onChange={(e) => setDeletePassword(e.target.value)}
                            placeholder="삭제 비밀번호를 입력하세요"
                            className="mr-2 p-1 border rounded flex-grow"
                        />
                    )}
                    <button
                        onClick={handleDeleteConfirm}
                        className="bg-red-500 text-white px-2 py-1 text-sm rounded hover:bg-red-600"
                    >
                        삭제 확인
                    </button>
                    <button
                        onClick={() => {
                            setShowDeleteConfirm(false);
                            setDeletePassword('');
                            setError('');
                        }}
                        className="ml-2 bg-gray-300 text-gray-700 px-2 py-1 text-sm rounded hover:bg-gray-400"
                    >
                        취소
                    </button>
                </div>
            )}
            {error && <div className="text-red-500 mt-2">{error}</div>}
        </div>
    );
};

const PostDetail = () => {
    const [post, setPost] = useState(null);
    const [comments, setComments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [commentError, setCommentError] = useState('');
    const [newComment, setNewComment] = useState('');
    const [anonymousName, setAnonymousName] = useState('');
    const [anonymousPassword, setAnonymousPassword] = useState('');
    const { postId } = useParams();
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [currentUserId, setCurrentUserId] = useState(null);
    const [isLiked, setIsLiked] = useState(false);
    const [likeCount, setLikeCount] = useState(0);
    const [deviceId, setDeviceId] = useState('');
    const [isLiking, setIsLiking] = useState(false);
    const [isAuthor, setIsAuthor] = useState(false);
    const navigate = useNavigate();  // useNavigate hook 선언

    useEffect(() => {
        const token = localStorage.getItem('eureka_jwt_token');
        setIsLoggedIn(!!token);

        let storedDeviceId = localStorage.getItem('device_id');
        if (!storedDeviceId) {
            storedDeviceId = uuidv4();
            localStorage.setItem('device_id', storedDeviceId);
        }
        setDeviceId(storedDeviceId);

        const fetchPostAndComments = async () => {
            try {
                const postResponse = await axios.get(`/api/posts/${postId}`);
                setPost(postResponse.data);
                setComments(postResponse.data.commentListDtoList || []);
                setLikeCount(postResponse.data.likeCount);
                setLoading(false);

                const userStr = localStorage.getItem('user');
                if (userStr) {
                    const user = JSON.parse(userStr);
                    // 게시글 작성자와 현재 사용자 비교
                    setIsAuthor(postResponse.data.authorName === user.username);
                }

                setLoading(false);

                const likeStatusResponse = await axios.get(`/api/posts/${postId}/like`, {
                    params: { deviceId: storedDeviceId }
                });
                setIsLiked(likeStatusResponse.data.liked);
            } catch (err) {
                setError('게시글을 불러오는데 실패했습니다.');
                setLoading(false);
            }
        };

        fetchPostAndComments();
    }, [postId]);



    const handleLike = async () => {
        if (isLiking) return;

        try {
            setIsLiking(true);
            if (!deviceId) {
                setError('Device ID not found');
                return;
            }

            const response = await axios.post(`/api/posts/${postId}/like`, null, {
                params: { deviceId }
            });

            setIsLiked(response.data.liked);
            setLikeCount(response.data.totalLikes);
        } catch (err) {
            setError('좋아요 처리 중 오류가 발생했습니다.');
        } finally {
            setIsLiking(false);
        }
    };

    const handleEdit = () => {
        navigate(`/create/post`, {
            state: {
                isEdit: true,
                post: {
                    id: postId,
                    title: post.title,
                    content: post.content,
                    tags: post.tags.join(',')
                }
            }
        });
    };

    const handleDelete = async () => {
        if (!window.confirm('정말로 이 글을 삭제하시겠습니까?')) {
            return;
        }

        try {
            await axios.delete(`/api/posts/${postId}`);
            navigate('/'); // 홈으로 이동
        } catch (err) {
            if (err.response?.status === 401) {
                setError('삭제 권한이 없습니다.');
            } else {
                setError('글 삭제 중 오류가 발생했습니다.');
            }
        }
    };

    const handleCommentSubmit = async (e) => {
        e.preventDefault();
        setCommentError('');

        try {
            const token = localStorage.getItem('eureka_jwt_token');
            const headers = token ? { Authorization: `Bearer ${token}` } : {};

            if (!isLoggedIn && (!anonymousName.trim() || !anonymousPassword.trim())) {
                setCommentError('닉네임과 비밀번호를 모두 입력해주세요.');
                return;
            }

            const commentDto = {
                content: newComment,
                postId: parseInt(postId),
                anonymous: !isLoggedIn,
                anonymousName: !isLoggedIn ? anonymousName : null,
                deletePassword: !isLoggedIn ? anonymousPassword : null
            };

            const response = await axios.post('/api/comments', commentDto, { headers });
            setComments(prevComments => [response.data, ...prevComments]);

            // 입력 폼 초기화
            setNewComment('');
            if (!isLoggedIn) {
                setAnonymousName('');
                setAnonymousPassword('');
            }
        } catch (err) {
            setCommentError(err.response?.data?.message || '댓글 작성 중 오류가 발생했습니다.');
        }
    };

    const handleCommentDelete = (commentId) => {
        setComments(comments.filter(comment => comment.id !== commentId));
    };

    const customRenderers = {
        code({node, inline, className, children, ...props}) {
            const match = /language-(\w+)/.exec(className || '');
            return !inline && match ? (
                <SyntaxHighlighter
                    style={vscDarkPlus}
                    language={match[1]}
                    PreTag="div"
                    customStyle={{padding: 15}}
                    {...props}
                >
                    {String(children).replace(/\n$/, '')}
                </SyntaxHighlighter>
            ) : (
                <code className={className} {...props}>
                    {children}
                </code>
            );
        },
        blockquote({node, children}) {
            return (
                <blockquote className="bg-gray-100 border-l-4 border-gray-600 py-1 px-4 mb-2 rounded text-gray-600">
                    {children}
                </blockquote>
            );
        },
        // 이미지 렌더링 커스터마이징
        img({src, alt}) {
            return (
                <div className="my-4">
                    <img
                        src={src}
                        alt={alt || ''}
                        className="max-w-full h-auto rounded-lg shadow-lg mx-auto"
                        style={{ maxHeight: '600px', objectFit: 'contain' }}
                    />
                </div>
            );
        },
        // 단락 렌더링 커스터마이징
        p({children}) {
            return (
                <p className="mb-4 whitespace-pre-wrap">
                    {children}
                </p>
            );
        }
    };

    const processContent = (content) => {
        // img 태그를 마크다운 이미지 문법으로 변환
        return content.replace(
            /<img\s+src="([^"]+)"\s+alt="([^"]*)">/g,
            (match, src, alt) => `\n\n![${alt}](${src})\n\n`
        );
    };



    if (loading) return <div className="pt-20 text-center">Loading...</div>;
    if (error) return <div className="pt-20 text-center text-red-600">{error}</div>;
    if (!post) return <div className="pt-20 text-center">게시글을 찾을 수 없습니다.</div>;

    return (
        <div className="min-h-screen bg-gray-100">
            <Header/>
            <div className="container mx-auto px-4 sm:px-6 lg:px-8 pt-20 pb-12">
                <div className="bg-white shadow-xl rounded-lg overflow-hidden">
                    <div className="p-6 sm:p-10">
                        <div className="flex justify-between items-center mb-4">
                            <h1 className="text-4xl font-bold">{post.title}</h1>
                            {isAuthor && (
                                <div className="flex gap-2">
                                    <button
                                        onClick={handleEdit}
                                        className="px-4 py-2 text-sm text-blue-600 hover:text-blue-800"
                                    >
                                        수정
                                    </button>
                                    <button
                                        onClick={handleDelete}
                                        className="px-4 py-2 text-sm text-red-600 hover:text-red-800"
                                    >
                                        삭제
                                    </button>
                                </div>
                            )}
                        </div>
                        <div className="text-gray-600 mb-2">
                            <span>By {post.authorName}</span>
                            <span className="mx-2">•</span>
                            <span>Created: {formatDate(post.createdAt)}</span>
                            {post.updatedAt && (
                                <>
                                    <span className="mx-2">•</span>
                                    <span>Updated: {formatDate(post.updatedAt)}</span>
                                </>
                            )}
                        </div>
                        {post.tags?.filter(tag => tag.trim()).length > 0 && (
                            <div className="mb-6">
                                {post.tags
                                    .filter(tag => tag.trim())
                                    .map((tag, index) => (
                                        <span key={index}
                                              className="inline-block bg-black rounded-md px-2 py-1.5 text-xs font-medium text-white mr-2 mb-2">
                {tag}
            </span>
                                    ))}
                            </div>
                        )}
                        <div className="prose max-w-none mb-8">
                            <ReactMarkdown
                                remarkPlugins={[remarkGfm]}
                                components={customRenderers}
                            >
                                {processContent(post.content)}
                            </ReactMarkdown>
                        </div>

                        <div className="mt-12">
                            <div className="flex items-center mb-4">
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" className="h-8 w-8 mr-2">
                                    <path
                                        d="M20,4H4C2.9,4,2,4.9,2,6v12c0,1.1,0.9,2,2,2h4l3,3l3-3h6c1.1,0,2-0.9,2-2V6C22,4.9,21.1,4,20,4z"
                                        fill="#90EE90" stroke="#228B22" strokeWidth="1.5"/>
                                    <circle cx="7" cy="11" r="1.5" fill="#228B22"/>
                                    <circle cx="12" cy="11" r="1.5" fill="#228B22"/>
                                    <circle cx="17" cy="11" r="1.5" fill="#228B22"/>
                                </svg>
                                <h2 className="text-lg font-bold">댓글 {comments.length}개</h2>
                                <div className="ml-6 flex items-center">
                                    <button
                                        onClick={handleLike}
                                        className="flex items-center space-x-2 focus:outline-none"
                                    >
                                        <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" viewBox="0 0 24 24"
                                             stroke="currentColor" fill={isLiked ? "red" : "none"}>
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                                  d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
                                        </svg>
                                        <span className="text-lg font-semibold">
                                            좋아요 {likeCount}개
                                        </span>
                                    </button>
                                </div>
                            </div>

                            {comments.map((comment) => (
                                <Comment
                                    key={comment.id}
                                    comment={comment}
                                    currentUserId={currentUserId}
                                    onDelete={handleCommentDelete}
                                />
                            ))}

                            <form onSubmit={handleCommentSubmit} className="mt-8">
                                {!isLoggedIn && (
                                    <>
                                        <div className="flex items-center mb-2">
                                            <svg xmlns="http://www.w3.org/2000/svg"
                                                 className="h-6 w-6 mr-2 text-gray-500"
                                                 fill="none"
                                                 viewBox="0 0 24 24"
                                                 stroke="currentColor">

                                                <path strokeLinecap="round"
                                                      strokeLinejoin="round"
                                                      strokeWidth={2}
                                                      d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
                                            </svg>
                                            <input
                                                type="text"
                                                value={anonymousName}
                                                onChange={(e) => setAnonymousName(e.target.value)}
                                                className="p-2 border-b border-gray-300 focus:border-blue-500 outline-none w-44"
                                                placeholder="닉네임을 입력해주세요"
                                                required
                                            />
                                        </div>
                                        <div className="flex items-center mb-2">
                                            <svg xmlns="http://www.w3.org/2000/svg"
                                                 className="h-6 w-6 mr-2 text-gray-500"
                                                 fill="none"
                                                 viewBox="0 0 24 24"
                                                 stroke="currentColor">
                                                <path strokeLinecap="round"
                                                      strokeLinejoin="round"
                                                      strokeWidth={2}
                                                      d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
                                            </svg>
                                            <input
                                                type="password"
                                                value={anonymousPassword}
                                                onChange={(e) => setAnonymousPassword(e.target.value)}
                                                className="p-2 border-b border-gray-300 focus:border-blue-500 outline-none w-44"
                                                placeholder="비밀번호를 입력해주세요"
                                                required
                                            />
                                        </div>
                                    </>
                                )}

                                <div className="relative">
                                    <textarea
                                        value={newComment}
                                        onChange={(e) => setNewComment(e.target.value)}
                                        className="w-full mb-4 p-2 border border-gray-300 rounded-lg focus:border-blue-500 outline-none"
                                        rows="6"
                                        placeholder={isLoggedIn ?
                                            "댓글을 작성해주세요." :
                                            "비회원으로 댓글을 작성합니다. 댓글 삭제 시 입력하신 비밀번호가 필요합니다."}
                                        required
                                    />
                                    {commentError && (
                                        <div className="text-red-500 text-sm mb-2">{commentError}</div>
                                    )}
                                </div>

                                <div className="flex justify-between items-center">
                                    {!isLoggedIn && (
                                        <span className="text-sm text-gray-500">
                                            회원으로 댓글을 작성하시면 더 많은 기능을 사용할 수 있습니다.
                                        </span>
                                    )}
                                    <button
                                        type="submit"
                                        className="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition-colors"
                                    >
                                        댓글 작성
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PostDetail;