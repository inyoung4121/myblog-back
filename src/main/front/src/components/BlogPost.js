import React from 'react';
import { Link } from 'react-router-dom';
import { FaHeart } from 'react-icons/fa'
import { useTag } from '../contexts/TagContext';

const formatDate = (date) => {
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');

    return `${year}-${month}-${day} ${hours}:${minutes}`;
};

const BlogPost = React.forwardRef(({ post }, ref) => {
    const { selectedTags, toggleTag } = useTag();

    const handleTagClick = (e, tag) => {
        e.preventDefault();  // Link의 기본 동작 방지
        toggleTag(tag);
    };

    return (
        <Link to={`/api/posts/${post.id}`} className="block">
            <article
                ref={ref}
                className="bg-white shadow-[0_0_4px_2px_rgba(0,0,0,0.1)] rounded-2xl overflow-hidden h-full flex flex-col cursor-pointer transition-all duration-300 hover:scale-105 border border-emerald-100"
            >
                <div className="p-6 flex-grow">
                    <div className="flex justify-between items-center text-gray-400 text-sm mb-4">
                        <span>{formatDate(post.createdAt)}</span>
                        <div className="flex items-center">
                            <span className="mr-2">{post.authorName}</span>
                            <FaHeart className="text-red-500 mr-1" size={14} />
                            <span>{post.likeCount}</span>
                        </div>
                    </div>
                    <h2 className="text-2xl font-semibold mb-2 transition-colors duration-300 group-hover:text-blue-600">
                        {post.title}
                    </h2>
                    <p
                        className="text-gray-700 text-sm mb-4"
                        style={{
                            overflow: 'hidden',
                            display: '-webkit-box',
                            WebkitLineClamp: 3,
                            WebkitBoxOrient: 'vertical',
                            textOverflow: 'ellipsis'
                        }}
                    >
                        {post.contentPreview}
                    </p>
                    <div className="flex flex-wrap gap-2 mt-2">
                        {post.tags.map((tag, index) => (
                            <button
                                key={index}
                                onClick={(e) => handleTagClick(e, tag)}
                                className={`px-2 py-1.5 rounded-md text-xs font-medium transition-colors duration-200 ${
                                    selectedTags.includes(tag)
                                        ? 'bg-amber-200 text-gray-700 hover:bg-black hover:text-white'
                                        : 'bg-black text-white hover:bg-amber-200 hover:text-gray-700'
                                }`}
                            >
                                {tag}
                            </button>
                        ))}
                    </div>
                </div>
            </article>
        </Link>
    );
});

export default BlogPost;