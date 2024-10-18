import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';

const formatDate = (dateString) => {
    const options = { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' };
    return new Date(dateString).toLocaleDateString(undefined, options);
};

const PostDetail = () => {
    const [post, setPost] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const { postId } = useParams();

    useEffect(() => {
        const fetchPost = async () => {
            try {
                const response = await axios.get(`/api/posts/${postId}`);
                setPost(response.data);
                setLoading(false);
            } catch (err) {
                setError('Failed to fetch post details');
                setLoading(false);
            }
        };

        fetchPost();
    }, [postId]);

    if (loading) return <div>Loading...</div>;
    if (error) return <div>{error}</div>;
    if (!post) return <div>No post found</div>;

    return (
        <div className="container mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold mb-4">{post.title}</h1>
            <div className="mb-4 text-gray-600">
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
            <div className="mb-8 whitespace-pre-wrap">{post.content}</div>

            <h2 className="text-2xl font-bold mb-4">Comments</h2>
            {post.commentListDtoList.length > 0 ? (
                <ul>
                    {post.commentListDtoList.map((comment) => (
                        <li key={comment.id} className="mb-4 p-4 bg-gray-100 rounded">
                            <div className="flex justify-between items-center mb-2">
                <span className="font-semibold">
                  {comment.isAnonymous ? comment.anonymousName : comment.authorName}
                </span>
                                <span className="text-sm text-gray-600">{formatDate(comment.createdAt)}</span>
                            </div>
                            <p>{comment.content}</p>
                        </li>
                    ))}
                </ul>
            ) : (
                <p>No comments yet.</p>
            )}
        </div>
    );
};

export default PostDetail;