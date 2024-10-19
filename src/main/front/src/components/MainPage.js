// MainPage.js
import React, { useState, useEffect, useRef, useCallback } from 'react';
import BlogPost from './BlogPost';
import Sidebar from './Sidebar';
import Header from './Header';
import { useTag } from '../contexts/TagContext';  // 경로 확인 필요

const MainPage = () => {
    const [posts, setPosts] = useState([]);
    const [page, setPage] = useState(0);
    const [loading, setLoading] = useState(false);
    const [hasMore, setHasMore] = useState(true);
    const [error, setError] = useState(null);
    const lastPostElementRef = useRef(null);
    const { selectedTags } = useTag();

    const fetchPosts = useCallback(async (pageNum) => {
        if (loading) return;
        setLoading(true);
        setError(null);
        try {
            const tagsQuery = selectedTags.length > 0 ? `&tags=${selectedTags.join(',')}` : '';
            const url = `/api/posts?page=${pageNum}&size=10${tagsQuery}`;
            console.log('Fetching URL:', url);
            const response = await fetch(url);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
            console.log('Fetched Data:', data);
            setPosts(prevPosts => pageNum === 0 ? data.content : [...prevPosts, ...data.content]);
            setHasMore(!data.last);
            setPage(pageNum);
        } catch (error) {
            console.error('Failed to fetch posts:', error);
            setError('게시글을 불러오는데 실패했습니다. 나중에 다시 시도해주세요.');
        } finally {
            setLoading(false);
        }
    }, [selectedTags]);

    useEffect(() => {
        console.log('Selected Tags:', selectedTags);
        setPage(0);
        setPosts([]);
        setHasMore(true);
        fetchPosts(0);
    }, [selectedTags, fetchPosts]);

    useEffect(() => {
        const observer = new IntersectionObserver(
            entries => {
                if (entries[0].isIntersecting && hasMore && !loading) {
                    console.log('Fetching more posts...');
                    fetchPosts(page + 1);
                }
            },
            { threshold: 1 }
        );

        if (lastPostElementRef.current) {
            observer.observe(lastPostElementRef.current);
        }

        return () => {
            if (lastPostElementRef.current) {
                observer.unobserve(lastPostElementRef.current);
            }
        };
    }, [fetchPosts, hasMore, loading, page]);

    return (
        <div className="bg-gray-0 min-h-screen flex flex-col">
            <Header />
            <div className="flex-grow flex pt-16">
                <main className="flex-grow overflow-y-auto lg:pr-80">
                    <div className="container mx-auto px-4 py-8">
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-8 mx-8">
                            {posts.map((post, index) => (
                                <BlogPost
                                    key={post.id}
                                    post={post}
                                    ref={index === posts.length - 1 ? lastPostElementRef : null}
                                />
                            ))}
                        </div>
                        {loading && <p className="text-center mt-4 col-span-full">로딩 중...</p>}
                        {error && <p className="text-red-500 text-center mt-4 col-span-full">{error}</p>}
                        {!loading && posts.length === 0 && (
                            <p className="text-center mt-4 col-span-full">게시글이 없습니다.</p>
                        )}
                    </div>
                </main>
                <Sidebar />
            </div>
        </div>
    );
};

export default MainPage;